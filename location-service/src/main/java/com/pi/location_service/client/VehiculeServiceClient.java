package com.pi.location_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import java.util.Map;

@HttpExchange("/vehicules")
public interface VehiculeServiceClient {

    @GetExchange("/{id}")
    Map<String, Object> getVehiculeById(@PathVariable("id") Long id);

    @PatchExchange("/{id}/statut")
    void updateStatut(@PathVariable("id") Long id, @RequestParam("statut") String statut);
}