# Parkreserve - Système de Réservation de Parking d'Entreprise

Parkreserve est une solution logicielle complète permettant la gestion intelligente des places de stationnement au sein d'une entreprise. Développée avec une architecture moderne, séparée entre un front-end réactif et un back-end robuste, l'application répond à des règles métiers exigeantes (limites de durée, check-in, gestion des bornes électriques).

Ce dépôt contient l'ensemble du code source, la documentation architecturale et la configuration de déploiement.

---

##  Fonctionnalités Principales

- **Système de rôles avancés (RBAC) :**
  - **Employé :** Peut réserver une place pour un maximum de 5 jours ouvrables. Doit procéder au check-in quotidiennement.
  - **Manager :** Accède à un tableau de bord analytique et possède une flexibilité de réservation (jusqu'à 30 jours calendaires continus).
  - **Secrétaire (Admin) :** Gère les utilisateurs et toutes les réservations, gère les profils nécessitant un accès de long terme.
- **Gestion des Bornes de Recharge :** Les rangées A et F sont équipées de bornes électriques (affichage dynamique  sur l'UI).
- **Processus de Check-in (QR Code) :** Points d'accès dédiés permettant la validation de la réservation. Une tâche planifiée (CRON) libère automatiquement les places non réclamées à 11h00 (No-Show).
- **Tableau de Bord Analytique :** Calcul du taux d'occupation, des statistiques de non-présentation et graphiques d'usage.
- **Notifications Asynchrones :** Envoi d'e-mails de confirmation (simulés) déchargés du flux principal via une file de messages (Event-Driven).

---

## 🏗 Architecture & Choix Techniques

Le système repose sur un modèle d'architecture en couches (N-Tiers) modernisé, intégrant des concepts **Event-Driven** pour améliorer la résilience et les performances (voir [ADR-003](docs/adr/ADR-003-architecture-backend.md) et [ADR-011](docs/adr/ADR-011-event-driven-messaging.md)). L'architecture complète est modélisée dans nos [diagrammes C4 (C4-container-diagram.md)](docs/diagrams/C4-container-diagram.md).

### Stack Back-End
- **Java 21 & Spring Boot 3 :** Le cœur du réacteur. Offre l'écosystème Spring pour l'IoC, la gestion transactionnelle, et le web.
- **Spring Security & JWT :** Authentification robuste et API stateless ([ADR-005](docs/adr/ADR-005-authentification-autorisation.md) / [ADR-010](docs/adr/ADR-010-authentication-jwt.md)).
- **MySQL 8 & Spring Data JPA (Hibernate) :** SGBD relationnel strict pour garantir la cohérence des transactions de réservation ([ADR-004](docs/adr/ADR-004-base-de-donnees.md)).
- **Flyway :** Outil de migration de schémas de base de données pour versionner l'état de la BDD de manière as-code.
- **RabbitMQ :** File d'attente / Message Broker utilisé pour découpler l'envoi des e-mails du processus de réservation, évitant un blocage de l'API ([ADR-006](docs/adr/ADR-006-file-de-messages.md)).

### Stack Front-End
- **React 18 & TypeScript :** UI basée sur les composants fortement typés réduisant drastiquement les régressions visuelles.
- **Vite :** Outil de build nouvelle génération pour une expérience de développement instantanée.
- **Recharts :** Bibliothèque de graphiques déclaratifs pour le tableau de bord Analytics.
- **Axios & React Router :** Gestion des appels HTTP via un client interceptant les tokens JWT de manière transparente, et un routing protégé côté client.

### Déploiement & DevOps
- **Docker & Docker Compose :** Conteneurisation de tous les services (Base de données, RabbitMQ, Back-backend, Frontend Proxy) pour assurer l'isolement et la fiabilité entre développement et production ([ADR-007](docs/adr/ADR-007-conteneurisation.md)).
- **Nginx :** Conteneurise le frontend construit par Vite et agit comme reverse-proxy (routing des appels de l'UI de `/api/v1` vers le backend interne).

---

## 📂 Organisation du Code Source

- **`backend/`** : Code source Java Spring Boot.
  - `src/main/java/com/parking/reservation/` : Logique de réservation critique, vérification des contraintes de jours (ouvré vs calendaire), conflits de dates.
  - `src/main/java/com/parking/messaging/` : Publication et Consommation des événements (RabbitMQ Listener).
  - `src/main/java/com/parking/checkin/` : Processus de check-in et CRON Scheduler libérant les Non-Shows.
- **`frontend/`** : Code source React Vite TypeScript.
  - `src/pages/` : Les pages principales (Dashboard, Parking, Authentication).
  - `src/api/` : Les clients HTTP de communication avec l'API.
  - `src/contexts/` : Contexte d'authentification et de gestion de session globale (Context API).
- **`docs/`** : Toute la documentation d'architecture détaillée (Architecture Decision Records - ADR).
  - Consulter l'ensemble des ADRs pour comprendre l'évolution explicite des décisions techniques prises.

---

## 🚀 Guide de Démarrage (Développement / Local)

Pour lancer le projet localement et observer l'ensemble de l'écosystème :

### Prérequis
- [Docker](https://www.docker.com/) et Docker Compose installés et lancés sur votre machine.

### Installation & Lancement

1. **Démarrer les conteneurs :**
   À la racine du projet, exécutez simplement :
   ```bash
   docker-compose up -d --build
   ```

2. **Accès aux Interfaces :**
   - **Frontend (Web Application)** : `http://localhost:3000` (Développement) ou `http://localhost:3001` (Docker Nginx) selon la configuration courante exposée.
   - **Back-End (API Root)** : `http://localhost:8080/api/v1/`
   - **RabbitMQ Management UI** : `http://localhost:15672` (Outil d'administration avec guest/guest)

3. **Comptes de test pré-configurés (Migration Flyway) :**
   - **Employé :** `employee@parking.com` / `employee123`
   - **Manager :** `manager@parking.com` / `manager123`
   - **Secrétaire (Admin) :** `admin@parking.com` / `admin123`

---

## 🧪 Stratégie de Test

Le backend comprend une batterie de tests d'intégration complète (se référer à : `backend/src/test/java/com/parking/reservation/ReservationIntegrationTest.java`).
Cette couverture garantit qu'il est physiquement impossible de "surréserver" une place ou de dépasser ses droits en fonction de son rôle. Spring Boot Test couplé à un context local assure la viabilité complète entre le Contrôleur, le Service et la Base de données ([ADR-008](docs/adr/ADR-008-strategie-de-test.md)).

---

*Développé dans le cadre des cours d'Architecture / Heuristique (ESGI).*
