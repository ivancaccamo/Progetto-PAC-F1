package com.ivancaccamo.pacf1.controller;

import com.ivancaccamo.pacf1.model.PredictionResponse;
import com.ivancaccamo.pacf1.model.RaceStrategy;
import com.ivancaccamo.pacf1.model.SavedStrategy;
import com.ivancaccamo.pacf1.repository.StrategyRepository;
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

    @Autowired
    private StrategyRepository repository;

    // 1. Calcola Strategia
    @GetMapping("/strategy")
    public List<RaceStrategy> getStrategy(
            @RequestParam(defaultValue = "Bahrain Grand Prix") String circuit,
            @RequestParam(defaultValue = "57") int laps,
            @RequestParam(defaultValue = "30.0") double airTemp,
            @RequestParam(defaultValue = "45.0") double trackTemp) {

        PredictionResponse predictions = mlService.getPrediction(circuit, airTemp, trackTemp);
        if (predictions == null) return new ArrayList<>();
        return optimizer.calculateTop3Strategies(laps, predictions.getPredictions());
    }

    // 2. Salva una strategia nel DB
    @PostMapping("/history")
    public SavedStrategy saveStrategy(@RequestBody SavedStrategy strategy) {
        return repository.save(strategy);
    }

    // 3. Leggi tutto lo storico
    @GetMapping("/history")
    public List<SavedStrategy> getHistory() {
        return repository.findAll();
    }

    // 4. Cancella una strategia dall'archivio
    @DeleteMapping("/history/{id}")
    public void deleteHistory(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
