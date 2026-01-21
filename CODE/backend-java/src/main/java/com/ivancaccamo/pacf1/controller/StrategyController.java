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

/**
 * Controller REST principale dell'applicazione SPS-F1.
 * <p>
 * Questa classe agisce come punto di ingresso per le API (Facade Pattern),
 * orchestrando la comunicazione tra il frontend, il servizio di Machine Learning (Python)
 * e il motore di ottimizzazione interno (Java).
 * Gestisce inoltre le operazioni CRUD per il salvataggio dello storico delle strategie.
 * </p>
 *
 * @author Team SPS-F1
 */
@RestController
@RequestMapping("/api")
public class StrategyController {

    @Autowired
    private PythonMLService mlService;

    @Autowired
    private OptimizationEngine optimizer;

    @Autowired
    private StrategyRepository repository;

    /**
     * Calcola le migliori strategie di gara basandosi sui parametri ambientali forniti.
     * <p>
     * Il flusso di esecuzione è il seguente:
     * 1. Invia i dati meteo al microservizio Python per ottenere le curve di degrado.
     * 2. Se le predizioni sono valide, invoca l'OptimizationEngine.
     * 3. Restituisce le Top 3 strategie ottimali calcolate tramite Programmazione Dinamica.
     * </p>
     *
     * @param circuit   Il nome del circuito (es. "Bahrain Grand Prix").
     * @param laps      Il numero totale di giri della gara.
     * @param airTemp   La temperatura dell'aria in gradi Celsius.
     * @param trackTemp La temperatura dell'asfalto in gradi Celsius.
     * @return Una lista di oggetti {@link RaceStrategy} contenente le strategie suggerite,
     * o una lista vuota se il servizio ML non risponde.
     */
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

    /**
     * Salva una specifica strategia nel database persistente.
     * <p>
     * Permette all'utente di archiviare una simulazione ritenuta interessante
     * per poterla consultare successivamente nella sezione "Archivio".
     * </p>
     *
     * @param strategy L'oggetto {@link SavedStrategy} contenente i dettagli della simulazione da salvare.
     * @return L'oggetto salvato, comprensivo dell'ID generato dal database.
     */
    // 2. Salva una strategia nel DB
    @PostMapping("/history")
    public SavedStrategy saveStrategy(@RequestBody SavedStrategy strategy) {
        return repository.save(strategy);
    }

    /**
     * Recupera l'intero storico delle simulazioni salvate.
     *
     * @return Una lista di tutte le strategie presenti nel database.
     */
    // 3. Leggi tutto lo storico
    @GetMapping("/history")
    public List<SavedStrategy> getHistory() {
        return repository.findAll();
    }

    /**
     * Elimina una strategia dall'archivio.
     *
     * @param id L'identificativo univoco (Primary Key) della strategia da rimuovere.
     */
    // 4. Cancella una strategia dall'archivio
    @DeleteMapping("/history/{id}")
    public void deleteHistory(@PathVariable Long id) {
        repository.deleteById(id);
    }
}