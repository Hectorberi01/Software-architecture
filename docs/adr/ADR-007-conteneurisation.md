# ADR-007 : Conteneurisation et orchestration locale

## Statut
Accepté

## Date
2026-02-02

## Contexte
L'énoncé du projet exige que l'application fonctionne dans des conteneurs pour simplifier :
- Le déploiement
- Les tests
- Les contributions de nouveaux développeurs

L'application se compose de plusieurs services :
- Frontend (SPA React)
- Backend (API Spring Boot)
- Base de données (MySQL)
- Broker de messages (RabbitMQ)

Chaque développeur doit pouvoir lancer l'ensemble de l'environnement en une seule commande.

## Options envisagées

| Option | Avantages | Inconvénients |
|---|---|---|
| **Docker + Docker Compose** | Standard, simple, bien documenté | Pas d'orchestration de production |
| Podman + Podman Compose | Rootless, compatible Docker | Moins répandu, compose moins mature |
| Kubernetes (local via minikube) | Orchestration complète | Complexité excessive pour le dev local |
| Vagrant + VMs | Isolation complète | Lourd, lent, consommation mémoire |

## Décision
Nous utilisons **Docker** pour la conteneurisation et **Docker Compose** pour l'orchestration locale de tous les services.

## Justification

- **Standard de l'industrie** : Docker est l'outil de conteneurisation le plus utilisé. Tous les développeurs y sont familiers.
- **Docker Compose** permet de définir l'ensemble de l'infrastructure dans un seul fichier `docker-compose.yml`, avec les dépendances, réseaux et volumes.
- **Reproductibilité** : chaque développeur obtient exactement le même environnement, indépendamment de son OS.
- **Scripts simples** : `docker compose up` démarre tout. Les scripts `build.sh`, `run.sh`, `test.sh` encapsulent les commandes Docker.

## Architecture des conteneurs

```
docker-compose.yml
├── frontend        (Node image → build React → serve via Nginx)
│   Port: 3000
│
├── backend         (Maven image → build Spring Boot → JRE runtime)
│   Port: 8080
│   Dépend de: db, rabbitmq
│
├── db              (mysql:8)
│   Port: 3306
│   Volume: mysql_data
│
└── rabbitmq        (rabbitmq:3-management-alpine)
    Ports: 5672 (AMQP), 15672 (Management UI)
    Volume: rabbitmq_data
```

### Stratégie de build

| Service | Image de base | Build | Commentaire |
|---|---|---|---|
| Frontend | `node:20-alpine` → `nginx:alpine` | Multi-stage | Stage 1 : build React. Stage 2 : copie du build dans Nginx |
| Backend | `maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jre` | Multi-stage | Stage 1 : build Maven. Stage 2 : JAR dans JRE minimal |
| MySQL | `mysql:8` | Aucun | Image officielle, config par variables d'env |
| RabbitMQ | `rabbitmq:3-management-alpine` | Aucun | Image officielle avec plugin management |

### Multi-stage build pour le frontend
```dockerfile
# Stage 1 : Build
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2 : Serve
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

### Multi-stage build pour le backend
```dockerfile
# Stage 1 : Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2 : Runtime
FROM eclipse-temurin:21-jre
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Avantage : l'image finale ne contient que le JRE et le JAR (~200 Mo), pas le JDK ni Maven.

### Scripts d'automatisation

```
scripts/
├── build.sh      # docker compose build
├── run.sh        # docker compose up -d
├── stop.sh       # docker compose down
├── test.sh       # lance les tests dans les conteneurs
├── logs.sh       # docker compose logs -f
└── reset-db.sh   # supprime le volume et réinitialise la BDD
```

Chaque action répétable est encapsulée dans un script simple, exécutable sans connaître les commandes Docker.

## Conséquences

### Positives
- Environnement de développement identique pour tous
- Démarrage de l'application en une commande (`./scripts/run.sh`)
- Isolation des services (pas de conflit de ports, versions, etc.)
- Facilement déployable sur n'importe quel serveur supportant Docker

### Négatives
- Docker doit être installé sur la machine de chaque développeur
- Consommation de ressources plus élevée qu'une exécution native
- Premier démarrage plus lent (téléchargement des images, compilation Maven)

### Risques
- Volumes Docker peuvent causer des problèmes de permissions sur certains OS. Mitigation : documentation claire et script `reset-db.sh`.
- Performance du filesystem sur macOS (volumes Docker lents). Mitigation : utilisation de volumes nommés plutôt que des bind mounts pour les données persistantes.
