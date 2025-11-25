package com.ivancaccamo.pacf1.service;

import com.ivancaccamo.pacf1.model.PredictionResponse.TyrePrediction;
import com.ivancaccamo.pacf1.model.RaceStrategy;
import com.ivancaccamo.pacf1.model.Stint;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OptimizationEngine {

    // Tempo medio perso in pit lane (cambio gomme + transito).
    // Costante fissa, ma potrebbe essere resa dinamica per circuito in futuro.
    private static final double PIT_STOP_LOSS = 20.0; 

    /**
     * Calcola le migliori strategie possibili basandosi sui dati di degrado predetti.
     * Utilizza un approccio di Ricerca Esaustiva (Brute Force) intelligente.
     * * @param totalLaps Numero totale di giri della gara.
     * @param tyres Lista delle mescole disponibili con i relativi parametri di performance.
     * @return Una lista contenente le 3 strategie migliori (ordinate per tempo totale).
     */
    public List<RaceStrategy> calculateTop3Strategies(int totalLaps, List<TyrePrediction> tyres) {
        List<RaceStrategy> allStrategies = new ArrayList<>();

        // Se non abbiamo dati sulle gomme, restituiamo una lista vuota
        if (tyres.isEmpty()) return allStrategies;

        System.out.println("--- AVVIO OTTIMIZZAZIONE: Calcolo Top 3 Strategie ---");

        // =================================================================================
        // SCENARIO 1: UNA SOSTA (2 STINT)
        // Complessità: O(M^2 * L) dove M=mescole, L=giri
        // =================================================================================
        for (TyrePrediction t1 : tyres) {
            for (TyrePrediction t2 : tyres) {
                
                // REGOLA F1: Obbligo cambio mescola.
                // Se la gomma del primo stint è uguale a quella del secondo, saltiamo.
                if (t1.getCompound().equals(t2.getCompound())) continue;

                // Cerchiamo il "giro di taglio" ottimale per la sosta.
                // Limitiamo la ricerca tra il 20% e l'80% della gara per evitare stint irreali (troppo corti/lunghi).
                // Usiamo uno step di 2 giri (stopLap += 2) per velocizzare e ridurre strategie duplicate simili.
                for (int stopLap = (int)(totalLaps * 0.2); stopLap < (int)(totalLaps * 0.8); stopLap += 2) {
                    
                    // Calcolo tempo Stint 1 (da start a stopLap)
                    double time1 = calculateStintTime(t1, stopLap);
                    
                    // Calcolo tempo Stint 2 (da stopLap alla fine)
                    int lapsStint2 = totalLaps - stopLap;
                    double time2 = calculateStintTime(t2, lapsStint2);
                    
                    // Tempo Totale = Stint 1 + Pit Stop + Stint 2
                    double totalTime = time1 + PIT_STOP_LOSS + time2;
                    
                    // Creiamo l'oggetto strategia
                    RaceStrategy s = new RaceStrategy();
                    s.setTotalTime(totalTime);
                    s.setPitStops(1);
                    s.getStints().add(new Stint(t1.getCompound(), 1, stopLap));
                    s.getStints().add(new Stint(t2.getCompound(), stopLap + 1, totalLaps));
                    
                    allStrategies.add(s);
                }
            }
        }

        // =================================================================================
        // SCENARIO 2: DUE SOSTE (3 STINT)
        // Complessità: O(M^3 * L^2) - Computazionalmente più pesante, ma gestibile per L=50
        // =================================================================================
        for (TyrePrediction t1 : tyres) {
            for (TyrePrediction t2 : tyres) {
                for (TyrePrediction t3 : tyres) {

                    // REGOLA F1: Almeno 2 mescole diverse usate in totale.
                    // Esempio valido: Soft -> Medium -> Soft
                    // Esempio illegale: Soft -> Soft -> Soft
                    boolean distinct = !t1.getCompound().equals(t2.getCompound()) || !t2.getCompound().equals(t3.getCompound());
                    if (!distinct) continue;

                    // Loop nidificati per trovare i due punti di sosta.
                    // Usiamo step di 5 giri per ridurre drasticamente il numero di combinazioni da valutare.
                    
                    // Stop 1: tra il 15% e il 50% della gara
                    for (int stop1 = (int)(totalLaps * 0.15); stop1 < (int)(totalLaps * 0.5); stop1 += 5) {
                        
                        // Stop 2: deve essere almeno 15 giri dopo il primo e prima del 90% della gara
                        for (int stop2 = stop1 + 15; stop2 < (int)(totalLaps * 0.9); stop2 += 5) {
                            
                            // Calcolo tempi dei 3 stint
                            double time1 = calculateStintTime(t1, stop1);
                            double time2 = calculateStintTime(t2, stop2 - stop1);
                            double time3 = calculateStintTime(t3, totalLaps - stop2);

                            // Paghiamo 2 volte il costo del pit stop
                            double totalTime = time1 + PIT_STOP_LOSS + time2 + PIT_STOP_LOSS + time3;

                            RaceStrategy s = new RaceStrategy();
                            s.setTotalTime(totalTime);
                            s.setPitStops(2);
                            s.getStints().add(new Stint(t1.getCompound(), 1, stop1));
                            s.getStints().add(new Stint(t2.getCompound(), stop1 + 1, stop2));
                            s.getStints().add(new Stint(t3.getCompound(), stop2 + 1, totalLaps));
                            
                            allStrategies.add(s);
                        }
                    }
                }
            }
        }

        // =================================================================================
        // SELEZIONE DELLE MIGLIORI
        // =================================================================================
        
        // 1. Ordiniamo tutte le strategie trovate dalla più veloce alla più lenta
        allStrategies.sort(Comparator.comparingDouble(RaceStrategy::getTotalTime));

        // 2. (Opzionale) Filtro "Intelligente": rimuovere strategie troppo simili 
        // (es. sosta al giro 20 e sosta al giro 22 con stesse gomme).
        // Per ora prendiamo semplicemente le prime 3 assolute.

        System.out.println("Strategie calcolate totali: " + allStrategies.size());
        
        // Restituiamo solo le prime 3 (il podio delle strategie)
        return allStrategies.stream().limit(3).collect(Collectors.toList());
    }

    /**
     * Calcola il tempo totale necessario per percorrere N giri con una specifica gomma,
     * tenendo conto del degrado progressivo.
     * * Modello Matematico: Progressione Aritmetica
     * Tempo(giro i) = BaseTime + (Degrado * i)
     */
    private double calculateStintTime(TyrePrediction tyre, int laps) {
        double total = 0;
        double currentLapTime = tyre.getBase_time();
        
        for (int i = 0; i < laps; i++) {
            total += currentLapTime;
            // Al giro successivo la gomma è più lenta
            currentLapTime += tyre.getDegradation_rate();
        }
        return total;
    }
}