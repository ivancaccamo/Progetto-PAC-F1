package com.ivancaccamo.pacf1.service;

import com.ivancaccamo.pacf1.model.PredictionResponse.TyrePrediction;
import com.ivancaccamo.pacf1.model.RaceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptimizationEngineTest {

    private OptimizationEngine engine;
    private List<TyrePrediction> mockTyres;

    @BeforeEach
    void setUp() {
        engine = new OptimizationEngine();
        mockTyres = new ArrayList<>();

        // Creiamo dei dati finti (Mock) per le gomme
        // SOFT: Veloce ma degrada presto
        TyrePrediction soft = new TyrePrediction();
        soft.setCompound("SOFT");
        soft.setBase_time(90.0);       // 1m 30s
        soft.setDegradation_rate(0.1); // perde 1 decimo a giro
        mockTyres.add(soft);

        // MEDIUM: Media
        TyrePrediction medium = new TyrePrediction();
        medium.setCompound("MEDIUM");
        medium.setBase_time(91.0);     // 1m 31s
        medium.setDegradation_rate(0.06);
        mockTyres.add(medium);

        // HARD: Lenta ma dura tanto
        TyrePrediction hard = new TyrePrediction();
        hard.setCompound("HARD");
        hard.setBase_time(92.5);       // 1m 32.5s
        hard.setDegradation_rate(0.02);
        mockTyres.add(hard);
    }

    @Test
    void testCalculateStrategy_StandardRace() {
        // Simuliamo una gara di 50 giri
        int totalLaps = 50;

        List<RaceStrategy> result = engine.calculateTop3Strategies(totalLaps, mockTyres);

        // VERIFICHE (Asserts)
        assertNotNull(result, "La lista delle strategie non deve essere null");
        assertFalse(result.isEmpty(), "Dovrebbe trovare almeno una strategia");
        
        RaceStrategy best = result.get(0);
        System.out.println("Miglior tempo trovato: " + best.getTotalTime());
        
        assertTrue(best.getTotalTime() > 0, "Il tempo totale deve essere positivo");
        assertTrue(best.getPitStops() >= 1, "Per 50 giri ci aspettiamo almeno 1 sosta");
    }

    @Test
    void testCalculateStrategy_ShortRace() {
        // Gara sprint di 15 giri (dovrebbe provare a farla con 0 o 1 sosta)
        int totalLaps = 15;
        
        List<RaceStrategy> result = engine.calculateTop3Strategies(totalLaps, mockTyres);
        
        assertFalse(result.isEmpty());
        // Controlliamo che il primo stint non superi i giri totali
        assertTrue(result.get(0).getStints().get(0).getLaps() <= totalLaps);
    }

    @Test
    void testCalculateStrategy_NoTyres() {
        // Caso limite: nessuna gomma disponibile
        List<RaceStrategy> result = engine.calculateTop3Strategies(50, new ArrayList<>());
        
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Se non ci sono gomme, non ci sono strategie");
    }
}