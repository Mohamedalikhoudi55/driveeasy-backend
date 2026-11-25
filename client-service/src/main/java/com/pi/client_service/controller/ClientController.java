package com.pi.client_service.controller;

import com.pi.client_service.model.CategorieClient;
import com.pi.client_service.model.Client;
import com.pi.client_service.repository.ClientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clients")
public class ClientController {

 @Autowired 
    private ClientRepository repository;

    // 1. GET /clients : Recherche par nom / CIN / email
    @GetMapping
    public List<Client> getClients(@RequestParam(required = false) String nom,
                                   @RequestParam(required = false) String cin,
                                   @RequestParam(required = false) String email) {
        return repository.findAll().stream()
                .filter(c -> nom == null || (c.getNom() != null && c.getNom().equalsIgnoreCase(nom)))
                .filter(c -> cin == null || (c.getCin() != null && c.getCin().equalsIgnoreCase(cin)))
                .filter(c -> email == null || (c.getEmail() != null && c.getEmail().equalsIgnoreCase(email)))
                .collect(Collectors.toList());
    }

    // 2. GET /clients/{id} : Détail d’un client
    @GetMapping("/{id}")
    public Client getClientById(@PathVariable Long id) {
        return repository.findById(id);
    }

    // 3. POST /clients : Création d’un client
    @PostMapping
    public Client createClient(@RequestBody Client client) {
        client.setDateInscription(LocalDate.now());
        if (client.getCategorieClient() == null) {
            client.setCategorieClient(CategorieClient.STANDARD);
        }
        return repository.addClient(client);
    }

    // 4. PUT /clients/{id} : Modification d’un client
    @PutMapping("/{id}")
    public Client updateClient(@PathVariable Long id, @RequestBody Client client) {
        Client existingClient = repository.findById(id);
        
        // On supprime l'ancien objet de la liste et on ajoute le nouveau
        repository.delete(existingClient);
        client.setId(id);
        
        // Pour garder la date d'inscription d'origine
        client.setDateInscription(existingClient.getDateInscription()); 
        
        repository.findAll().add(client); 
        return client;
    }

    // 5. DELETE /clients/{id} : Suppression (refusée si location active)
    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable Long id) {
        // Remarque : La logique "refusée si location active" nécessitera 
        // une communication avec le location-service plus tard.
        Client client = repository.findById(id);
        repository.delete(client);
    }

    // 6. GET /clients/{id}/eligibilite : Retourne si le client peut louer (âge >= 21 ans, permis valide)
    @GetMapping("/{id}/eligibilite")
    public boolean verifierEligibilite(@PathVariable Long id) {
        Client client = repository.findById(id);
        
        if (client.getDateNaissance() == null || client.getDateExpirationPermis() == null) {
            return false;
        }
        
        // Calcul de l'âge
        int age = Period.between(client.getDateNaissance(), LocalDate.now()).getYears();
        // Vérification de la validité du permis
        boolean permisValide = client.getDateExpirationPermis().isAfter(LocalDate.now());
        
        return age >= 21 && permisValide;
    }

    // 7. PATCH /clients/{id}/categorie : Mise à jour de la catégorie de fidélité
    @PatchMapping("/{id}/categorie")
    public Client updateCategorie(@PathVariable Long id, @RequestParam CategorieClient categorie) {
        Client client = repository.findById(id);
        client.setCategorieClient(categorie);
        return client;
    }
}