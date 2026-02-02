# ADR-005 : Authentification et autorisation

## Statut
Accepté

## Date
2026-02-02

## Contexte
L'application dispose de trois profils utilisateurs avec des droits différents :

| Profil | Droits |
|---|---|
| **Employé** | Voir les places disponibles, réserver (max 5 jours), faire le check-in, voir ses propres réservations |
| **Manager** | Mêmes droits que l'employé + réservation jusqu'à 30 jours + accès au dashboard de statistiques |
| **Secrétaire (Admin)** | Accès complet au back-office : gestion des utilisateurs, modification/annulation de toute réservation, consultation de tout l'historique |

L'authentification doit être sécurisée mais adaptée à un contexte intranet/entreprise. Les utilisateurs ne sont pas techniques.

## Options envisagées

### Authentification

| Option | Avantages | Inconvénients |
|---|---|---|
| **JWT (JSON Web Tokens)** | Stateless, scalable, standard, pas de session serveur | Révocation complexe, taille du token |
| Sessions côté serveur | Révocation simple | Nécessite un store (Redis), pas stateless |
| OAuth2 / OpenID Connect (SSO) | Intégration avec l'annuaire d'entreprise | Complexité d'intégration, dépendance à un IdP |
| Basic Auth | Simple | Pas sécurisé, pas de gestion de session |

### Autorisation

| Option | Avantages | Inconvénients |
|---|---|---|
| **RBAC (Role-Based Access Control)** | Simple, adapté à 3 rôles fixes | Peu flexible pour des permissions fines |
| ABAC (Attribute-Based Access Control) | Très flexible | Sur-dimensionné pour notre besoin |
| ACL (Access Control Lists) | Granulaire | Complexe à maintenir |

## Décision
- **Authentification** : JWT avec access token (courte durée) + refresh token (longue durée)
- **Autorisation** : RBAC avec trois rôles (`EMPLOYEE`, `MANAGER`, `ADMIN`)

## Justification

### JWT
- **Stateless** : pas besoin de stocker les sessions en base. Le serveur valide le token via sa signature.
- **Standard** : largement supporté, bibliothèques matures (`jjwt` ou `spring-security-oauth2-jose` côté Java).
- **Adapté à une SPA** : le frontend stocke le token et l'envoie dans le header `Authorization: Bearer <token>`.
- **Performance** : pas d'accès base de données à chaque requête pour vérifier la session.

### Stratégie de tokens
- **Access token** : durée de vie courte (15 minutes), contient l'ID utilisateur et le rôle.
- **Refresh token** : durée de vie longue (7 jours), stocké en base, permet de renouveler l'access token sans re-saisir les identifiants.
- **Révocation** : en cas de besoin, le refresh token peut être invalidé en base (ex : désactivation d'un compte par une secrétaire).

### RBAC
- Notre modèle ne comporte que 3 rôles avec des permissions clairement définies. RBAC est le modèle le plus simple et adapté.
- Les rôles sont stockés dans le JWT, ce qui permet de vérifier les permissions sans requête supplémentaire.
- Implémentation via **Spring Security** avec des annotations de sécurité :

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/reservations")
public ResponseEntity<List<ReservationDto>> findAllReservations() { ... }
```

### Flux d'authentification

```
┌─────────┐         ┌─────────┐         ┌─────────┐
│ Frontend│         │ Backend │         │  MySQL  │
└────┬────┘         └────┬────┘         └────┬────┘
     │  POST /auth/login  │                   │
     │  {email, password} │                   │
     │───────────────────►│                   │
     │                    │  Vérifier user    │
     │                    │──────────────────►│
     │                    │◄──────────────────│
     │                    │  Générer JWT      │
     │  {accessToken,     │                   │
     │   refreshToken}    │                   │
     │◄───────────────────│                   │
     │                    │                   │
     │  GET /reservations │                   │
     │  Authorization:    │                   │
     │  Bearer <token>    │                   │
     │───────────────────►│                   │
     │                    │  Valider JWT      │
     │                    │  Vérifier rôle    │
     │  200 OK + données  │                   │
     │◄───────────────────│                   │
```

### Pourquoi pas OAuth2/SSO maintenant ?
OAuth2 avec un Identity Provider (Keycloak, Azure AD, etc.) serait l'idéal en production pour s'intégrer à l'annuaire de l'entreprise. Cependant, cela ajoute une dépendance d'infrastructure significative. Notre architecture en couches (ADR-003) et l'utilisation de Spring Security permettent de remplacer la stratégie d'authentification (JWT → OAuth2) sans modifier la logique métier. Nous prévoyons cette évolution pour une version ultérieure.

## Conséquences

### Positives
- Authentification stateless et performante
- Autorisation simple et prévisible avec 3 rôles
- Possibilité de révocation via les refresh tokens
- Évolution vers OAuth2/SSO possible grâce à la modularité de Spring Security

### Négatives
- Les mots de passe sont gérés par notre application (responsabilité sécurité : hashing bcrypt via `BCryptPasswordEncoder`, politique de mot de passe)
- Le JWT n'est pas révocable instantanément (délai = durée de vie de l'access token, 15 min)

### Risques
- Vol de token : mitigation via HTTPS obligatoire, durée de vie courte, rotation des refresh tokens.
- Stockage du token côté client : le frontend stockera les tokens en mémoire (variable JavaScript) et non en localStorage pour éviter les attaques XSS. Le refresh token sera stocké dans un cookie HttpOnly.
