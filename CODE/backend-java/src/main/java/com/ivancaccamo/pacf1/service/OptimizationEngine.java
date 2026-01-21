package com.ivancaccamo.pacf1.service;

import com.ivancaccamo.pacf1.model.PredictionResponse.TyrePrediction;
import com.ivancaccamo.pacf1.model.RaceStrategy;
import com.ivancaccamo.pacf1.model.Stint;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Motore algoritmico principale del sistema SPS-F1.
 * <p>
 * Questa classe implementa la logica di ottimizzazione per determinare la strategia di gara ideale.
 * Utilizza un approccio di <b>Programmazione Dinamica (DP)</b> con Memoization per esplorare
 * l'albero delle possibili decisioni (mescola, lunghezza stint) e trovare il cammino minimo
 * in termini di tempo totale.
 * </p>
 * <p>
 * L'algoritmo tiene conto del degrado degli pneumatici, del tempo perso in pit-lane e
 * dei vincoli regolamentari (obbligo di usare almeno due mescole diverse).
 * </p>
 *
 * @author Team SPS-F1
 */
@Service
public class OptimizationEngine {

    /**
     * Tempo medio perso per effettuare un pit-stop (percorrenza pit-lane + cambio gomme).
     * Valore fisso stimato a 20.0 secondi.
     */
    private static final double PIT_STOP_LOSS = 20.0;

    /**
     * Valore sentinella per indicare un tempo o un costo infinito (strategia non valida).
     */
    private static final double INFINITY = 1e9; // Un numero grandissimo

    /**
     * Cache per la Memoization.
     * Mappa uno stato univoco (giro corrente + maschera gomme usate) al miglior tempo ottenibile da quello stato in poi.
     * Chiave: "giroCorrente-mascheraGommeUsate" -> Valore: Miglior Tempo
     */
    // Cache per la Memoization: salva i risultati parziali per non ricalcolarli
    // Chiave: "giroCorrente-mascheraGommeUsate" -> Valore: Miglior Tempo
    private Map<String, Double> memo = new HashMap<>();
    
    /**
     * Mappa utilizzata per tracciare le decisioni ottimali prese ad ogni passo.
     * Fondamentale per la fase di backtracking che ricostruisce la lista degli stint finali.
     */
    // Per ricostruire la strategia alla fine
    private Map<String, StintDecision> bestDecisions = new HashMap<>();

    /**
     * Metodo principale per il calcolo delle strategie.
     * <p>
     * Esegue una ricerca esaustiva intelligente per trovare le Top 3 strategie migliori.
     * Invece di lanciare una singola esecuzione DP, itera su tutte le possibili combinazioni
     * di "Primo Stint" (mescola iniziale e durata) e delega alla DP l'ottimizzazione del resto della gara.
     * Questo approccio ibrido permette di diversificare i risultati e trovare alternative valide
     * (es. strategia a 1 sosta vs 2 soste).
     * </p>
     *
     * @param totalLaps Il numero totale di giri della gara.
     * @param tyres     La lista delle predizioni di degrado per le mescole disponibili.
     * @return Una lista contenente le migliori 3 strategie uniche, ordinate per tempo totale crescente.
     */
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
     * FUNZIONE RICORSIVA CORE (DP).
     * <p>
     * Questo metodo implementa il cuore dell'algoritmo di Programmazione Dinamica.
     * Calcola il tempo minimo necessario per completare la gara partendo da uno stato specifico
     * definito dal giro corrente e dalle mescole già utilizzate.
     * </p>
     * <p>
     * Utilizza la tecnica della <b>Memoization</b>: prima di calcolare una soluzione, controlla
     * se lo stato (currentLap, usedTyresMask) è già stato risolto e salvato nella mappa {@code memo}.
     * Se è un nuovo stato, esplora tutte le possibili decisioni future (quale gomma montare e per quanti giri),
     * sceglie quella che minimizza il tempo totale e salva la decisione migliore.
     * </p>
     *
     * @param currentLap    Il giro attuale da cui inizia il prossimo stint (stato temporale).
     * @param usedTyresMask Una bitmask intera che traccia lo storico delle mescole usate.
     * (es. bit 0 = Soft, bit 1 = Medium, bit 2 = Hard).
     * Indispensabile per verificare il regolamento delle due mescole.
     * @param totalLaps     Il numero totale di giri della gara.
     * @param tyres         La lista delle predizioni disponibili per le gomme.
     * @return Il tempo minimo stimato in secondi per arrivare alla fine della gara da questo punto,
     * oppure {@code INFINITY} se non esiste una strategia valida.
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

    /**
     * Ricostruisce la strategia ottinale completa a partire dai dati salvati.
     * <p>
     * Una volta che il metodo {@code solve()} ha popolato la mappa {@code bestDecisions},
     * questo metodo "naviga" attraverso le decisioni migliori salvate per trasformarle
     * in una lista ordinata di oggetti {@link Stint} comprensibile per l'utente.
     * </p>
     *
     * @param startLap  Il giro di partenza della ricostruzione.
     * @param startMask La maschera delle gomme iniziale.
     * @param totalLaps Il numero totale di giri.
     * @param tyres     Le informazioni sulle gomme.
     * @return Un oggetto {@link RaceStrategy} completo, o {@code null} se il percorso è interrotto.
     */
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

    /**
     * Calcola il tempo totale impiegato per percorrere un certo numero di giri con una specifica gomma.
     * <p>
     * Simula il degrado giro per giro: al tempo base viene aggiunto il tasso di degrado
     * accumulato per ogni giro successivo al primo.
     * Modello lineare: Tempo(giro i) = BaseTime + (i * DegradationRate).
     * </p>
     *
     * @param tyre La predizione della gomma (contiene base time e degradation rate).
     * @param laps Il numero di giri da percorrere nello stint.
     * @return Il tempo totale in secondi.
     */
    private double calculateStintTime(TyrePrediction tyre, int laps) {
        double total = 0;
        double currentLapTime = tyre.getBase_time();
        for (int i = 0; i < laps; i++) {
            total += currentLapTime;
            currentLapTime += tyre.getDegradation_rate();
        }
        return total;
    }

    /**
     * Classe helper interna (DTO) per memorizzare una decisione ottimale nel grafo DP.
     * <p>
     * Salva quale gomma è stata scelta, per quanti giri e quale sarà la prossima bitmask,
     * permettendo al metodo {@code reconstructStrategy} di ripercorrere il cammino minimo.
     * </p>
     */
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