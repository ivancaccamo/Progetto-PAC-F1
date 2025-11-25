package com.ivancaccamo.pacf1.model;

import java.util.List;

public class PredictionRequest {
    private String circuit_name;
    private double air_temp;
    private double track_temp;
    private List<String> compounds;

    // COSTRUTTORE VUOTO (Necessario per Spring/Jackson)
    public PredictionRequest() {
    }

    // COSTRUTTORE CON TUTTI GLI ARGOMENTI (Quello che stavi provando a usare)
    public PredictionRequest(String circuit_name, double air_temp, double track_temp, List<String> compounds) {
        this.circuit_name = circuit_name;
        this.air_temp = air_temp;
        this.track_temp = track_temp;
        this.compounds = compounds;
    }

    // GETTERS & SETTERS (Generati a mano per sicurezza)
    public String getCircuit_name() { return circuit_name; }
    public void setCircuit_name(String circuit_name) { this.circuit_name = circuit_name; }

    public double getAir_temp() { return air_temp; }
    public void setAir_temp(double air_temp) { this.air_temp = air_temp; }

    public double getTrack_temp() { return track_temp; }
    public void setTrack_temp(double track_temp) { this.track_temp = track_temp; }

    public List<String> getCompounds() { return compounds; }
    public void setCompounds(List<String> compounds) { this.compounds = compounds; }
}