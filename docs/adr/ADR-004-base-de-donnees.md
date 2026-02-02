# ADR-004 : Base de données

## Statut
Accepté

## Date
2026-02-02

## Contexte
L'application de réservation de parking manipule des données fortement structurées et relationnelles :
- **Utilisateurs** avec des rôles (employé, secrétaire, manager)
- **Places de parking** avec des caractéristiques fixes (numéro, rangée, prise électrique)
- **Réservations** liées à un utilisateur et une place, avec des créneaux temporels (demi-journées)
- **Check-ins** liés à une réservation
- **Historique** complet de toutes les réservations passées

Les données sont transactionnelles : une réservation doit être atomique (on ne peut pas réserver une place déjà prise). L'intégrité référentielle est essentielle.

## Options envisagées

| Option | Type | Avantages | Inconvénients |
|---|---|---|---|
| **MySQL** | Relationnel | ACID, performant, très populaire, open source, excellent écosystème | Moins de fonctionnalités avancées que PostgreSQL (pas de contraintes d'exclusion) |
| PostgreSQL | Relationnel | Très riche en fonctionnalités, types avancés | Plus lourd, fonctionnalités avancées non nécessaires ici |
| SQLite | Relationnel embarqué | Aucune infra, simple | Pas adapté aux accès concurrents, pas de réseau |
| MongoDB | Document (NoSQL) | Flexible, scalable | Pas de transactions multi-documents natives, schéma non garanti |

## Décision
Nous choisissons **MySQL** comme système de gestion de base de données.

## Justification

- **Données relationnelles** : notre modèle est intrinsèquement relationnel (utilisateur ↔ réservation ↔ place). MySQL gère nativement les contraintes d'intégrité référentielle, les clés étrangères et les index.
- **Transactions ACID** : avec le moteur InnoDB (par défaut), MySQL garantit les propriétés ACID, essentielles pour qu'une place ne soit pas réservée deux fois simultanément.
- **Performance** : MySQL est reconnu pour ses excellentes performances en lecture, ce qui correspond bien à notre cas d'usage (consultation fréquente des disponibilités, dashboard de statistiques).
- **Popularité et maturité** : MySQL est le SGBD open source le plus utilisé au monde. Documentation abondante, communauté très active, problèmes bien connus et résolus.
- **Excellente intégration avec Spring Boot** : Spring Data JPA et Hibernate (voir ADR-002) offrent un support natif et éprouvé de MySQL. Le driver `mysql-connector-java` est stable et performant.
- **Conteneurisation** : image Docker officielle légère et bien maintenue (`mysql:8`).
- **Outils d'administration** : MySQL Workbench, phpMyAdmin, DBeaver — large choix d'outils graphiques pour les secrétaires si besoin.
- **Open source** : pas de coût de licence (édition Community), communauté active.
- **Compatibilité IntelliJ** : intégration native dans IntelliJ IDEA (datasource, requêtes, auto-complétion SQL), ce qui s'aligne avec le choix de Spring Boot et l'environnement de développement Java.

### Pourquoi pas PostgreSQL ?
PostgreSQL offre des fonctionnalités plus avancées (contraintes d'exclusion, types JSONB, index partiels), mais notre domaine métier ne les nécessite pas. MySQL est plus simple à opérer, mieux connu de l'équipe, et son intégration avec l'écosystème Java/Spring est éprouvée depuis des années.

### Pourquoi pas MongoDB ?
Les données sont structurées et relationnelles. MongoDB n'apporte aucun avantage ici et introduit des risques d'incohérence (pas de contraintes d'intégrité native entre collections).

### Pourquoi pas SQLite ?
SQLite ne supporte pas les accès concurrents en écriture, ce qui est problématique pour une application multi-utilisateurs. De plus, il n'est pas adapté à un déploiement conteneurisé (pas de serveur réseau).

## Modèle de données simplifié

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│    User      │     │   Reservation    │     │  ParkingSpot     │
├──────────────┤     ├──────────────────┤     ├──────────────────┤
│ id (BIGINT)  │◄────│ userId (FK)      │     │ id (PK)          │
│ email        │     │ spotId (FK)      │────►│ code (ex: A01)   │
│ firstName    │     │ date             │     │ row (A-F)        │
│ lastName     │     │ period (AM/PM/FD)│     │ number (1-10)    │
│ role (enum)  │     │ status (enum)    │     │ hasCharger (bool)│
│ vehicleType  │     │ checkedInAt      │     └──────────────────┘
│ createdAt    │     │ createdAt        │
└──────────────┘     │ cancelledAt      │
                     └──────────────────┘
```

**Statuts d'une réservation** : `CONFIRMED`, `CHECKED_IN`, `RELEASED`, `CANCELLED`, `EXPIRED`

**Note** : pour empêcher les doublons de réservation (même place, même date, même période), nous utiliserons une contrainte `UNIQUE` composite sur `(spotId, date, period)` avec un filtre applicatif sur les statuts actifs. La vérification de non-chevauchement sera gérée côté applicatif dans une transaction avec verrouillage (`SELECT ... FOR UPDATE`).

## Conséquences

### Positives
- Intégrité des données garantie par le SGBD
- Requêtes complexes possibles (statistiques dashboard, historique, rapports)
- Écosystème d'outils mature (MySQL Workbench, mysqldump, réplication)
- Excellente intégration avec Spring Data JPA / Hibernate

### Négatives
- Nécessite un serveur dédié (conteneur Docker)
- Moins de fonctionnalités avancées que PostgreSQL (non bloquant pour notre besoin)

### Risques
- Performance sous forte charge : non pertinent pour ~200 utilisateurs. Mitigation : index sur les colonnes fréquemment requêtées (date, spotId, userId, status).
- Licencing Oracle : la version Community est GPL, ce qui est suffisant pour un usage interne. Mitigation : rester sur MySQL Community Edition.
