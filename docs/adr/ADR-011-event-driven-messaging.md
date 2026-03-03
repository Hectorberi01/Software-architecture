# ADR-011 : Architecture événementielle avec RabbitMQ

## Statut
Accepté

## Date
2026-03-02

## Contexte
L'ADR-006 a acté l'utilisation de RabbitMQ comme broker de messages pour découpler l'envoi de notifications de la logique de réservation. Le présent ADR documente l'implémentation concrète réalisée dans le backend Spring Boot, en précisant les choix de topologie (exchange, queue, routing key), le format du payload et le pattern d'intégration retenu.

Les exigences fonctionnelles et contraintes techniques sont :
- **Découplage** : la création d'une réservation ne doit pas être bloquée par l'indisponibilité du service d'envoi d'e-mail.
- **Fiabilité de livraison** : les messages de confirmation doivent être durables (persistance sur disque) pour survivre à un redémarrage de RabbitMQ.
- **Extensibilité** : la topologie doit permettre d'ajouter facilement de nouveaux consommateurs (notifications push, SMS, audit log) sans modifier le producteur.
- **Simplicité opérationnelle** : le projet cible environ 200 utilisateurs. La solution doit rester simple à opérer dans Docker Compose.

## Options envisagées

| Option | Avantages | Inconvénients |
|---|---|---|
| **RabbitMQ AMQP (retenu)** | Fiabilité, acknowledgments, persistance, routage flexible, interface d'admin web | Service Docker supplémentaire (~128 Mo RAM) |
| Appels HTTP synchrones | Simplicité maximale | Couplage fort, indisponibilité email = réservation bloquée |
| Spring Events (`ApplicationEventPublisher`) | Zéro infrastructure, in-process | Pas de garantie de livraison, in-process uniquement (pas d'extensibilité inter-services) |
| Apache Kafka | Très haute performance, log distribué, replay | Sur-dimensionné pour ~200 utilisateurs, complexité opérationnelle (KRaft, partitions, offsets) |
| Amazon SQS / Google Pub/Sub | Managé, zéro maintenance | Dépendance cloud, coût, pas de version locale simple pour le développement |

## Décision
Nous utilisons RabbitMQ avec une topologie **TOPIC exchange** nommée `parking.notifications`, une queue durable `email.send` et une routing key `spots.listed`. Le backend publie les événements en mode **fire-and-forget** (pas d'attente d'acquittement du consommateur) pour garantir la non-régression du chemin critique de réservation.

### Configuration Spring AMQP (`RabbitMQConfig.java`)

```java
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "parking.notifications";
    public static final String QUEUE_NAME    = "email.send";
    public static final String ROUTING_KEY  = "spots.listed";

    @Bean
    public TopicExchange parkingExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding binding(Queue emailQueue, TopicExchange parkingExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(parkingExchange)
                .with(ROUTING_KEY);
    }
}
```

### Justification de la topologie TOPIC exchange
Un **TOPIC exchange** a été préféré à un DIRECT exchange car il permet un routage par motif de routing key (`parking.reservation.*`, `parking.admin.*`). Cela rend l'ajout de nouveaux types d'événements non destructif : les consommateurs existants ne reçoivent que les messages dont la routing key correspond à leur binding pattern, sans modification du producteur.

## Architecture des messages

### Pattern d'intégration : fire-and-forget

```
┌──────────────────┐   AMQP publish    ┌───────────────────────┐
│  Backend API     │──────────────────►│  RabbitMQ             │
│  (Producteur)    │  parking.notif.   │  Exchange:            │
│                  │  email.send       │  parking.notifications│
│  POST /reserv.   │  routing key:     │  (TOPIC)              │
│  → Save DB       │  spots.listed     │                       │
│  → Publish Event │                   │  Queue: email.send    │
│  → Return 201    │                   │  (durable)            │
└──────────────────┘                   └───────────┬───────────┘
                                                   │ AMQP consume
                                                   ▼
                                       ┌───────────────────────┐
                                       │  Service E-mail       │
                                       │  (Consommateur futur) │
                                       │  Lit message + ACK    │
                                       │  Envoie e-mail SMTP   │
                                       └───────────────────────┘
```

### Format du payload `ReservationCreatedEvent`

```json
{
  "eventType": "RESERVATION_CONFIRMED",
  "timestamp": "2026-03-02T10:30:00Z",
  "payload": {
    "reservationId": "550e8400-e29b-41d4-a716-446655440000",
    "userEmail": "jean.dupont@company.com",
    "userName": "Jean Dupont",
    "spotCode": "B05",
    "reservationDate": "2026-03-03",
    "period": "FULL_DAY",
    "hasCharger": false
  }
}
```

### Types d'événements publiés

| `eventType` | Déclencheur |
|---|---|
| `RESERVATION_CONFIRMED` | Nouvelle réservation confirmée avec succès |
| `RESERVATION_CANCELLED` | Réservation annulée par l'utilisateur ou un administrateur |
| `RESERVATION_NO_SHOW` | Place libérée automatiquement par le scheduler à 11h (aucun check-in) |

### Cycle de vie d'un message

```
Backend publie → Exchange route → Queue durable (persisté sur disque)
     │                                      │
     └── Retourne 201 Created               └── Consommateur lit
         (indépendamment du                     Envoie e-mail
          traitement email)                     Envoie ACK → message supprimé de la queue
```

Les messages non acquittés (consommateur en panne) restent dans la queue jusqu'à reconnexion. Un TTL de 24 heures est recommandé en production pour éviter l'accumulation.

## Conséquences

### Positives
- **Découplage complet** : la logique de réservation est imperméable aux pannes du service email. Le chemin critique POST `/api/v1/reservations` → MySQL n'est pas impacté.
- **Résilience** : la durabilité de la queue garantit qu'aucun événement n'est perdu si le consommateur est temporairement indisponible (redémarrage, déploiement).
- **Extensibilité** : le TOPIC exchange permet d'ajouter de nouveaux consommateurs (notifications push, journalisation d'audit, analytics temps réel) par simple ajout d'un binding, sans modifier le producteur ni redéployer le backend.
- **Monitoring intégré** : l'interface web RabbitMQ Management (port 15672) offre une visibilité sur les queues, les taux de publication/consommation et les messages en attente.
- **Interopérabilité** : le protocole AMQP est indépendant du langage. Le consommateur d'e-mails peut être implémenté dans n'importe quelle technologie (Python, Node.js, Go).

### Négatives
- **Infrastructure supplémentaire** : un conteneur Docker de plus à déployer et surveiller, consommant environ 128 Mo de RAM.
- **Ordering non garanti** : RabbitMQ ne garantit pas l'ordre de livraison entre plusieurs consommateurs concurrents sur la même queue. Non critique pour les notifications email, mais à considérer pour des cas d'usage futurs.
- **Complexité de débogage** : les erreurs de messagerie sont asynchrones. Un message mal formé peut aboutir dans une Dead Letter Queue (DLQ) sans feedback immédiat au producteur.

### Risques
- **Accumulation de messages** : si le consommateur est en panne prolongée, la queue peut grossir indéfiniment. Mitigation : configurer un TTL sur les messages (`x-message-ttl`) et une Dead Letter Queue, avec alertes de monitoring sur le nombre de messages en attente.
- **Perte de messages en cas d'arrêt brutal sans flush disque** : mitigation via `QueueBuilder.durable()` et `MessageProperties.PERSISTENT_TEXT_PLAIN` sur les messages publiés.

## Alternatives rejetées

### Appels HTTP synchrones
La solution la plus simple, mais elle crée un couplage fort entre la réservation et le service email. Une indisponibilité du service email provoquerait des erreurs 500 lors des réservations, ce qui est inacceptable. Rejeté.

### Spring Events (`ApplicationEventPublisher`)
Les événements Spring sont in-process (même JVM). Ils ne survivent pas à un redémarrage et ne permettent pas l'extensibilité inter-services. Utile pour découpler des composants internes, insuffisant pour une architecture orientée services. Rejeté.

### Apache Kafka
Kafka est optimisé pour le streaming de données à très haute volumétrie (millions d'événements/seconde). Notre système cible environ 200 utilisateurs, soit quelques dizaines de réservations par jour. La complexité opérationnelle de Kafka (gestion des partitions, offsets, KRaft ou Zookeeper) est totalement injustifiée. Rejeté comme surdimensionné.
