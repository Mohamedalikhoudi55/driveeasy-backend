# DriveEasy — Back-End Microservices (Spring Boot / Spring Cloud)

Plateforme de gestion de location de véhicules développée selon une architecture microservices avec découverte dynamique de services, configuration centralisée, passerelle API et traçage distribué.

---

## 1. Description du projet

**DriveEasy** est une application back-end modulaire permettant de gérer l'ensemble du cycle de vie d'une agence de location de véhicules :

* **Gestion du parc automobile (`vehicule-service`) :** ajout, consultation, filtrage et mise à jour automatique des statuts des véhicules (`DISPONIBLE`, `LOUE`, `EN_MAINTENANCE`).
* **Gestion des clients (`client-service`) :** enregistrement des profils clients et vérification métier de l'éligibilité à la location (âge >= 21 ans et permis de conduire en cours de validité via `/api/clients/{id}/eligibilite`).
* **Gestion des locations (`location-service`) :** simulation tarifaire, contrôle de disponibilité sans conflit de dates, création de réservation (`RESERVEE`), enregistrement du départ (`EN_COURS`), clôture au retour (`TERMINEE`) avec calcul automatique des pénalités de retard, annulation (`ANNULEE`), pagination et calcul de statistiques (chiffre d'affaires, taux d'occupation).
* **Infrastructure Spring Cloud :** découverte des services via **Eureka**, configuration externalisée via **Config Server**, point d'entrée unique via **API Gateway** et traçage distribué des appels inter-services via **Zipkin**.

---

## 2. Prérequis

* **Java JDK :** version 17 ou supérieure
* **Apache Maven :** version 3.8+ (ou wrapper Maven `mvnw.cmd` sous Windows / `./mvnw` sous Linux/macOS inclus dans chaque projet)
* **Serveur Zipkin :** exécutable `zipkin-server-*.jar` (port `9411`)
* **Outil de test API :** Postman ou cURL
* **Git**

---

## 3. Architecture & Ports utilisés

| Service / Composant | Nom Spring | Port | URL locale | Rôle |
| :--- | :--- | :--- | :--- | :--- |
| **Service Registry (Eureka)** | `service-registry` | `8761` | `http://localhost:8761` | Annuaire et découverte dynamique des services |
| **Config Server** | `config-server` | `8888` | `http://localhost:8888` | Gestion centralisée des configurations |
| **API Gateway** | `api-gateway` | `8085` | `http://localhost:8085` | Point d'entrée unique et routage (`/api/**`) |
| **Microservice Véhicules** | `vehicule-service` | `8081` | `http://localhost:8081` | Gestion du parc et des statuts |
| **Microservice Clients** | `client-service` | `8082` | `http://localhost:8082` | Gestion des clients et de leur éligibilité |
| **Microservice Locations** | `location-service` | `8083` | `http://localhost:8083` | Réservations, cycle de vie et statistiques |
| **Zipkin (Tracing)** | `zipkin` | `9411` | `http://localhost:9411` | Traçage distribué des requêtes inter-services |

---

## 4. Instructions d'installation et de lancement pas à pas

### Étape 1 : Cloner le dépôt
```bash
git clone [https://github.com/Mohamedalikhoudi55/driveeasy-backend.git](https://github.com/Mohamedalikhoudi55/driveeasy-backend.git)
cd driveeasy-backend
```

### Étape 2 : Démarrer Zipkin (traçage distribué)
Dans un premier terminal, lancer le serveur Zipkin :
```bash
java -jar zipkin-server-3.4.0-exec.jar
```
Vérifier l'accès à l'interface sur `http://localhost:9411`.

### Étape 3 : Démarrer les microservices dans l'ordre strict
Ouvrir un terminal distinct pour chaque service et respecter l'ordre suivant (sous Windows, utiliser `.\mvnw.cmd spring-boot:run`) :

1. **Service Registry (Eureka — Port 8761) :**
   ```bash
   cd service-registry
   ./mvnw spring-boot:run
   ```
   *Attendre le démarrage complet et vérifier `http://localhost:8761`.*

2. **Config Server (Port 8888) :**
   ```bash
   cd config-server
   ./mvnw spring-boot:run
   ```

3. **Microservices métier (Ports 8081, 8082, 8083) :**
   ```bash
   # Terminal 3
   cd vehicule-service && ./mvnw spring-boot:run

   # Terminal 4
   cd client-service && ./mvnw spring-boot:run

   # Terminal 5
   cd location-service && ./mvnw spring-boot:run
   ```

4. **API Gateway (Port 8085) :**
   ```bash
   cd api-gateway
   ./mvnw spring-boot:run
   ```
   *Vérifier sur `http://localhost:8761` que les 4 instances (`API-GATEWAY`,`VEHICULE-SERVICE`, `CLIENT-SERVICE`, `LOCATION-SERVICE`) affichent le statut `UP`.*

---

## 5. Catalogue des Endpoints REST (via API Gateway `http://localhost:8085`)

### Véhicules (`vehicule-service`)
* `POST /api/vehicules` — Ajouter un véhicule
* `GET /api/vehicules` — Lister tous les véhicules
* `GET /api/vehicules/{id}` — Consulter un véhicule par ID
* `PUT /api/vehicules/{id}` — Modifier un véhicule
* `DELETE /api/vehicules/{id}` — Supprimer un véhicule

### Clients (`client-service`)
* `POST /api/clients` — Enregistrer un client
* `GET /api/clients` — Lister tous les clients
* `GET /api/clients/{id}` — Consulter un client par ID
* `GET /api/clients/{id}/eligibilite` — Vérifier l'éligibilité du client (âge >= 21 ans et permis valide)

### Locations (`location-service`)
* `POST /api/locations/simulation` — Simuler le coût d'une location
* `POST /api/locations` — Créer une réservation (`RESERVEE`)
* `GET /api/locations/{id}` — Obtenir le détail enrichi (Location + Client + Véhicule)
* `GET /api/locations?statut=...&page=0&size=5` — Lister les locations avec pagination et filtre
* `PATCH /api/locations/{id}/depart?kmDepart=...` — Enregistrer le départ (`EN_COURS`, véhicule `LOUE`)
* `PATCH /api/locations/{id}/retour?kmRetour=...&dateRetourEffectif=...` — Clôturer la location (`TERMINEE`, véhicule `DISPONIBLE`) et calculer les pénalités de retard
* `PATCH /api/locations/{id}/annulation` — Annuler une réservation (`ANNULEE`)
* `GET /api/locations/statistiques` — Consulter les indicateurs (chiffre d'affaires, taux d'occupation)

---

## 6. Comptes de test & Jeux de données (Postman)

Les bases de données étant configurées en mémoire (**H2**), les données sont initialisées via les requêtes REST ci-dessous à travers l'**API Gateway (`http://localhost:8085`)**. Aucune authentification n'est requise en environnement de développement.

### A. Création d'un véhicule (`POST http://localhost:8085/api/vehicules`)
```json
{
  "marque": "Peugeot",
  "modele": "208",
  "immatriculation": "215 TN 4821",
  "categorie": "CITADINE",
  "prixJournalier": 100.0,
  "kilometrage": 15000,
  "statut": "DISPONIBLE"
}
```

### B. Création d'un client éligible (`POST http://localhost:8085/api/clients`)
*(Règle métier : âge >= 21 ans et `dateExpirationPermis` postérieure à la date du jour)*
```json
{
  "nom": "Ben Ali",
  "prenom": "Sami",
  "email": "sami.ok@email.com",
  "telephone": "22123456",
  "dateNaissance": "1995-03-15",
  "numeroPermis": "12/345678",
  "dateExpirationPermis": "2030-05-10",
  "categorie": "STANDARD",
  "actif": true
}
```

### C. Création d'un client non éligible (`POST http://localhost:8085/api/clients`)
*(Permis expiré et âge < 21 ans — permet de tester le rejet `400 Bad Request` lors d'une réservation)*
```json
{
  "nom": "Trabelsi",
  "prenom": "Youssef",
  "email": "youssef.test@email.com",
  "telephone": "55987654",
  "dateNaissance": "2008-06-20",
  "numeroPermis": "99/112233",
  "dateExpirationPermis": "2024-01-10",
  "categorie": "STANDARD",
  "actif": true
}
```

### D. Simulation & Création d'une réservation (`POST http://localhost:8085/api/locations`)
```json
{
  "clientId": 1,
  "vehiculeId": 1,
  "dateDebut": "2026-11-26",
  "dateFinPrevue": "2026-11-29"
}
```

### E. Exécution complète du cycle de vie
1. **Simulation :** `POST http://localhost:8085/api/locations/simulation` (avec le JSON ci-dessus)
2. **Création (`201 Created`) :** `POST http://localhost:8085/api/locations` (avec le JSON ci-dessus)
3. **Consultation enrichie (`200 OK`) :** `GET http://localhost:8085/api/locations/1`
4. **Départ (`200 OK`) :** `PATCH http://localhost:8085/api/locations/1/depart?kmDepart=15000`
5. **Retour avec retard et pénalité (`200 OK`) :** `PATCH http://localhost:8085/api/locations/1/retour?kmRetour=15450&dateRetourEffectif=2026-12-01`
6. **Statistiques (`200 OK`) :** `GET http://localhost:8085/api/locations/statistiques`

---

## 7. Captures d'écran

### Tableau de bord Eureka (`http://localhost:8761`)
![Eureka Dashboard](docs/eureka.png)

### Traçage distribué Zipkin (`http://localhost:9411`)
![Zipkin Traces](docs/zipkin.png)

### Création d'une réservation (`POST /api/locations` — `201 Created`)
![Création Réservation](docs/postman1.png)

### Blocage métier d'un client non éligible (`POST /api/locations` — `400 Bad Request`)
![Rejet Client Non Éligible](docs/postman2.png)

### Consultation des statistiques globales (`GET /api/locations/statistiques` — `200 OK`)
![Statistiques](docs/postman3.png)
