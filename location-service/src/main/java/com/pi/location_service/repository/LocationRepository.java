package com.pi.location_service.repository;

import com.pi.location_service.model.Location;
import com.pi.location_service.model.StatutLocation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class LocationRepository {

    private final List<Location> locations = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Location> findAll() {
        return locations;
    }

    public Optional<Location> findById(Long id) {
        return locations.stream()
                .filter(l -> l.getId().equals(id))
                .findFirst();
    }

    public List<Location> findByClientId(Long clientId) {
        return locations.stream()
                .filter(l -> l.getClientId().equals(clientId))
                .collect(Collectors.toList());
    }

    public List<Location> findByVehiculeId(Long vehiculeId) {
        return locations.stream()
                .filter(l -> l.getVehiculeId().equals(vehiculeId))
                .collect(Collectors.toList());
    }

    public List<Location> findByStatut(StatutLocation statut) {
        return locations.stream()
                .filter(l -> l.getStatut() == statut)
                .collect(Collectors.toList());
    }

    public Location save(Location location) {
        if (location.getId() == null) {
            location.setId(sequence.getAndIncrement());
            locations.add(location);
        } else {
            update(location.getId(), location);
        }
        return location;
    }

    public Optional<Location> update(Long id, Location updatedLocation) {
        return findById(id).map(existing -> {
            existing.setClientId(updatedLocation.getClientId());
            existing.setVehiculeId(updatedLocation.getVehiculeId());
            existing.setDateDebut(updatedLocation.getDateDebut());
            existing.setDateFinPrevue(updatedLocation.getDateFinPrevue());
            existing.setDateRetourEffectif(updatedLocation.getDateRetourEffectif());
            existing.setKmDepart(updatedLocation.getKmDepart());
            existing.setKmRetour(updatedLocation.getKmRetour());
            existing.setPrixJournalierApplique(updatedLocation.getPrixJournalierApplique());
            existing.setMontantTotal(updatedLocation.getMontantTotal());
            existing.setPenalite(updatedLocation.getPenalite());
            existing.setStatut(updatedLocation.getStatut());
            existing.setDateCreation(updatedLocation.getDateCreation());
            return existing;
        });
    }

    public boolean deleteById(Long id) {
        return locations.removeIf(l -> l.getId().equals(id));
    }

    public long count() {
        return locations.size();
    }
}
