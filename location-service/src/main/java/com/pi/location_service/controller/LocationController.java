package com.pi.location_service.controller;

import com.pi.location_service.client.ClientServiceClient;
import com.pi.location_service.client.VehiculeServiceClient;
import com.pi.location_service.model.Location;
import com.pi.location_service.model.StatutLocation;
import com.pi.location_service.repository.LocationRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationRepository repo;
    private final ClientServiceClient clientClient;
    private final VehiculeServiceClient vehiculeClient;

    public LocationController(LocationRepository repo, ClientServiceClient clientClient, VehiculeServiceClient vehiculeClient) {
        this.repo = repo;
        this.clientClient = clientClient;
        this.vehiculeClient = vehiculeClient;
    }

    // 1. GET /locations : Liste paginée avec filtres
    @GetMapping
    public Map<String, Object> getAll(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Location> list = repo.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || l.getClientId().equals(clientId))
                .filter(l -> vehiculeId == null || l.getVehiculeId().equals(vehiculeId))
                .filter(l -> dateDebut == null || !l.getDateDebut().isBefore(dateDebut))
                .filter(l -> dateFin == null || !l.getDateFinPrevue().isAfter(dateFin))
                .toList();

        int start = Math.min(page * size, list.size());
        int end = Math.min(start + size, list.size());

        return Map.of(
                "content", list.subList(start, end),
                "page", page,
                "totalElements", list.size(),
                "totalPages", (int) Math.ceil((double) list.size() / size)
        );
    }

    // 2. GET /locations/{id} : Détail enrichi (client + véhicule)
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).<ResponseEntity<?>>map(loc -> {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("location", loc);
            try { res.put("client", clientClient.getClientById(loc.getClientId())); } catch (Exception e) { res.put("client", null); }
            try { res.put("vehicule", vehiculeClient.getVehiculeById(loc.getVehiculeId())); } catch (Exception e) { res.put("vehicule", null); }
            return ResponseEntity.ok(res);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 3. POST /locations/simulation : Simulation du prix sans création[cite: 1]
    @PostMapping("/simulation")
    public ResponseEntity<?> simuler(@RequestBody Location req) {
        try {
            double[] t = calculerTarif(req);
            return ResponseEntity.ok(Map.of("jours", (long) t[0], "prixJournalierApplique", t[1], "montantTotal", t[2]));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur simulation : " + e.getMessage());
        }
    }

    // 4. POST /locations : Création d'une réservation[cite: 1]
    @PostMapping
    public ResponseEntity<?> creer(@RequestBody Location req) {
        try {
            if (!Boolean.TRUE.equals(clientClient.isClientEligible(req.getClientId()))) {
                return ResponseEntity.badRequest().body("Client non éligible.");
            }
            Map<String, Object> vehicule = vehiculeClient.getVehiculeById(req.getVehiculeId());
            if (!"DISPONIBLE".equalsIgnoreCase(String.valueOf(vehicule.get("statut"))) || !estDisponible(req)) {
                return ResponseEntity.badRequest().body("Véhicule indisponible sur cette période.");
            }

            double[] t = calculerTarif(req);
            req.setPrixJournalierApplique(t[1]);
            req.setMontantTotal(t[2]);
            req.setPenalite(0.0);
            req.setStatut(StatutLocation.RESERVEE);
            req.setDateCreation(LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(req));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Erreur inter-services : " + e.getMessage());
        }
    }

    // 5. PATCH /locations/{id}/depart : Démarrage de la location[cite: 1]
    @PatchMapping("/{id}/depart")
    public ResponseEntity<?> depart(@PathVariable Long id, @RequestParam Double kmDepart) {
        return repo.findById(id).<ResponseEntity<?>>map(loc -> {
            if (loc.getStatut() != StatutLocation.RESERVEE) return ResponseEntity.badRequest().body("Statut invalide.");
            try {
                vehiculeClient.updateStatut(loc.getVehiculeId(), "LOUE");
                loc.setKmDepart(kmDepart);
                loc.setStatut(StatutLocation.EN_COURS);
                return ResponseEntity.ok(repo.save(loc));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Échec mise à jour véhicule.");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // 6. PATCH /locations/{id}/retour : Retour, pénalités et fidélité[cite: 1]
    @PatchMapping("/{id}/retour")
    public ResponseEntity<?> retour(@PathVariable Long id, @RequestParam Double kmRetour,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRetourEffectif) {
        return repo.findById(id).<ResponseEntity<?>>map(loc -> {
            if (loc.getStatut() != StatutLocation.EN_COURS) return ResponseEntity.badRequest().body("Statut invalide.");
            try {
                vehiculeClient.updateStatut(loc.getVehiculeId(), "DISPONIBLE");

                LocalDate retour = (dateRetourEffectif != null) ? dateRetourEffectif : LocalDate.now();
                long retard = Math.max(0, ChronoUnit.DAYS.between(loc.getDateFinPrevue(), retour));
                double penalite = retard * 1.5 * loc.getPrixJournalierApplique();

                loc.setKmRetour(kmRetour);
                loc.setDateRetourEffectif(retour);
                loc.setPenalite(penalite);
                loc.setMontantTotal(loc.getMontantTotal() + penalite);
                loc.setStatut(StatutLocation.TERMINEE);
                repo.save(loc);

                majFideliteClient(loc.getClientId());
                return ResponseEntity.ok(loc);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Échec libération véhicule.");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // 7. PATCH /locations/{id}/annulation : Annulation d'une réservation[cite: 1]
    @PatchMapping("/{id}/annulation")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        return repo.findById(id).<ResponseEntity<?>>map(loc -> {
            if (loc.getStatut() != StatutLocation.RESERVEE) return ResponseEntity.badRequest().body("Annulation impossible.");
            loc.setStatut(StatutLocation.ANNULEE);
            return ResponseEntity.ok(repo.save(loc));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 8. GET /locations/statistiques : CA, taux d'occupation, top véhicules[cite: 1]
    @GetMapping("/statistiques")
    public Map<String, Object> statistiques() {
        List<Location> toutes = repo.findAll();
        double ca = toutes.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                .mapToDouble(l -> l.getMontantTotal() != null ? l.getMontantTotal() : 0.0).sum();

        long enCours = toutes.stream().filter(l -> l.getStatut() == StatutLocation.EN_COURS).count();
        long actives = toutes.stream().filter(l -> l.getStatut() != StatutLocation.ANNULEE).count();

        return Map.of(
                "chiffreAffaires", ca,
                "tauxOccupation", actives > 0 ? Math.round((enCours * 10000.0) / actives) / 100.0 : 0.0,
                "topVehicules", toutes.stream().filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                        .collect(Collectors.groupingBy(Location::getVehiculeId, Collectors.counting()))
        );
    }

    // --- Helpers ---

    private boolean estDisponible(Location req) {
        return repo.findByVehiculeId(req.getVehiculeId()).stream()
                .filter(l -> l.getStatut() == StatutLocation.RESERVEE || l.getStatut() == StatutLocation.EN_COURS)
                .noneMatch(l -> !req.getDateFinPrevue().isBefore(l.getDateDebut()) && !req.getDateDebut().isAfter(l.getDateFinPrevue()));
    }

    private double[] calculerTarif(Location req) {
        Map<String, Object> client = clientClient.getClientById(req.getClientId());
        Map<String, Object> vehicule = vehiculeClient.getVehiculeById(req.getVehiculeId());

        long jours = Math.max(1, ChronoUnit.DAYS.between(req.getDateDebut(), req.getDateFinPrevue()));
        double prix = Double.parseDouble(String.valueOf(vehicule.getOrDefault("prixJournalier", 100.0)))
                * Double.parseDouble(String.valueOf(vehicule.getOrDefault("coefficientCategorie", 1.0)));

        String cat = String.valueOf(client.getOrDefault("categorie", "STANDARD"));
        double remise = "VIP".equalsIgnoreCase(cat) ? 0.10 : ("FIDELE".equalsIgnoreCase(cat) ? 0.05 : 0.0);

        return new double[]{jours, prix, jours * prix * (1.0 - remise)};
    }

    private void majFideliteClient(Long clientId) {
        long count = repo.findByClientId(clientId).stream().filter(l -> l.getStatut() == StatutLocation.TERMINEE).count();
        try {
            if (count >= 15) clientClient.updateCategorie(clientId, "VIP");
            else if (count >= 5) clientClient.updateCategorie(clientId, "FIDELE");
        } catch (Exception ignored) {}
    }
}