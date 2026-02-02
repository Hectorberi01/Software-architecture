# ADR-002 : Stack technique

## Statut
Accepté

## Date
2026-02-02

## Contexte
Suite à la décision ADR-001 de développer une application web SPA, nous devons choisir les technologies pour le frontend, le backend et les outils associés. Le système doit gérer :
- Une interface utilisateur responsive avec trois profils (employé, secrétaire, manager)
- Une API backend avec logique métier (réservations, check-in, libération automatique de places)
- Une base de données relationnelle (voir ADR-004)
- Une file de messages pour les notifications e-mail (voir ADR-006)
- Le tout conteneurisé (voir ADR-007)

Les critères de choix sont : productivité de développement, maintenabilité, écosystème, typage fort pour réduire les bugs et mieux expliciter les données, et facilité de conteneurisation.

## Décision

### Frontend : React + TypeScript + Vite

| Option | Avantages | Inconvénients |
|---|---|---|
| **React + TypeScript** | Écosystème mature, large communauté, composants réutilisables, typage fort | Boilerplate plus important que Vue |
| Vue.js + TypeScript | Courbe d'apprentissage douce, API composition | Écosystème un peu moins large |
| Angular | Framework complet, très structuré | Complexité excessive pour ce projet |
| Svelte | Performances, syntaxe concise | Écosystème jeune, moins de bibliothèques |

**Choix : React + TypeScript** avec **Vite** comme outil de build.

Justification :
- **TypeScript** apporte la sécurité du typage statique, essentielle pour la fiabilité d'une application métier.
- **React** offre un écosystème très riche de bibliothèques de composants UI (Material UI, Ant Design) adaptées aux dashboards et formulaires dont nous avons besoin.
- **Vite** offre un démarrage instantané en développement et un build optimisé en production, bien supérieur à Create React App (déprécié).

### Backend : Java (Spring Boot)

| Option | Avantages | Inconvénients |
|---|---|---|
| **Java (Spring Boot)** | Structuré, injection de dépendances, très robuste, écosystème entreprise, bonne intégration avec IntelliJ, Docker, RabbitMQ | Plus verbeux, JVM plus lourde en mémoire |
| NestJS (TypeScript) | Même langage que le frontend, léger | Écosystème moins mature en entreprise |
| Express.js | Simple, flexible | Pas de structure imposée, difficile à maintenir |
| FastAPI (Python) | Rapide à prototyper, bonne doc auto | Typage runtime, moins structuré |

**Choix : Spring Boot (Java 21)**

Justification :
- **Langage robuste** : Java est un langage fortement typé, compilé, avec une gestion mémoire mature (JVM). Il offre des garanties de fiabilité essentielles pour une application métier.
- **Architecture structurée** : Spring Boot impose naturellement une organisation en couches (controllers, services, repositories) et facilite l'architecture hexagonale (voir ADR-003) grâce à son injection de dépendances native.
- **Injection de dépendances** : le conteneur IoC de Spring facilite les tests unitaires et le découplage entre composants.
- **Support natif** de RabbitMQ (Spring AMQP), JPA/Hibernate (Spring Data), sécurité (Spring Security), documentation API (springdoc-openapi / Swagger UI), tâches planifiées (`@Scheduled` pour la libération automatique à 11h).
- **Excellente intégration avec IntelliJ IDEA** : debugging, refactoring, auto-complétion, profiling — l'outillage Java est parmi les meilleurs du marché.
- **Performance suffisante** pour notre cas d'usage (~200 employés maximum, charge faible).

### ORM : Spring Data JPA + Hibernate

**Choix : Spring Data JPA** avec **Hibernate** comme implémentation JPA.

Justification :
- Standard JPA : indépendant de l'implémentation, portabilité garantie
- Mapping objet-relationnel transparent avec les annotations (`@Entity`, `@OneToMany`, etc.)
- Génération automatique des requêtes via les conventions de nommage (`findByDateAndStatus(...)`)
- Migrations de schéma gérées par **Flyway** (scripts SQL versionnés, reproductibles)
- Excellente intégration avec MySQL (voir ADR-004) et Spring Boot (auto-configuration)

### Bibliothèques frontend complémentaires
- **React Router** : routage côté client
- **TanStack Query (React Query)** : gestion du cache et des requêtes API
- **Material UI (MUI)** : bibliothèque de composants UI prête à l'emploi, adaptée aux applications métier
- **Recharts** : graphiques pour le dashboard manager
- **html5-qrcode** : scan de QR codes via la caméra du navigateur

## Conséquences

### Positives
- Backend robuste et structuré grâce à Spring Boot et l'écosystème Java
- Typage statique côté backend (Java) et frontend (TypeScript) pour une fiabilité accrue
- Écosystème très riche côté Spring (sécurité, messaging, data, web) et React (composants UI)
- Facilité de recrutement (Java et React sont parmi les technologies les plus demandées)
- Excellents outils de développement (IntelliJ IDEA, Maven, JUnit)

### Négatives
- Deux langages différents (Java backend, TypeScript frontend) : pas de partage de code entre front et back
- La JVM consomme plus de mémoire qu'un runtime Node.js (non critique pour notre usage)
- Verbosité de Java par rapport à TypeScript/Python (atténuée par les records Java 21 et Lombok si nécessaire)

### Risques
- Temps de démarrage de la JVM en développement. Mitigation : utilisation de Spring DevTools pour le rechargement à chaud.
