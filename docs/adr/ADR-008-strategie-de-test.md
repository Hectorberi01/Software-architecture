# ADR-008 : Stratégie de test

## Statut
Accepté

## Date
2026-02-02

## Contexte
Le projet exige des tests réels et significatifs. L'architecture en couches (ADR-003) facilite le test de la logique métier en mockant les couches inférieures. Nous devons définir une stratégie de test claire, avec les types de tests, les outils et les conventions.

## Décision
Nous adoptons une stratégie de test en **pyramide** avec trois niveaux, en priorisant les tests unitaires de la couche service (logique métier).

## Pyramide de tests

```
        ╱  E2E  ╲           Peu de tests, coûteux, lents
       ╱─────────╲          Vérifient les parcours utilisateur complets
      ╱Integration╲         Tests moyens, vérifient l'assemblage
     ╱─────────────╲        (API + BDD, controllers + services)
    ╱   Unitaires   ╲       Beaucoup de tests, rapides, isolés
   ╱─────────────────╲      Logique métier pure (services)
  ╱───────────────────╲
```

### Niveau 1 : Tests unitaires (priorité haute)

**Cible** : la couche service (logique métier).

**Exemples de tests significatifs** :
- Un employé ne peut pas réserver plus de 5 jours consécutifs
- Un manager peut réserver jusqu'à 30 jours
- Une place sans check-in à 11h est marquée comme libérée
- Les rangées A et F sont réservées aux véhicules électriques/hybrides
- On ne peut pas réserver une place déjà occupée pour le même créneau
- Une réservation ne peut pas démarrer dans le passé

**Outils** :
- **JUnit 5** : framework de test standard pour Java / Spring Boot
- **Mockito** : mocking des repositories et autres dépendances pour isoler la logique métier
- **AssertJ** : assertions fluides et lisibles

**Convention** : chaque classe `*Service.java` a une classe de test `*ServiceTest.java` associée dans `src/test/java`.

### Niveau 2 : Tests d'intégration (priorité moyenne)

**Cible** : l'assemblage entre couches (controllers → services → base de données).

**Exemples** :
- Un appel `POST /reservations` crée bien une réservation en base
- Un appel `POST /checkin` avec un QR code valide met à jour le statut
- L'authentification JWT rejette un token expiré
- Les endpoints admin sont inaccessibles avec un rôle employé

**Outils** :
- **Spring Boot Test** (`@SpringBootTest`) : chargement du contexte complet
- **MockMvc** : requêtes HTTP simulées sur les controllers sans serveur réel
- **Testcontainers** : conteneur MySQL jetable pour les tests d'intégration, garantit un environnement identique à la production
- **`@DataJpaTest`** : tests ciblés sur la couche repository avec base embarquée

### Niveau 3 : Tests E2E (priorité basse, itération 4)

**Cible** : parcours utilisateur complets via le frontend.

**Exemples** :
- Un employé se connecte, voit les places disponibles, réserve une place, et voit la confirmation
- Une secrétaire annule une réservation depuis le back-office

**Outils** :
- **Playwright** ou **Cypress** : automatisation de navigateur
- Exécution dans Docker pour la reproductibilité

### Tests frontend (React)

**Outils** :
- **Vitest** : compatible Vite, rapide
- **React Testing Library** : tests centrés sur le comportement utilisateur (pas sur l'implémentation)

**Exemples** :
- Le formulaire de réservation affiche une erreur si aucune place n'est sélectionnée
- Le dashboard manager affiche les graphiques avec les données fournies
- Le composant de scan QR demande l'accès à la caméra

## Conventions

| Aspect | Convention |
|---|---|
| Nommage backend | `*Test.java` (unitaire), `*IntegrationTest.java` (intégration) |
| Nommage frontend | `*.spec.ts` (unitaire), `*.e2e.spec.ts` (E2E) |
| Exécution backend | `mvn test` (unitaires), `mvn verify` (intégration) |
| Exécution frontend | `npm run test` (unitaires), `npm run test:e2e` (E2E) |
| CI | Les tests unitaires et d'intégration sont exécutés à chaque push |
| Couverture | Objectif : > 80% sur le code de la couche service |
| Script global | `./scripts/test.sh` lance tous les tests dans les conteneurs |

## Conséquences

### Positives
- La logique métier est testée indépendamment de l'infrastructure grâce à Mockito
- Les tests d'intégration avec Testcontainers garantissent un comportement identique à la production
- Les scripts simplifient l'exécution des tests pour toute l'équipe
- Le mocking des repositories est naturel dans l'architecture en couches

### Négatives
- Maintenir Testcontainers ajoute une dépendance Docker pour les tests d'intégration
- Les tests E2E sont fragiles et lents (à réserver aux parcours critiques)

### Risques
- Tests qui testent l'implémentation plutôt que le comportement. Mitigation : revue de code systématique des tests, focus sur les assertions métier.
