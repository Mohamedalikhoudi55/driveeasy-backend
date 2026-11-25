package com.pi.location_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import java.util.Map;

@HttpExchange("/clients")
public interface ClientServiceClient {

    @GetExchange("/{id}")
    Map<String, Object> getClientById(@PathVariable("id") Long id);

    @GetExchange("/{id}/eligibilite")
    Boolean isClientEligible(@PathVariable("id") Long id);

    @PatchExchange("/{id}/categorie")
    void updateCategorie(@PathVariable("id") Long id, @RequestParam("categorie") String categorie);
}