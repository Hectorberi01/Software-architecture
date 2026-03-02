# C4 Container Diagram — Parking Reservation System

## Vue d'ensemble

Ce document présente le diagramme C4 de niveau 2 (Container) du système de réservation de parking. Il détaille les conteneurs technologiques qui composent le système, leurs responsabilités, les technologies utilisées et les flux de communication entre eux.

Ce diagramme complète le diagramme de contexte C4 niveau 1 (`docs/diagrams/c4/C4-01-context.puml`) en décomposant le système en ses unités déployables distinctes, orchestrées par Docker Compose.

---

## Diagramme PlantUML

```plantuml
@startuml C4-Container-ParkingSystem
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

LAYOUT_WITH_LEGEND()

title C4 Level 2 — Container Diagram | Parking Reservation System

' ─── Acteurs ───────────────────────────────────────────────────────────────
Person(employee, "Employé", "Réserve une place (max 5 jours), fait le check-in QR, consulte ses réservations.")
Person(manager, "Manager", "Réserve jusqu'à 30 jours + consulte le dashboard analytique.")
Person(admin, "Secrétaire (Admin)", "Back-office complet : gestion utilisateurs, réservations, historique.")

' ─── Boundary système ───────────────────────────────────────────────────────
System_Boundary(parking_system, "Parking Reservation System — Docker Compose Network: parking-net") {

    Container(spa, "Frontend SPA", "React 18, TypeScript, Vite (build), Nginx (serve)", "Interface utilisateur responsive mobile-first. Fonctionnalités : login, carte interactive des places, création/annulation de réservation, check-in QR via caméra navigateur, dashboard analytics (Manager/Admin).\nExposé sur le port 3000 (hôte) → 80 (conteneur Nginx).")

    Container(api, "Backend API", "Spring Boot 3.2.2, Java 21, Maven", "API REST /api/v1. Logique métier : règles de réservation, validation des rôles RBAC, check-in. Authentification JWT (jjwt 0.12.3) via JwtAuthenticationFilter + Spring Security 6. Publication d'événements AMQP vers RabbitMQ.\nExposé sur le port 8080.")

    Container(scheduler, "NoShow Scheduler", "Spring @Scheduled (intégré au Backend API)", "Tâche CRON exécutée à 11h00 du lundi au vendredi. Marque comme NO_SHOW toutes les réservations du jour dont le check-in n'a pas été effectué. Met à jour le statut en base et publie un événement RESERVATION_NO_SHOW.")

    ContainerDb(db, "Base de données", "MySQL 8.0, InnoDB, Flyway", "Stockage persistant transactionnel. Tables principales : users, parking_spots, reservations. Migrations versionnées via Flyway (V1__, V2__...). Volume Docker : mysql_data.\nExposé sur le port 3306.")

    Container(mq, "Message Broker", "RabbitMQ 3 (image rabbitmq:3-management-alpine)", "Exchange TOPIC 'parking.notifications'. Queue durable 'email.send'. Routing key 'spots.listed'. Découple la publication d'événements de réservation du traitement asynchrone (envoi e-mail). Interface d'administration web Management Plugin.\nPorts : 5672 (AMQP), 15672 (Management UI).")
}

' ─── Système externe ────────────────────────────────────────────────────────
System_Ext(email_service, "Service E-mail (futur)", "Consommateur AMQP. Lit les messages de la queue 'email.send' et envoie les e-mails de confirmation/annulation/no-show aux utilisateurs via SMTP.")

' ─── Relations ──────────────────────────────────────────────────────────────
Rel(employee, spa, "Utilise", "HTTPS / Navigateur")
Rel(manager, spa, "Utilise", "HTTPS / Navigateur")
Rel(admin, spa, "Utilise", "HTTPS / Navigateur")

Rel(spa, api, "Appelle l'API REST (Authorization: Bearer JWT)", "REST/JSON, HTTPS, port 8080")

Rel(api, db, "Lit et écrit les entités (User, ParkingSpot, Reservation)", "JPA/Hibernate, SQL, port 3306")
Rel(api, mq, "Publie les événements de réservation", "AMQP, port 5672")

Rel(scheduler, db, "Met à jour le statut NO_SHOW des réservations non pointées", "JPA/Hibernate, SQL, port 3306")

Rel(mq, email_service, "Livre les messages de notification (futur consommateur)", "AMQP, port 5672")

@enduml
```

---

## Composants clés

| Composant | Technologie | Port(s) | Responsabilité principale |
|---|---|---|---|
| Frontend SPA | React 18 + TypeScript + Vite + Nginx | 3000 (hôte) | UI réservations, carte parking, check-in QR, analytics |
| Backend API | Spring Boot 3.2.2 + Java 21 | 8080 | Auth JWT, RBAC, logique métier réservations, check-in |
| NoShow Scheduler | Spring `@Scheduled` (intégré API) | — | Automatisation NO_SHOW quotidien à 11h |
| MySQL 8 | InnoDB + Flyway (migrations) | 3306 | Persistance transactionnelle, migrations versionnées |
| RabbitMQ 3 | AMQP + Management Plugin | 5672, 15672 | Événements de réservation, découplage asynchrone |
| Service E-mail | Consommateur AMQP (futur) | — | Envoi e-mails confirmation/annulation/no-show |

---

## Flux d'authentification (ADR-010)

```
1. Login
   Client ──POST /api/v1/auth/login──► Backend
                                       ├── BCrypt.verify(password)
                                       ├── JwtService.generateAccessToken() [expire: 15 min]
                                       └── JwtService.generateRefreshToken() [expire: 7 jours]
   Client ◄── { accessToken, refreshToken } ──────────────────────────────

2. Requêtes authentifiées
   Client ──GET /api/v1/reservations──► JwtAuthenticationFilter
           Authorization: Bearer <jwt>   ├── Valider signature HS256
                                         ├── Vérifier exp claim
                                         ├── Extraire email + role
                                         └── Injecter SecurityContext
                                        Route Handler (@PreAuthorize)
   Client ◄── 200 OK + données ────────────────────────────────────────────

3. Renouvellement silencieux
   Client ──POST /api/v1/auth/refresh──► Backend [valide refreshToken en base]
   Client ◄── { accessToken } (nouveau, 15 min) ───────────────────────────
```

---

## Flux de réservation avec événement asynchrone (ADR-011)

```
1. Création de réservation
   Client ──POST /api/v1/reservations──► Backend API
                                         ├── Authentifier (JWT)
                                         ├── Autoriser (ROLE_EMPLOYEE / MANAGER / ADMIN)
                                         ├── Valider règles métier
                                         │    ├── Place disponible ?
                                         │    ├── Quota jours (5 / 30) respecté ?
                                         │    └── Pas de doublon même date ?
                                         ├── INSERT reservation → MySQL
                                         └── RabbitTemplate.convertAndSend()
                                              exchange: parking.notifications
                                              routingKey: spots.listed
                                              payload: ReservationCreatedEvent (JSON)
   Client ◄── 201 Created + ReservationDto ────────────────────────────────

2. Traitement asynchrone (découplé)
   RabbitMQ queue email.send ──► Service E-mail (futur consommateur)
                                  ├── Désérialiser ReservationCreatedEvent
                                  ├── Envoyer e-mail SMTP à userEmail
                                  └── ACK → message supprimé de la queue

3. Gestion des NO_SHOW (scheduler)
   CRON 0 0 11 * * MON-FRI ──► NoShow Scheduler
                                ├── SELECT reservations WHERE date=today AND status=CONFIRMED AND checkedIn=false
                                ├── UPDATE status = NO_SHOW
                                └── RabbitTemplate.convertAndSend() [RESERVATION_NO_SHOW event]
```

---

## Topologie réseau Docker Compose

```
┌─────────────────────────────────────────────────────────────┐
│  Docker network: parking-net (bridge)                       │
│                                                             │
│  ┌──────────────┐    REST :8080    ┌──────────────────┐     │
│  │  frontend    │ ───────────────► │    backend       │     │
│  │  (Nginx :80) │                  │  (Spring Boot)   │     │
│  └──────────────┘                  └────────┬─────────┘     │
│         ▲                                   │               │
│         │ :3000                   SQL :3306 │  AMQP :5672   │
│   (hôte)│                                   │               │
│                               ┌─────────────┘               │
│                               │             │               │
│                        ┌──────▼──────┐ ┌────▼─────────┐     │
│                        │   mysql     │ │  rabbitmq    │     │
│                        │  (MySQL 8)  │ │  (:5672      │     │
│                        └─────────────┘ │   :15672)    │     │
│                                        └──────────────┘     │
└─────────────────────────────────────────────────────────────┘

Ports exposés sur l'hôte :
  3000  → Frontend SPA (Nginx)
  8080  → Backend API (Spring Boot)
  3306  → MySQL (dev/debug uniquement)
  5672  → RabbitMQ AMQP
  15672 → RabbitMQ Management UI
```

---

## Références

- ADR-005 : Authentification et autorisation (RBAC, JWT, rôles)
- ADR-006 : File de messages pour les notifications (choix RabbitMQ)
- ADR-007 : Conteneurisation (Docker Compose, réseau parking-net)
- ADR-009 : Design de l'API REST (/api/v1, conventions)
- ADR-010 : Implémentation JWT avec Spring Security 6
- ADR-011 : Architecture événementielle avec RabbitMQ
- Diagramme C4 Contexte : `docs/diagrams/c4/C4-01-context.puml`
- Diagramme C4 Container (PlantUML source) : `docs/diagrams/c4/C4-02-container.puml`
