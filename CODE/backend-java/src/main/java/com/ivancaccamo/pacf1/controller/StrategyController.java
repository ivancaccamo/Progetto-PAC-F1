package com.ivancaccamo.pacf1.controller;

import com.ivancaccamo.pacf1.model.PredictionResponse;
import com.ivancaccamo.pacf1.model.RaceStrategy;
import com.ivancaccamo.pacf1.service.OptimizationEngine;
import com.ivancaccamo.pacf1.service.PythonMLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StrategyController {

    @Autowired
    private PythonMLService mlService;

    @Autowired
    private OptimizationEngine optimizer;

    @GetMapping("/strategy")
    public RaceStrategy getStrategy(
            @RequestParam(defaultValue = "Monza Grand Prix") String circuit,
            @RequestParam(defaultValue = "53") int laps,
            @RequestParam(defaultValue = "25.0") double airTemp,
            @RequestParam(defaultValue = "35.0") double trackTemp) {
        
        System.out.println("--- NUOVA RICHIESTA STRATEGIA ---");
        System.out.println("Circuito: " + circuit + ", Giri: " + laps);

        // 1. Chiedi i dati a Python
        PredictionResponse predictions = mlService.getPrediction(circuit, airTemp, trackTemp);
        
        if (predictions == null) {
            System.err.println("ERRORE: La risposta di Python è NULL!");
            return null;
        }

        System.out.println("Risposta Python ricevuta. Numero predizioni: " + predictions.getPredictions().size());
        
        // Stampiamo cosa ci ha dato Python per essere sicuri
        for(var p : predictions.getPredictions()) {
            System.out.println(" - Gomma: " + p.getCompound() + 
                               " | Base: " + p.getBase_time() + 
                               " | Degrado: " + p.getDegradation_rate());
        }

        // 2. Calcola la strategia
        RaceStrategy strategy = optimizer.calculateOptimalStrategy(laps, predictions.getPredictions());
        
        System.out.println("Strategia Calcolata -> Soste: " + strategy.getPitStops());
        return strategy;
    }

    @GetMapping("/test-connection")
    public PredictionResponse testConnection(
            @RequestParam(defaultValue = "Monaco Grand Prix") String circuit,
            @RequestParam(defaultValue = "25.0") double airTemp,
            @RequestParam(defaultValue = "35.0") double trackTemp) {
        return mlService.getPrediction(circuit, airTemp, trackTemp);
    }
}