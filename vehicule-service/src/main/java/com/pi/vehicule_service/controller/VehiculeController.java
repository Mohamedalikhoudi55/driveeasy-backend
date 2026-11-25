package com.pi.vehicule_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.pi.vehicule_service.model.Vehicule;
import com.pi.vehicule_service.repository.VehiculeRepository;

import com.pi.vehicule_service.model.Carburant;
import com.pi.vehicule_service.model.StatutVehicule;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;      

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    @Autowired 
    private VehiculeRepository repository;
    
   @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicule add(@RequestBody Vehicule v) {
        if (repository.findByImmatriculation(v.getImmatriculation()) != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        if (v.getPrixJournalier() == null || v.getPrixJournalier().doubleValue() <= 0 || v.getAnnee() > LocalDate.now().getYear())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (v.getStatut() == null) v.setStatut(StatutVehicule.DISPONIBLE);
        return repository.addVehicule(v);
    }
    
     @GetMapping
    public List<Vehicule> findAll(@RequestParam(required = false) String categorie,
                                  @RequestParam(required = false) Carburant carburant,
                                  @RequestParam(required = false) StatutVehicule statut,
                                  @RequestParam(required = false) BigDecimal prixMax) {
        return repository.findAll().stream()
                .filter(v -> categorie == null || v.getCategorie().equalsIgnoreCase(categorie))
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || v.getPrixJournalier().compareTo(prixMax) <= 0)
                .toList();
    }

@GetMapping("/{id}")
    public Vehicule findById(@PathVariable Long id) {
        Vehicule v = repository.findById(id);
        if (v == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return v;
    }

@GetMapping("/disponibles")
public List<Vehicule> findDisponibles(
        @RequestParam(required = false) LocalDate dateDebut,
        @RequestParam(required = false) LocalDate dateFin) {
    if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateFin doit être après dateDebut");
    }
    return repository.findAll().stream()
            .filter(v -> v.getStatut() == StatutVehicule.DISPONIBLE)
            .toList();
}

    @PutMapping("/{id}")
    public Vehicule update(@PathVariable Long id, @RequestBody Vehicule v) {
        Vehicule existing = findById(id);
        v.setId(existing.getId());
        repository.delete(existing);
        return repository.addVehicule(v);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Vehicule v = findById(id);
        if (v.getStatut() == StatutVehicule.LOUE) throw new ResponseStatusException(HttpStatus.CONFLICT);
        repository.delete(v);
    }

    @PatchMapping("/{id}/statut")
    public Vehicule updateStatut(@PathVariable Long id, @RequestParam StatutVehicule statut) {
        Vehicule v = findById(id);
        v.setStatut(statut);
        return v;
    }

   
}