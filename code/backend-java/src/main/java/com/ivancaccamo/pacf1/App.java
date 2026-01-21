package com.ivancaccamo.pacf1; // Assicurati che combaci con la tua cartella!

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Classe principale di avvio per l'applicazione backend SPS-F1.
 * <p>
 * Questa classe contiene il metodo main che inizializza il contesto di Spring Boot,
 * avvia il server embedded (Tomcat) e configura i bean globali necessari per
 * l'applicazione, come il client REST per le chiamate ai microservizi esterni.
 * </p>
 *
 * @author Team SPS-F1
 */
@SpringBootApplication // <--- QUESTA è la magia che avvia il server
public class App {

    /**
     * Punto di ingresso dell'applicazione Java.
     * <p>
     * Questo metodo lancia l'intera suite Spring Boot, eseguendo la scansione dei
     * componenti, la configurazione automatica e l'avvio del web server sulla porta specificata
     * (default: 8080).
     * </p>
     *
     * @param args Argomenti da riga di comando passati all'avvio (opzionali).
     */
    public static void main(String[] args) {
        // Questo comando avvia Tomcat e tiene l'app accesa
        SpringApplication.run(App.class, args);
    }

    /**
     * Configura e rende disponibile un bean {@link RestTemplate}.
     * <p>
     * Il RestTemplate è il client HTTP sincrono di Spring. Viene iniettato
     * nei service (come {@code PythonMLService}) per permettere la comunicazione
     * via rete con il modulo di Machine Learning scritto in Python.
     * </p>
     *
     * @return Una nuova istanza di {@link RestTemplate}.
     */
    // Ci servirà tra poco per chiamare Python
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}