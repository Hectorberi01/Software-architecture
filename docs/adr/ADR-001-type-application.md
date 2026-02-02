# ADR-001 : Type d'application
Web application SPA (Singl)
## Statut
Accepté

## Date
2026-02-02

## Contexte
Nous devons remplacer le processus actuel de réservation de parking (échanges par e-mail avec les secrétaires + fichier Excel partagé) par une application dédiée. Les utilisateurs cibles sont des employés non techniques, des secrétaires (administration) et des managers. L'application doit être accessible facilement, sans friction, et supportée sur différents types d'appareils (PC de bureau, ordinateurs portables, smartphones).

Les options envisagées sont :
1. **Application mobile native** (iOS/Android)
2. **Application desktop**
3. **Application web (SPA)**
4. **Application web progressive (PWA)**

## Décision
Nous optons pour une **application web de type Single Page Application (SPA)**, avec une conception responsive (mobile-first) permettant une utilisation confortable sur smartphone, tablette et desktop.

## Justification

| Critère |App Mobile Native |App Desktop |SPA Web |PWA |
|--------|---------------------|---------------|------------|------------|
| **Accessibilité immédiate** | Installation requise | Installation requise | URL dans le navigateur | URL + installation optionnelle |
| **Cross-platform** | iOS + Android séparés | Windows / Mac / Linux | Tous navigateurs | Tous navigateurs |
| **Déploiement / Mise à jour** | App Store review | Distribution complexe | Déploiement serveur unique | Déploiement serveur unique |
| **Scan QR Code (check-in)** | Caméra native |Pas de caméra | API Web (caméra) | API Web (caméra) |
| **Coût de développement** |Élevé (2 plateformes) |Modéré |Faible |Faible |
| **Complexité de maintenance** |Élevée |Modérée |Faible |Faible |


La SPA web est le **meilleur compromis** car :
- **Zéro installation** : les employés ouvrent simplement une URL dans leur navigateur.
- **Scan QR Code** : les navigateurs modernes supportent l'accès à la caméra via l'API `navigator.mediaDevices`, suffisant pour scanner un QR code de check-in.
- **Un seul code source** pour tous les appareils.
- **Déploiement simplifié** : une seule mise en production côté serveur suffit, pas de passage par des stores.
- **Adapté aux utilisateurs non techniques** : pas de processus d'installation.

La PWA a été écartée pour cette première version car elle ajoute de la complexité (service workers, cache offline) sans bénéfice majeur pour notre cas d'usage : le parking nécessite une connexion réseau pour vérifier la disponibilité en temps réel. Elle pourra être envisagée dans une évolution future.

## Conséquences

### Positives
- Déploiement unique et centralisé
- Accessibilité universelle via navigateur
- Maintenance simplifiée (un seul codebase frontend)
- Coût de développement réduit

### Négatives
- Pas d'accès offline (acceptable car les réservations nécessitent une connexion)
- Dépendance au navigateur pour le scan QR (fonctionnalité bien supportée aujourd'hui)

### Risques
- Les navigateurs très anciens pourraient ne pas supporter certaines fonctionnalités (scan QR). Mitigation : communiquer les navigateurs supportés (Chrome, Firefox, Safari, Edge versions récentes).
