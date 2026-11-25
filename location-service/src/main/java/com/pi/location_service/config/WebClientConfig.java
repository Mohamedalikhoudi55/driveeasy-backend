package com.pi.location_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import com.pi.location_service.client.ClientServiceClient;
import com.pi.location_service.client.VehiculeServiceClient;

@Configuration
public class WebClientConfig {

    @Autowired
    private LoadBalancedExchangeFilterFunction filterFunction;

    @Bean
    public WebClient clientWebClient() {
        return WebClient.builder()
                .baseUrl("http://client-service")
                .filter(filterFunction)
                .build();
    }

    @Bean
    public ClientServiceClient clientServiceClient() {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(clientWebClient()))
                .build();
        return httpServiceProxyFactory.createClient(ClientServiceClient.class);
    }

    @Bean
    public WebClient vehiculeWebClient() {
        return WebClient.builder()
                .baseUrl("http://vehicule-service")
                .filter(filterFunction)
                .build();
    }

    @Bean
    public VehiculeServiceClient vehiculeServiceClient() {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(vehiculeWebClient()))
                .build();
        return httpServiceProxyFactory.createClient(VehiculeServiceClient.class);
    }
}
