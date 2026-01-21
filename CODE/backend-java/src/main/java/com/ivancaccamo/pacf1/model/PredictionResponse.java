package com.ivancaccamo.pacf1.model;

import java.util.List;

/**
 * Data Transfer Object (DTO) che incapsula la risposta del servizio di Machine Learning.
 * <p>
 * Questa classe viene utilizzata per deserializzare il JSON restituito dal microservizio Python
 * dopo una richiesta di predizione. Contiene le informazioni generali sul circuito
 * e una lista di parametri fisici predetti per ciascuna mescola disponibile.
 * </p>
 *
 * @author Team SPS-F1
 */
public class PredictionResponse {
    private String circuit;
    private List<TyrePrediction> predictions;

    // Costruttori, Getter, Setter per PredictionResponse

    /**
     * Restituisce il nome del circuito a cui si riferiscono le predizioni.
     * @return Il nome del circuito (es. "Monza").
     */
    public String getCircuit() { return circuit; }

    /**
     * Imposta il nome del circuito.
     * @param circuit Il nome del circuito.
     */
    public void setCircuit(String circuit) { this.circuit = circuit; }

    /**
     * Restituisce la lista delle predizioni per le diverse mescole.
     * @return Una lista di oggetti {@link TyrePrediction}.
     */
    public List<TyrePrediction> getPredictions() { return predictions; }

    /**
     * Imposta la lista delle predizioni delle mescole.
     * @param predictions La lista di predizioni.
     */
    public void setPredictions(List<TyrePrediction> predictions) { this.predictions = predictions; }

    /**
     * Classe interna statica che rappresenta i parametri fisici predetti
     * per una singola mescola di pneumatici.
     * <p>
     * Contiene i due valori chiave necessari all'algoritmo di ottimizzazione:
     * il tempo base (performance pura) e il tasso di degrado (usura).
     * </p>
     */
    public static class TyrePrediction {
        private String compound;
        private double base_time;
        private double degradation_rate;

        // Costruttori, Getter, Setter per TyrePrediction

        /**
         * Restituisce il tipo di mescola (es. "SOFT", "MEDIUM", "HARD" o codici "C1"-"C5").
         * @return La stringa identificativa della mescola.
         */
        public String getCompound() { return compound; }

        /**
         * Imposta il tipo di mescola.
         * @param compound Il nome della mescola.
         */
        public void setCompound(String compound) { this.compound = compound; }

        /**
         * Restituisce il tempo sul giro base stimato ($T_{base}$).
         * <p>
         * Rappresenta il tempo ideale che la vettura effettuerebbe con questa gomma
         * nuova (usura 0), date le condizioni meteo attuali.
         * </p>
         * @return Il tempo base in secondi.
         */
        public double getBase_time() { return base_time; }

        /**
         * Imposta il tempo sul giro base.
         * @param base_time Il tempo in secondi.
         */
        public void setBase_time(double base_time) { this.base_time = base_time; }

        /**
         * Restituisce il tasso di degrado previsto ($D_{rate}$).
         * <p>
         * Indica quanti secondi di prestazione vengono persi per ogni giro percorso.
         * Un valore più alto indica un'usura più rapida.
         * </p>
         * @return Il fattore di degrado (secondi/giro).
         */
        public double getDegradation_rate() { return degradation_rate; }

        /**
         * Imposta il tasso di degrado.
         * @param degradation_rate Il degrado in secondi per giro.
         */
        public void setDegradation_rate(double degradation_rate) { this.degradation_rate = degradation_rate; }
    }
}