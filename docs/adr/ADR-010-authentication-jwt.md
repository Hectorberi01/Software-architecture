# ADR-010 : Implémentation JWT avec Spring Security 6

## Statut
Accepté

## Date
2026-03-02

## Contexte
L'application dispose de trois profils utilisateurs (`EMPLOYEE`, `MANAGER`, `ADMIN`) aux droits distincts, définis dans l'ADR-005. La décision d'utiliser JWT pour l'authentification et RBAC pour l'autorisation a été actée. Le présent ADR documente l'implémentation concrète retenue dans le backend Spring Boot 3.2.2 / Java 21.

Les contraintes techniques majeures sont :
- **Stateless** : le backend ne doit conserver aucune session serveur pour rester scalable et conforme à l'architecture REST (ADR-009).
- **Spring Security 6** : rompt avec la configuration héritée de `WebSecurityConfigurerAdapter` (supprimé) ; la chaîne de filtres doit être déclarée via un bean `SecurityFilterChain`.
- **Bibliothèque JWT** : `jjwt 0.12.3` (io.jsonwebtoken) est déjà intégrée dans le `pom.xml` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`).
- **Mots de passe** : hachage avec `BCryptPasswordEncoder` (algorithme adaptatif, résistant aux attaques par force brute).

## Options envisagées

| Option | Avantages | Inconvénients |
|---|---|---|
| **JWT stateless (retenu)** | Pas de session serveur, scalable, standard IETF RFC 7519, bibliothèques matures | Révocation d'access token complexe sans blacklist |
| Sessions côté serveur | Révocation immédiate, simple à implémenter | Nécessite un store distribué (Redis), casse le stateless |
| OAuth2 / OpenID Connect | Intégration annuaire entreprise, SSO | Dépendance à un IdP externe (Keycloak, Azure AD), surcoût d'infrastructure |
| Basic Auth | Extrêmement simple | Pas de session, credentials envoyés à chaque requête, pas adapté à une SPA |

## Décision
Nous implémentons l'authentification JWT via une chaîne de filtres Spring Security 6 personnalisée, articulée autour de trois composants principaux : `JwtService`, `JwtAuthenticationFilter` et `SecurityConfig`.

### Composants d'implémentation

| Composant | Rôle |
|---|---|
| `JwtService` | Génération, signature (HS256 + clé secrète 256 bits), validation et extraction des claims du JWT |
| `JwtAuthenticationFilter` | Filtre `OncePerRequestFilter` interceptant chaque requête HTTP pour extraire et valider le Bearer token |
| `SecurityConfig` | Bean `SecurityFilterChain` : désactivation CSRF (SPA stateless), politique STATELESS, règles d'autorisation par rôle, enregistrement du filtre JWT |
| `UserDetailsServiceImpl` | Implémentation `UserDetailsService` chargeant l'utilisateur depuis MySQL par email (`UserRepository`) |
| `User` (model) | Entité JPA implémentant `UserDetails` ; `getAuthorities()` retourne `ROLE_EMPLOYEE`, `ROLE_MANAGER` ou `ROLE_ADMIN` |
| `BCryptPasswordEncoder` | Bean Spring de hachage des mots de passe, utilisé lors de la création de compte et de la validation des credentials |

## Architecture de sécurité

### Chaîne de filtres

```
Requête HTTP entrante
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Security Filter Chain              │
│                                                             │
│  1. JwtAuthenticationFilter (OncePerRequestFilter)          │
│     ├── Extraire header: Authorization: Bearer <token>      │
│     ├── JwtService.validateToken(token)                     │
│     │     ├── Vérifier signature HMAC-SHA256                │
│     │     └── Vérifier expiration (exp claim)               │
│     ├── JwtService.extractUsername(token) → email           │
│     ├── UserDetailsService.loadUserByUsername(email)        │
│     └── Injecter UsernamePasswordAuthenticationToken        │
│         dans SecurityContextHolder                          │
│                                                             │
│  2. AuthorizationFilter                                     │
│     └── Vérifier @PreAuthorize / hasRole() sur le handler   │
└─────────────────────────────────────────────────────────────┘
       │
       ▼
  Route Handler (@RestController)
```

### Cycle de vie du token

| Token | Durée de vie | Contenu des claims |
|---|---|---|
| Access token | 15 minutes | `sub` (email), `role` (EMPLOYEE / MANAGER / ADMIN), `iat`, `exp` |
| Refresh token | 7 jours | `sub` (email), `iat`, `exp` ; stocké en base pour permettre la révocation |

### Règles d'autorisation par rôle

```java
// Extrait de SecurityConfig.java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/auth/**").permitAll()
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/dashboard/**").hasAnyRole("MANAGER", "ADMIN")
    .anyRequest().authenticated()
);
```

### Flux d'authentification complet

```
┌─────────┐         ┌─────────┐         ┌─────────┐
│ Frontend│         │ Backend │         │  MySQL  │
└────┬────┘         └────┬────┘         └────┬────┘
     │  POST /api/v1/auth/login              │
     │  { "email": "...", "password": "..." }│
     │──────────────────►│                   │
     │                   │  SELECT user      │
     │                   │  WHERE email=?    │
     │                   │──────────────────►│
     │                   │◄──────────────────│
     │                   │  BCrypt.verify()  │
     │                   │  JwtService.      │
     │                   │  generateToken()  │
     │  200 OK            │                  │
     │  { "accessToken":  │                  │
     │    "refreshToken"} │                  │
     │◄──────────────────│                   │
     │                   │                   │
     │  GET /api/v1/reservations             │
     │  Authorization: Bearer <accessToken>  │
     │──────────────────►│                   │
     │                   │  JwtAuthFilter    │
     │                   │  validateToken()  │
     │                   │  inject Security  │
     │                   │  Context          │
     │  200 OK + données │                   │
     │◄──────────────────│                   │
```

## Conséquences

### Positives
- **Scalabilité** : aucune session serveur, chaque nœud peut valider un token de façon autonome.
- **Performance** : la validation JWT se fait en mémoire (cryptographie symétrique HS256) sans requête base de données supplémentaire.
- **RBAC intégré** : le rôle est embarqué dans le token, les vérifications `hasRole()` sont sans I/O.
- **Standard ouvert** : jjwt implémente RFC 7519 ; migration vers une autre bibliothèque (nimbus-jose-jwt, spring-security-oauth2-jose) facilitée.
- **Évolutivité** : la modularité de Spring Security permet de basculer vers OAuth2/OIDC sans modifier la logique métier (ADR-005).

### Négatives
- **Pas de révocation immédiate de l'access token** : un token volé reste valide pendant 15 minutes maximum. Mitigation : durée de vie courte + HTTPS obligatoire.
- **Gestion des refresh tokens en base** : introduit une dépendance en base pour la révocation lors de la déconnexion ou désactivation de compte.
- **Rotation des secrets** : la rotation de la clé secrète de signature invalide tous les tokens en cours, nécessitant une reconnexion globale.

### Risques
- **Vol de token** : HTTPS obligatoire sur tous les environnements. Le frontend stocke l'access token en mémoire JavaScript (pas en `localStorage`) pour prévenir les attaques XSS. Le refresh token est stocké dans un cookie `HttpOnly; Secure; SameSite=Strict`.
- **Expiration simultanée** : avec une durée de 15 minutes, la UX peut être dégradée. Mitigation : le frontend gère le renouvellement silencieux via le refresh token avant expiration.

## Alternatives rejetées

### Sessions côté serveur
Une session serveur stockée dans un conteneur partagé (Redis) aurait permis une révocation immédiate. Cependant, cette approche casse le principe stateless de notre API REST, introduit un point de défaillance supplémentaire (Redis), et complexifie le déploiement Docker Compose. Écarté pour la version initiale.

### OAuth2 / SSO (feuille de route)
L'intégration d'un Identity Provider (Keycloak, Azure Active Directory) serait la solution idéale pour un environnement d'entreprise avec un annuaire existant. Elle a été délibérément reportée à une version ultérieure car elle introduit une infrastructure supplémentaire significative et sort du cadre de ce projet. L'architecture Spring Security actuelle est conçue pour faciliter cette migration future (remplacement du `JwtAuthenticationFilter` par un filtre OAuth2 Resource Server).
