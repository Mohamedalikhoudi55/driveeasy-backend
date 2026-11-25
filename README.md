# DriveEasy — Architecture Microservices (Spring Boot / Spring Cloud)

Plateforme de gestion de location de véhicules développée avec Spring Boot et Spring Cloud.

## Architecture & Services

| Service | Port | Rôle |
| :--- | :--- | :--- |
| **service-registry** (Eureka) | `8761` | Annuaire et découverte dynamique des microservices |
| **config-server** | `8888` | Gestion centralisée des configurations |
| **api-gateway** | `8085` | Point d'entrée unique et routage (`/api/**`) |
| **vehicule-service** | `8081` | Gestion du parc automobile, disponibilités et statuts |
| **client-service** | `8082` | Gestion des clients et vérification d'éligibilité (`/eligibilite`) |
| **location-service** | `8083` | Réservations, tarification, départs/retours, pénalités et statistiques |
| **Zipkin** | `9411` | Traçage distribué des appels inter-services |

## Ordre de démarrage

1. Lancer **Zipkin** (`java -jar zipkin.jar` sur le port `9411`)
2. Démarrer `service-registry` (`8761`)
3. Démarrer `config-server` (`8888`)
4. Démarrer `vehicule-service` (`8081`), `client-service` (`8082`) et `location-service` (`8083`)
5. Démarrer `api-gateway` (`8085`)

## Endpoints principaux (via API Gateway `http://localhost:8085`)

* **Véhicules :** `GET / POST /api/vehicules`, `GET /api/vehicules/{id}`
* **Clients :** `GET / POST /api/clients`, `GET /api/clients/{id}/eligibilite`
* **Locations :**
  * `POST /api/locations/simulation` — Calcul estimatif du tarif
  * `POST /api/locations` — Création d'une réservation (vérification éligibilité + disponibilité)
  * `GET /api/locations/{id}` — Détail enrichi (location + client + véhicule)
  * `PATCH /api/locations/{id}/depart?kmDepart=...` — Passage au statut `EN_COURS` (`LOUE`)
  * `PATCH /api/locations/{id}/retour?kmRetour=...&dateRetourEffectif=...` — Clôture (`TERMINEE`) et calcul des pénalités de retard
  * `PATCH /api/locations/{id}/annulation` — Annulation d'une réservation
  * `GET /api/locations/statistiques` — Chiffre d'affaires, taux d'occupation et véhicules les plus loués
