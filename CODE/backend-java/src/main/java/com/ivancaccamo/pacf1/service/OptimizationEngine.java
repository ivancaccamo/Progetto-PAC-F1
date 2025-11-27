package com.ivancaccamo.pacf1.service;

import com.ivancaccamo.pacf1.model.PredictionResponse.TyrePrediction;
import com.ivancaccamo.pacf1.model.RaceStrategy;
import com.ivancaccamo.pacf1.model.Stint;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OptimizationEngine {

    private static final double PIT_STOP_LOSS = 20.0;
    private static final double INFINITY = 1e9; // Un numero grandissimo

    // Cache per la Memoization: salva i risultati parziali per non ricalcolarli
    // Chiave: "giroCorrente-mascheraGommeUsate" -> Valore: Miglior Tempo
    private Map<String, Double> memo = new HashMap<>();
    
    // Per ricostruire la strategia alla fine
    private Map<String, StintDecision> bestDecisions = new HashMap<>();

    public List<RaceStrategy> calculateTop3Strategies(int totalLaps, List<TyrePrediction> tyres) {
        System.out.println("--- AVVIO ALGORITMO DP (N SOSTE) - RICERCA ESAUSTIVA TOP 3 ---");
        
        List<RaceStrategy> candidates = new ArrayList<>();
        if (tyres.isEmpty()) return candidates;

        // Invece di chiedere subito "qual è il meglio assoluto",
        // proviamo manualmente ogni possibile PRIMO STINT (Mescola + Durata)
        // e chiediamo alla DP di risolvere ottimamente il RESTO della gara.
        
        for (int i = 0; i < tyres.size(); i++) {
            TyrePrediction startTyre = tyres.get(i);
            int startMask = (1 << i); // Maschera con la prima gomma usata

            // Proviamo diverse durate per il primo stint (es. da 1 giro fino a metà gara)
            // Usiamo uno step di 3 giri per avere varietà senza troppi calcoli
            int minStint = Math.max(2, (int)(totalLaps * 0.15));
            int maxStint = Math.max(minStint + 2, totalLaps - 2);
            int step = Math.max(1, totalLaps / 15); // Step adattivo

            for (int firstStintLaps = 1; firstStintLaps < totalLaps - 1; firstStintLaps++) {
                
                // IMPORTANTE: Puliamo la memoria per ogni tentativo, altrimenti riusa calcoli vecchi
                memo.clear();
                bestDecisions.clear();

                // Calcoliamo il costo del primo stint manuale
                double firstStintTime = calculateStintTime(startTyre, firstStintLaps);

                // Chiediamo alla DP: "Qual è il tempo minimo per finire la gara da qui in poi?"
                // Nota: solve() aggiungerà automaticamente il costo del pit stop iniziale
                double timeRest = solve(firstStintLaps, startMask, totalLaps, tyres);

                if (timeRest < INFINITY) {
                    // Ricostruiamo la strategia per la seconda parte
                    RaceStrategy restStrategy = reconstructStrategy(firstStintLaps, startMask, totalLaps, tyres);
                    
                    if (restStrategy != null) {
                        // Creiamo la strategia completa unendo Primo Stint + Resto
                        RaceStrategy fullStrategy = new RaceStrategy();
                        
                        // Tempo totale = Primo Stint + Resto (che include i pit stop successivi)
                        fullStrategy.setTotalTime(firstStintTime + timeRest);
                        
                        // Aggiungiamo il primo stint in testa alla lista
                        fullStrategy.getStints().add(new Stint(startTyre.getCompound(), 1, firstStintLaps));
                        fullStrategy.getStints().addAll(restStrategy.getStints());
                        
                        // Calcoliamo le soste (Numero di stint - 1)
                        fullStrategy.setPitStops(fullStrategy.getStints().size() - 1);

                        candidates.add(fullStrategy);
                    }
                }
            }
        }

        // 3. ORDINIAMO E FILTRIAMO
        // Ordiniamo per tempo totale crescente
        candidates.sort(Comparator.comparingDouble(RaceStrategy::getTotalTime));

        // Rimuoviamo duplicati (strategie con lo stesso tempo quasi identico)
        List<RaceStrategy> uniqueResults = new ArrayList<>();
        Set<Integer> seenTimes = new HashSet<>(); // Usiamo int per arrotondare e filtrare

        for (RaceStrategy s : candidates) {
            int timeInt = (int) s.getTotalTime();
            // Se non abbiamo già una strategia con questo tempo esatto, la aggiungiamo
            if (!seenTimes.contains(timeInt)) {
                uniqueResults.add(s);
                seenTimes.add(timeInt);
            }
            if (uniqueResults.size() >= 3) break; // Ci fermiamo a 3
        }

        return uniqueResults;
    }
    /**
     * FUNZIONE RICORSIVA CORE (DP)
     * Calcola il tempo minimo per finire la gara partendo da 'currentLap'.
     *
     * @param currentLap Giro attuale (inizio del prossimo stint)
     * @param usedTyresMask Bitmask che traccia quali mescole abbiamo già usato (bit 0=Soft, 1=Medium, 2=Hard)
     */
    private double solve(int currentLap, int usedTyresMask, int totalLaps, List<TyrePrediction> tyres) {
        // CASO BASE: Gara finita
        if (currentLap == totalLaps) {
            // Controlliamo la regola: abbiamo usato almeno 2 mescole diverse?
            // Contiamo i bit a 1 nella maschera
            if (Integer.bitCount(usedTyresMask) >= 2) {
                return 0; // Costo 0 per finire (abbiamo già finito)
            } else {
                return INFINITY; // Strategia illegale, penalità infinita
            }
        }

        // MEMOIZATION: Se abbiamo già calcolato questo stato, restituiamo il valore salvato
        String stateKey = currentLap + "-" + usedTyresMask;
        if (memo.containsKey(stateKey)) {
            return memo.get(stateKey);
        }

        double minTime = INFINITY;
        StintDecision bestDecision = null;

        // PROVIAMO TUTTE LE POSSIBILI MOSSE (Next Stint)
        // Iteriamo su ogni mescola disponibile
        for (int i = 0; i < tyres.size(); i++) {
            TyrePrediction tyre = tyres.get(i);
            
            // Maschera aggiornata se usiamo questa gomma (1 << i accende il bit i-esimo)
            int nextMask = usedTyresMask | (1 << i);

            // Proviamo tutte le lunghezze possibili per questo stint
            // Minimo 5 giri, massimo fino alla fine della gara
            for (int laps = 10; laps <= (totalLaps - currentLap); laps += 1) { // Step 1 per precisione massima
                
                int nextLap = currentLap + laps;
                
                // Costo = Tempo guida + Pit Stop (se non è la partenza)
                double driveTime = calculateStintTime(tyre, laps);
                double pitCost = (currentLap == 0) ? 0 : PIT_STOP_LOSS;
                
                // RICORSIONE: Tempo totale = costo attuale + costo migliore dal prossimo giro in poi
                double timeToFinish = solve(nextLap, nextMask, totalLaps, tyres);
                double totalTime = driveTime + pitCost + timeToFinish;

                if (totalTime < minTime) {
                    minTime = totalTime;
                    // Memorizziamo la decisione presa per ricostruire il percorso dopo
                    bestDecision = new StintDecision(tyre.getCompound(), laps, nextMask);
                }
            }
        }

        // Salviamo il risultato in memoria
        memo.put(stateKey, minTime);
        if (bestDecision != null) {
            bestDecisions.put(stateKey, bestDecision);
        }

        return minTime;
    }

    // Ricostruisce la strategia seguendo le "briciole di pane" lasciate dalla DP
    private RaceStrategy reconstructStrategy(int startLap, int startMask, int totalLaps, List<TyrePrediction> tyres) {
        RaceStrategy strategy = new RaceStrategy();
        int currentLap = startLap;
        int mask = startMask;
        int stops = -1; 
        
        // Se partiamo da metà gara (es. per le alternative), i pit stop vanno contati diversamente
        if (startLap > 0) stops = 0; 

        while (currentLap < totalLaps) {
            String key = currentLap + "-" + mask;
            StintDecision decision = bestDecisions.get(key);
            
            if (decision == null) return null; 

            strategy.getStints().add(new Stint(decision.compound, currentLap + 1, currentLap + decision.laps));
            
            stops++;
            currentLap += decision.laps;
            mask = decision.nextMask;
        }
        
        strategy.setPitStops(stops);
        
        // Recuperiamo il tempo totale specifico per QUESTA strategia
        // Usiamo la chiave di partenza passata come argomento
        String startKey = startLap + "-" + startMask;
        Double totalTime = memo.get(startKey);
        strategy.setTotalTime(totalTime != null ? totalTime : 0.0);
        
        return strategy;
    }

    private double calculateStintTime(TyrePrediction tyre, int laps) {
        double total = 0;
        double currentLapTime = tyre.getBase_time();
        for (int i = 0; i < laps; i++) {
            total += currentLapTime;
            currentLapTime += tyre.getDegradation_rate();
        }
        return total;
    }

    // Classe helper interna per salvare le decisioni
    private static class StintDecision {
        String compound;
        int laps;
        int nextMask;

        public StintDecision(String compound, int laps, int nextMask) {
            this.compound = compound;
            this.laps = laps;
            this.nextMask = nextMask;
        }
    }
}