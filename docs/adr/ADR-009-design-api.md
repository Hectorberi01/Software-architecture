# ADR-009 : Design de l'API

## Statut
Accepté

## Date
2026-02-02

## Contexte
Le frontend (SPA React) communique avec le backend (Spring Boot) via une API. Nous devons définir le style d'API, les conventions de nommage, le format des réponses et la documentation.

## Options envisagées

| Option | Avantages | Inconvénients |
|---|---|---|
| **REST** | Standard, simple, bien outillé, cacheable | Peut nécessiter plusieurs requêtes (under/over-fetching) |
| GraphQL | Flexibilité des requêtes, un seul endpoint | Complexité, cache difficile, sur-dimensionné ici |
| gRPC | Très performant, typage fort | Pas adapté aux navigateurs directement |

## Décision
Nous adoptons une **API REST** avec documentation **OpenAPI/Swagger** auto-générée via **springdoc-openapi**.

## Justification

- **Simplicité** : REST est le standard le plus répandu pour les API web. Toute l'équipe le maîtrise.
- **Adapté au besoin** : notre API est principalement CRUD (Create, Read, Update, Delete) sur des ressources bien identifiées (réservations, places, utilisateurs). REST excelle dans ce cas.
- **Cache HTTP** : les réponses GET (liste des places, historique) sont cacheables nativement.
- **Swagger/OpenAPI** : springdoc-openapi génère automatiquement la documentation API interactive à partir des annotations Spring (`@Operation`, `@Schema`). Utile pour le développement frontend et les tests.

### Pourquoi pas GraphQL ?
GraphQL est pertinent quand le client a besoin de flexibilité pour composer ses requêtes (ex : application mobile avec bande passante limitée). Notre SPA consomme des données prévisibles et bien définies. GraphQL ajouterait de la complexité sans bénéfice.

## Conventions

### Structure des URLs

```
Base URL : /api/v1

Authentification :
  POST   /api/v1/auth/login          Connexion
  POST   /api/v1/auth/refresh        Renouveler le token
  POST   /api/v1/auth/logout         Déconnexion

Places de parking :
  GET    /api/v1/spots                Liste des places (avec filtres)
  GET    /api/v1/spots/:id            Détail d'une place
  GET    /api/v1/spots/available      Places disponibles (date, période)

Réservations :
  POST   /api/v1/reservations         Créer une réservation
  GET    /api/v1/reservations         Mes réservations (employé) ou toutes (admin)
  GET    /api/v1/reservations/:id     Détail d'une réservation
  DELETE /api/v1/reservations/:id     Annuler une réservation

Check-in :
  POST   /api/v1/checkin              Check-in via QR code

Administration (secrétaires) :
  GET    /api/v1/admin/users          Liste des utilisateurs
  POST   /api/v1/admin/users          Créer un utilisateur
  PUT    /api/v1/admin/users/:id      Modifier un utilisateur
  DELETE /api/v1/admin/users/:id      Désactiver un utilisateur
  GET    /api/v1/admin/reservations   Toutes les réservations (avec filtres)
  PUT    /api/v1/admin/reservations/:id  Modifier une réservation

Dashboard (managers) :
  GET    /api/v1/dashboard/occupancy       Taux d'occupation
  GET    /api/v1/dashboard/no-show-rate    Taux de no-show
  GET    /api/v1/dashboard/charger-usage   Utilisation des bornes électriques
  GET    /api/v1/dashboard/summary         Résumé global
```

### Format des réponses

**Succès** :
```json
{
  "data": { ... },
  "meta": {
    "page": 1,
    "pageSize": 20,
    "total": 42
  }
}
```

**Erreur** :
```json
{
  "statusCode": 400,
  "error": "Bad Request",
  "message": "La date de début doit être aujourd'hui ou dans le futur",
  "details": [
    {
      "field": "startDate",
      "message": "Doit être >= date du jour"
    }
  ]
}
```

### Codes HTTP utilisés

| Code | Utilisation |
|---|---|
| 200 | Succès (GET, PUT) |
| 201 | Création réussie (POST) |
| 204 | Suppression réussie (DELETE) |
| 400 | Requête invalide (validation) |
| 401 | Non authentifié |
| 403 | Non autorisé (rôle insuffisant) |
| 404 | Ressource non trouvée |
| 409 | Conflit (place déjà réservée) |
| 422 | Erreur de validation métier |
| 500 | Erreur serveur |

### Pagination
Les endpoints qui retournent des listes supportent la pagination via query parameters :
- `?page=1&pageSize=20` (valeurs par défaut)
- `?sort=date&order=desc`

### Filtres
Les filtres sont passés en query parameters :
- `GET /api/v1/spots/available?date=2026-02-03&period=AM`
- `GET /api/v1/admin/reservations?userId=xxx&status=CONFIRMED&from=2026-01-01&to=2026-01-31`

### Versioning
L'API est versionnée via le préfixe `/api/v1`. En cas d'évolution majeure, une `/api/v2` pourra coexister temporairement.

## Conséquences

### Positives
- API prévisible et facile à consommer côté frontend
- Documentation interactive auto-générée (Swagger UI accessible sur `/swagger-ui.html`)
- Conventions claires pour toute l'équipe
- Codes d'erreur explicites facilitant le debug

### Négatives
- Potential over-fetching sur certains endpoints (acceptable vu la taille des données)
- Le versioning par URL est simple mais peut mener à de la duplication si on crée v2

### Risques
- Incohérence dans les conventions si les développeurs ne suivent pas les guidelines. Mitigation : revue de code et validation des annotations Swagger.
