package com.almacenesgallego.primeraVersion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public WebClient webClient() {
        // Aquí pones la URL base de tu API
        return WebClient.create("http://localhost:8080/api/");
    }
}
