package com.pi.vehicule_service.repository;

import org.springframework.stereotype.Repository;
import com.pi.vehicule_service.model.Vehicule;
import java.util.ArrayList;
import java.util.List;



@Repository
public class VehiculeRepository {
   
    private List<Vehicule> vehicules = new ArrayList<>();
    private Long currentId = 1L;

    public Vehicule addVehicule(Vehicule vehicule) {
        vehicule.setId(currentId++);
        vehicules.add(vehicule);
        return vehicule;
    }
    public Vehicule findByImmatriculation(String immatriculation) {
        return vehicules.stream()
                .filter(vehicule -> vehicule.getImmatriculation().equalsIgnoreCase(immatriculation))
                .findFirst()
                .orElse(null);
    }
    public Vehicule findById(Long id) {
        return vehicules.stream().filter(vehicule -> vehicule.getId().equals(id)).findFirst().orElseThrow() ;  
     }
    public List<Vehicule> findAll() {
        return vehicules;
    }
        public void delete(Vehicule vehicule) {
        vehicules.remove(vehicule);
    }
   

    }
