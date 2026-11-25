package com.pi.client_service.repository;

import org.springframework.stereotype.Repository;
import com.pi.client_service.model.Client;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ClientRepository {
   
    private List<Client> clients = new ArrayList<>();
    private Long currentId = 1L;

    public Client addClient(Client client) {
        client.setId(currentId++);
        clients.add(client);
        return client;
    }

    public Client findByCin(String cin) {
        return clients.stream()
                .filter(client -> client.getCin() != null && client.getCin().equalsIgnoreCase(cin))
                .findFirst()
                .orElse(null);
    }
    
    public Client findByEmail(String email) {
        return clients.stream()
                .filter(client -> client.getEmail() != null && client.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }
    public Client findByNom(String nom) {
        return clients.stream()
                .filter(client -> client.getNom() != null && client.getNom().equalsIgnoreCase(nom))
                .findFirst()
                .orElse(null);
    }

    public Client findById(Long id) {
        return clients.stream()
                .filter(client -> client.getId().equals(id))
                .findFirst()
                .orElseThrow();  
    }

    public List<Client> findAll() {
        return clients;
    }

    public void delete(Client client) {
        clients.remove(client);
    }
}