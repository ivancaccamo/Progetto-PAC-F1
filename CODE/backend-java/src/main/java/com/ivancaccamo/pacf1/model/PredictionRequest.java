package com.ivancaccamo.pacf1.model;

import java.util.List;

/**
 * Data Transfer Object (DTO) utilizzato per incapsulare i parametri di input
 * da inviare al servizio di Machine Learning per la predizione del degrado.
 * <p>
 * Questa classe mappa la struttura JSON attesa dall'endpoint Python, contenente
 * le informazioni sul circuito, le condizioni meteo e le mescole da analizzare.
 * </p>
 *
 * @author Team SPS-F1
 */
public class PredictionRequest {

    /**
     * Il nome identificativo del circuito (es. "Bahrain Grand Prix", "Monza").
     * Deve corrispondere ai nomi utilizzati nel dataset di training.
     */
    private String circuit_name;

    /**
     * La temperatura dell'aria in gradi Celsius al momento della gara.
     */
    private double air_temp;

    /**
     * La temperatura dell'asfalto in gradi Celsius.
     * Questo è un parametro critico che influenza direttamente il degrado termico della gomma.
     */
    private double track_temp;

    /**
     * La lista delle mescole di pneumatici disponibili per la gara
     * (es. ["C1", "C2", "C3"] oppure ["SOFT", "MEDIUM", "HARD"]).
     */
    private List<String> compounds;

    /**
     * Costruttore di default senza argomenti.
     * <p>
     * Necessario per consentire la deserializzazione automatica da JSON
     * da parte di framework come Jackson (utilizzato da Spring Boot).
     * </p>
     */
    // COSTRUTTORE VUOTO (Necessario per Spring/Jackson)
    public PredictionRequest() {
    }

    /**
     * Costruttore completo per l'inizializzazione manuale dell'oggetto.
     * Utile in fase di test o quando l'oggetto viene creato internamente dal backend.
     *
     * @param circuit_name Il nome del circuito.
     * @param air_temp     La temperatura dell'aria.
     * @param track_temp   La temperatura della pista.
     * @param compounds    La lista delle mescole da testare.
     */
    // COSTRUTTORE CON TUTTI GLI ARGOMENTI (Quello che stavi provando a usare)
    public PredictionRequest(String circuit_name, double air_temp, double track_temp, List<String> compounds) {
        this.circuit_name = circuit_name;
        this.air_temp = air_temp;
        this.track_temp = track_temp;
        this.compounds = compounds;
    }

    // GETTERS & SETTERS (Generati a mano per sicurezza)

    /**
     * Restituisce il nome del circuito impostato per la richiesta.
     * @return Il nome del circuito.
     */
    public String getCircuit_name() { return circuit_name; }

    /**
     * Imposta il nome del circuito.
     * @param circuit_name Il nome del circuito.
     */
    public void setCircuit_name(String circuit_name) { this.circuit_name = circuit_name; }

    /**
     * Restituisce la temperatura dell'aria.
     * @return Temperatura in °C.
     */
    public double getAir_temp() { return air_temp; }

    /**
     * Imposta la temperatura dell'aria.
     * @param air_temp Temperatura in °C.
     */
    public void setAir_temp(double air_temp) { this.air_temp = air_temp; }

    /**
     * Restituisce la temperatura della pista.
     * @return Temperatura in °C.
     */
    public double getTrack_temp() { return track_temp; }

    /**
     * Imposta la temperatura della pista.
     * @param track_temp Temperatura in °C.
     */
    public void setTrack_temp(double track_temp) { this.track_temp = track_temp; }

    /**
     * Restituisce la lista delle mescole oggetto della predizione.
     * @return Lista di stringhe rappresentanti le mescole.
     */
    public List<String> getCompounds() { return compounds; }

    /**
     * Imposta la lista delle mescole da analizzare.
     * @param compounds Lista di mescole.
     */
    public void setCompounds(List<String> compounds) { this.compounds = compounds; }
}