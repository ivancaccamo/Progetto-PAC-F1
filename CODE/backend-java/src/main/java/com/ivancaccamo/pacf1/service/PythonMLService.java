package com.ivancaccamo.pacf1.service;

import com.ivancaccamo.pacf1.model.PredictionRequest;
import com.ivancaccamo.pacf1.model.PredictionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class PythonMLService {

    @Autowired
    private RestTemplate restTemplate;

    // Assicurati che il server Python sia acceso su questa porta!
    private final String ML_SERVICE_URL = "http://127.0.0.1:5000/predict";

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