# ADR-003 : Style d'architecture backend

## Statut
Accepté

## Date
2026-02-02

## Contexte
Le backend de l'application de réservation de parking doit gérer une logique métier spécifique :
- Règles de réservation différentes selon le profil (employé : 5 jours max, manager : 30 jours max)
- Libération automatique des places non confirmées après 11h
- Gestion de l'état des places (disponible, réservée, occupée)
- Envoi de messages à une file d'attente pour les confirmations e-mail
- Historique complet des réservations

Cette logique métier doit rester **lisible, testable et maintenable**, tout en s'intégrant efficacement à l'écosystème technique (HTTP, base de données, messagerie).

L'objectif est de privilégier une architecture **simple et pragmatique**, compréhensible par l'ensemble de l'équipe, sans sur-abstraction.

## Options envisagées

1. **Architecture en couches classique (Layered)** : Controller → Service → Repository
2. Architecture hexagonale (Ports & Adapters)
3. Architecture microservices
4. CQRS + Event Sourcing

## Décision
Nous adoptons une **architecture en couches classique (Layered)** basée sur le pattern :

**Controller → Service → Repository**

au sein d'une **application monolithique modulaire**.

## Justification

### Pourquoi pas les microservices ?
Le système couvre un **domaine unique** (réservation de parking) avec un volume d'utilisateurs limité (~200 utilisateurs).

Une architecture microservices introduirait :
- une complexité opérationnelle élevée (réseau, déploiements multiples, observabilité distribuée),
- des problématiques de cohérence des données,
- un coût de maintenance non justifié par le besoin fonctionnel.

Un monolithe modulaire bien structuré est plus adapté et plus efficace pour ce contexte.

### Pourquoi pas CQRS + Event Sourcing ?
Le domaine est principalement **transactionnel** et relativement simple.

CQRS + Event Sourcing impliqueraient :
- une complexité conceptuelle importante,
- la gestion de projections et d'eventual consistency,
- un outillage plus lourd,

sans bénéfice immédiat pour le projet.

### Pourquoi pas l'architecture hexagonale ?
L'architecture hexagonale (Ports & Adapters) offre un découplage maximal entre domaine et infrastructure, mais pour notre projet :
- le domaine métier est relativement simple,
- l'équipe est plus familière avec le pattern en couches classique de Spring Boot,
- la multiplication des interfaces/ports ajouterait du boilerplate sans valeur ajoutée significative.

L'architecture en couches classique offre un **découplage suffisant** via les interfaces Spring Data et l'injection de dépendances de Spring, tout en restant plus accessible.

### Pourquoi une architecture en couches classique ?
L'architecture **Controller / Service / Repository** offre un excellent compromis entre simplicité, clarté et testabilité :

- Structure largement connue par l'équipe et naturelle dans Spring Boot
- Rapidité de mise en œuvre
- Lisibilité du flux applicatif
- Testabilité suffisante pour le périmètre du projet

Elle permet de concentrer la logique métier dans une couche dédiée (`Service`), tout en gardant une séparation claire des responsabilités.

## Schéma logique

```
        ┌─────────────────────────────┐
        │         Controller          │
        │  - Endpoints HTTP           │
        │  - Validation des entrées   │
        │  - Mapping DTO ↔ domaine    │
        └──────────────┬──────────────┘
                       │ appelle
        ┌──────────────▼──────────────┐
        │          Service            │
        │  - Règles métier            │
        │  - Orchestration            │
        │  - Transactions             │
        │  - Appels asynchrones       │
        └──────────────┬──────────────┘
                       │ utilise
        ┌──────────────▼──────────────┐
        │         Repository          │
        │  - Accès base de données    │
        │  - Requêtes & persistance   │
        └─────────────────────────────┘
```

## Organisation des modules

L’application est découpée en **modules fonctionnels**, chacun respectant la structure en couches :

```

src/
├── parking/
│ ├── controller/
│ ├── service/
│ ├── repository/
│ └── model/
│
├── reservation/
│ ├── controller/
│ ├── service/
│ ├── repository/
│ └── model/
│
├── checkin/
│ ├── controller/
│ ├── service/
│ ├── repository/
│ └── model/
│
├── user/
│ ├── controller/
│ ├── service/
│ ├── repository/
│ └── model/
│
├── notification/
│ ├── service/
│ └── messaging/
│
├── dashboard/
│ ├── controller/
│ └── service/
│
└── shared/
├── dto/
├── guards/
└── utils/

```

## Principes respectés
- **Séparation des responsabilités**
  - `Controller` : exposition HTTP, validation, mapping
  - `Service` : logique métier et orchestration
  - `Repository` : accès aux données
- **Testabilité** : la logique métier est testable via la couche `Service` avec mocks des repositories
- **Lisibilité et cohérence** : structure uniforme dans tous les modules
- **Évolutivité maîtrisée** : possibilité d’introduire plus tard des abstractions supplémentaires si la complexité augmente

```

## Conséquences

### Positives
- Architecture simple et compréhensible par toute l’équipe
- Démarrage rapide et livraison incrémentale facilitée
- Tests unitaires clairs sur la couche métier
- Moins de boilerplate et d’interfaces inutiles

### Négatives
- Couplage plus fort entre la logique métier et l’infrastructure que dans une architecture hexagonale
- Moins de flexibilité immédiate pour changer radicalement d’implémentation technique

### Risques
- Glissement de logique métier dans les controllers  
  **Mitigation** : règles strictes de code review, services obligatoires pour toute logique métier
- Services trop volumineux  
  **Mitigation** : découpage par cas d’usage si la complexité augmente

