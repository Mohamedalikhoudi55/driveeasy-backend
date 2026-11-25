package com.pi.location_service.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Location {
    private Long id;
    private Long clientId;
    private Long vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateRetourEffectif;
    private Double kmDepart;
    private Double kmRetour;
    private Double prixJournalierApplique;
    private Double montantTotal;
    private Double penalite;
    private StatutLocation statut;
    private LocalDateTime dateCreation;

    public Location() {
        this.dateCreation = LocalDateTime.now();
        this.penalite = 0.0;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }

    public LocalDate getDateRetourEffectif() { return dateRetourEffectif; }
    public void setDateRetourEffectif(LocalDate dateRetourEffectif) { this.dateRetourEffectif = dateRetourEffectif; }

    public Double getKmDepart() { return kmDepart; }
    public void setKmDepart(Double kmDepart) { this.kmDepart = kmDepart; }

    public Double getKmRetour() { return kmRetour; }
    public void setKmRetour(Double kmRetour) { this.kmRetour = kmRetour; }

    public Double getPrixJournalierApplique() { return prixJournalierApplique; }
    public void setPrixJournalierApplique(Double prixJournalierApplique) { this.prixJournalierApplique = prixJournalierApplique; }

    public Double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(Double montantTotal) { this.montantTotal = montantTotal; }

    public Double getPenalite() { return penalite; }
    public void setPenalite(Double penalite) { this.penalite = penalite; }

    public StatutLocation getStatut() { return statut; }
    public void setStatut(StatutLocation statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}