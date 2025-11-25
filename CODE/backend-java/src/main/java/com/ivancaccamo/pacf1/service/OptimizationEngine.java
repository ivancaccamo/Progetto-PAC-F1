package com.ivancaccamo.pacf1.service;

import com.ivancaccamo.pacf1.model.PredictionResponse.TyrePrediction;
import com.ivancaccamo.pacf1.model.RaceStrategy;
import com.ivancaccamo.pacf1.model.Stint;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
public class OptimizationEngine {

    // Tempo medio perso in pit lane (cambio gomme + transito)
    private static final double PIT_STOP_LOSS = 20.0; 

    public RaceStrategy calculateOptimalStrategy(int totalLaps, List<TyrePrediction> tyres) {
        System.out.println("--- AVVIO OTTIMIZZAZIONE AVANZATA (1 e 2 Soste) ---");
        
        RaceStrategy bestStrategy = new RaceStrategy();
        bestStrategy.setTotalTime(Double.MAX_VALUE); // Inizializziamo con infinito

        if (tyres.isEmpty()) return bestStrategy;

        // =================================================================================
        // SCENARIO 1: UNA SOSTA (2 STINT)
        // =================================================================================
        for (TyrePrediction t1 : tyres) {
            for (TyrePrediction t2 : tyres) {
                
                // REGOLA F1: Devi usare almeno due mescole diverse in gara.
                // Se t1 e t2 sono uguali (es. Soft-Soft), strategia ILLEGALE.
                if (t1.getCompound().equals(t2.getCompound())) continue;

                // Cerchiamo il giro di sosta ottimale (tra il 10% e il 90% della gara)
                for (int stopLap = (int)(totalLaps * 0.10); stopLap < (int)(totalLaps * 0.90); stopLap++) {
                    
                    // Stint 1: da giro 0 a stopLap
                    double time1 = calculateStintTime(t1, stopLap);
                    // Stint 2: da stopLap alla fine
                    double time2 = calculateStintTime(t2, totalLaps - stopLap);
                    
                    double totalTime = time1 + PIT_STOP_LOSS + time2;

                    if (totalTime < bestStrategy.getTotalTime()) {
                        updateBestStrategy(bestStrategy, totalTime, 1, 
                            new Stint(t1.getCompound(), 1, stopLap),
                            new Stint(t2.getCompound(), stopLap + 1, totalLaps)
                        );
                    }
                }
            }
        }

        // =================================================================================
        // SCENARIO 2: DUE SOSTE (3 STINT)
        // =================================================================================
        for (TyrePrediction t1 : tyres) {
            for (TyrePrediction t2 : tyres) {
                for (TyrePrediction t3 : tyres) {

                    // REGOLA F1: Devi usare almeno 2 mescole diverse.
                    // Esempio valido: Soft -> Medium -> Soft (2 mescole usate).
                    // Esempio illegale: Soft -> Soft -> Soft (1 mescola usata).
                    Set<String> usedCompounds = new HashSet<>();
                    usedCompounds.add(t1.getCompound());
                    usedCompounds.add(t2.getCompound());
                    usedCompounds.add(t3.getCompound());
                    
                    if (usedCompounds.size() < 2) continue; // Salta se illegale

                    // Loop nidificati per trovare i due punti di sosta
                    // Stop1: tra giro 10% e 60%
                    for (int stop1 = (int)(totalLaps * 0.10); stop1 < (int)(totalLaps * 0.60); stop1++) {
                        
                        // Stop2: deve essere almeno 10 giri dopo Stop1 e prima della fine
                        for (int stop2 = stop1 + 10; stop2 < (int)(totalLaps * 0.95); stop2++) {
                            
                            // Stint 1
                            double time1 = calculateStintTime(t1, stop1);
                            // Stint 2 (durata = stop2 - stop1)
                            double time2 = calculateStintTime(t2, stop2 - stop1);
                            // Stint 3 (durata = total - stop2)
                            double time3 = calculateStintTime(t3, totalLaps - stop2);

                            // Qui paghiamo DUE VOLTE il pit stop loss
                            double totalTime = time1 + PIT_STOP_LOSS + time2 + PIT_STOP_LOSS + time3;

                            if (totalTime < bestStrategy.getTotalTime()) {
                                updateBestStrategy(bestStrategy, totalTime, 2, 
                                    new Stint(t1.getCompound(), 1, stop1),
                                    new Stint(t2.getCompound(), stop1 + 1, stop2),
                                    new Stint(t3.getCompound(), stop2 + 1, totalLaps)
                                );
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Strategia Migliore Trovata -> Soste: " + bestStrategy.getPitStops() + 
                           " | Tempo: " + bestStrategy.getTotalTime());
        return bestStrategy;
    }

    // Helper per aggiornare l'oggetto strategia (gestisce varargs per gli stint)
    private void updateBestStrategy(RaceStrategy strategy, double time, int stops, Stint... stints) {
        strategy.setTotalTime(time);
        strategy.setPitStops(stops);
        strategy.getStints().clear();
        for (Stint s : stints) {
            strategy.getStints().add(s);
        }
    }

    // Calcolo fisico del tempo impiegato per percorrere N giri con degrado
    private double calculateStintTime(TyrePrediction tyre, int laps) {
        double total = 0;
        double currentLapTime = tyre.getBase_time();
        
        for (int i = 0; i < laps; i++) {
            total += currentLapTime;
            // Al giro successivo la gomma è più lenta a causa del degrado
            currentLapTime += tyre.getDegradation_rate();
        }
        return total;
    }
}