package com.ivancaccamo.pacf1.controller;

import com.ivancaccamo.pacf1.model.PredictionResponse;
import com.ivancaccamo.pacf1.model.RaceStrategy;
import com.ivancaccamo.pacf1.service.OptimizationEngine;
import com.ivancaccamo.pacf1.service.PythonMLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class StrategyController {

    @Autowired
    private PythonMLService mlService;

    @Autowired
    private OptimizationEngine optimizer;

    // Questo è l'endpoint PRINCIPALE che restituisce la lista delle strategie
    @GetMapping("/strategy")
    public List<RaceStrategy> getStrategy(
            @RequestParam(defaultValue = "Bahrain Grand Prix") String circuit,
            @RequestParam(defaultValue = "57") int laps,
            @RequestParam(defaultValue = "30.0") double airTemp,
            @RequestParam(defaultValue = "45.0") double trackTemp) {
        
        // 1. Chiediamo i dati a Python
        PredictionResponse predictions = mlService.getPrediction(circuit, airTemp, trackTemp);
        
        if (predictions == null) return new ArrayList<>();

        // 2. Chiamiamo il NUOVO metodo che restituisce le top 3 strategie
        return optimizer.calculateTop3Strategies(laps, predictions.getPredictions());
    }

    // Endpoint di test (opzionale, puoi lasciarlo o toglierlo)
    @GetMapping("/test-connection")
    public PredictionResponse testConnection(
            @RequestParam(defaultValue = "Monaco Grand Prix") String circuit,
            @RequestParam(defaultValue = "25.0") double airTemp,
            @RequestParam(defaultValue = "35.0") double trackTemp) {
        return mlService.getPrediction(circuit, airTemp, trackTemp);
    }
}