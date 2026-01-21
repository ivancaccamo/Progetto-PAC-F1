package com.ivancaccamo.pacf1.service;

import com.ivancaccamo.pacf1.model.PredictionRequest;
import com.ivancaccamo.pacf1.model.PredictionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * Servizio responsabile della comunicazione con il microservizio esterno di Machine Learning.
 * <p>
 * Questa classe agisce come un client HTTP (o Adapter) che interfaccia il backend Java
 * con l'applicazione Python (Flask). Si occupa di costruire la richiesta, inviare i dati
 * ambientali e del circuito, e deserializzare la risposta JSON contenente le predizioni
 * sul degrado gomme.
 * </p>
 *
 * @author Team SPS-F1
 */
@Service
public class PythonMLService {

    /**
     * Client HTTP fornito dal framework Spring per effettuare chiamate REST.
     */
    @Autowired
    private RestTemplate restTemplate;

    /**
     * URL dell'endpoint esposto dal servizio Python Flask.
     * Attualmente configurato per puntare all'istanza locale (localhost).
     */
    // Assicurati che il server Python sia acceso su questa porta!
    private final String ML_SERVICE_URL = "http://127.0.0.1:5000/predict";

    /**
     * Richiede al modulo Python le previsioni di degrado per il circuito e le condizioni specificate.
     * <p>
     * Il metodo prepara il payload JSON (incluso il set standard di mescole SOFT, MEDIUM, HARD),
     * esegue una richiesta POST sincrona e gestisce eventuali errori di comunicazione.
     * </p>
     *
     * @param circuit   Il nome del circuito su cui effettuare la predizione.
     * @param airTemp   La temperatura dell'aria attuale.
     * @param trackTemp La temperatura dell'asfalto attuale.
     * @return Un oggetto {@link PredictionResponse} contenente i dati di degrado se la chiamata ha successo,
     * {@code null} se il servizio Python non è raggiungibile o restituisce errore.
     */
    public PredictionResponse getPrediction(String circuit, double airTemp, double trackTemp) {
        List<String> compounds = List.of("SOFT", "MEDIUM", "HARD");
        
        PredictionRequest request = new PredictionRequest(circuit, airTemp, trackTemp, compounds);

        try {
            return restTemplate.postForObject(ML_SERVICE_URL, request, PredictionResponse.class);
        } catch (Exception e) {
            System.err.println("ERRORE: Python non risponde! " + e.getMessage());
            return null;
        }
    }
}