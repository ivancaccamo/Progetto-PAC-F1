package com.ivancaccamo.pacf1; // Assicurati che combaci con la tua cartella!

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication // <--- QUESTA è la magia che avvia il server
public class App {

    public static void main(String[] args) {
        // Questo comando avvia Tomcat e tiene l'app accesa
        SpringApplication.run(App.class, args);
    }

    // Ci servirà tra poco per chiamare Python
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}