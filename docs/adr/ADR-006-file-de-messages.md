# ADR-006 : File de messages pour les notifications

## Statut
Accepté

## Date
2026-02-02

## Contexte
Lorsqu'un employé effectue une réservation, un message doit être envoyé à une file d'attente pour être traité par une application externe qui enverra un e-mail de confirmation. Ce découplage est une exigence du cahier des charges.

L'envoi d'e-mails ne doit pas bloquer le processus de réservation : si le service d'e-mail est temporairement indisponible, la réservation doit quand même être confirmée.

## Options envisagées

| Option | Avantages | Inconvénients |
|---|---|---|
| **RabbitMQ** | Protocole AMQP standard, routage flexible, fiable, interface d'admin web | Plus lourd que Redis |
| Apache Kafka | Très haute performance, log distribué | Sur-dimensionné, complexité opérationnelle |
| Redis (Pub/Sub ou Streams) | Léger, polyvalent (cache + queue) | Pas de garantie de livraison native avec Pub/Sub |
| Amazon SQS / Google Pub/Sub | Managé, scalable | Dépendance cloud, coût, pas de version locale simple |
| Bull (Redis-based) | Intégration NestJS native | Dépendance Redis, moins standard qu'AMQP |

## Décision
Nous choisissons **RabbitMQ** comme broker de messages.

## Justification

- **Fiabilité** : RabbitMQ garantit la livraison des messages (acknowledgments, persistance sur disque). Si le consommateur (service d'e-mail) est temporairement indisponible, les messages sont conservés dans la queue.
- **Protocole standard AMQP** : interopérable avec n'importe quelle technologie. L'application externe qui envoie les e-mails peut être écrite dans n'importe quel langage.
- **Routage flexible** : exchanges (direct, topic, fanout) permettent d'ajouter facilement de nouveaux types de notifications (ex : notification push, SMS) sans modifier le producteur.
- **Interface d'administration web** : le plugin Management permet de surveiller les queues, les messages en attente, les taux de traitement. Utile pour le monitoring et le debug.
- **Conteneurisation simple** : image Docker officielle, configuration par variables d'environnement.
- **Intégration NestJS** : le module `@nestjs/microservices` supporte nativement RabbitMQ.

### Pourquoi pas Kafka ?
Kafka est conçu pour le streaming de données à haute volumétrie. Notre besoin est de l'ordre de quelques dizaines de messages par jour (réservations). Kafka introduit une complexité opérationnelle (Zookeeper/KRaft, partitions, offsets) injustifiée.

### Pourquoi pas Redis seul ?
Redis Pub/Sub ne persiste pas les messages : si le consommateur est déconnecté au moment de la publication, le message est perdu. Redis Streams corrige cela mais reste moins riche que RabbitMQ en routage et en fonctionnalités de queuing.

## Architecture de messaging

```
┌──────────────┐     ┌──────────────┐     ┌──────────────────────┐
│   Backend    │     │   RabbitMQ   │     │  Service E-mail      │
│  (Producer)  │     │   (Broker)   │     │  (Consumer externe)  │
├──────────────┤     ├──────────────┤     ├──────────────────────┤
│ Réservation  │────►│ Exchange:    │────►│ Lit les messages      │
│ confirmée    │     │ parking.notif│     │ Envoie l'e-mail       │
│              │     │              │     │ Acknowledge           │
│ Réservation  │────►│ Queue:       │     │                      │
│ annulée      │     │ email.send   │     │                      │
└──────────────┘     └──────────────┘     └──────────────────────┘
```

### Format du message

```json
{
  "type": "RESERVATION_CONFIRMED",
  "timestamp": "2026-02-02T10:30:00Z",
  "payload": {
    "reservationId": "uuid",
    "userEmail": "jean.dupont@company.com",
    "userName": "Jean Dupont",
    "spotCode": "B05",
    "date": "2026-02-03",
    "period": "FULL_DAY"
  }
}
```

### Types d'événements publiés
- `RESERVATION_CONFIRMED` : nouvelle réservation confirmée
- `RESERVATION_CANCELLED` : réservation annulée par l'utilisateur ou l'admin
- `RESERVATION_RELEASED` : place libérée automatiquement (pas de check-in avant 11h)

## Conséquences

### Positives
- Découplage complet entre la réservation et l'envoi d'e-mail
- Résilience : les messages sont conservés si le consommateur est indisponible
- Extensibilité : ajout facile de nouveaux consommateurs (notifications push, logs, etc.)
- Monitoring intégré via l'interface web RabbitMQ

### Négatives
- Service supplémentaire à déployer et maintenir (conteneur Docker)
- Consommation mémoire supplémentaire (~128 Mo)

### Risques
- Queue qui grossit indéfiniment si le consommateur est en panne prolongée. Mitigation : configurer un TTL (Time To Live) sur les messages et des alertes de monitoring.
