package com.ivancaccamo.pacf1.model;

import java.util.List;

public class PredictionResponse {
    private String circuit;
    private List<TyrePrediction> predictions;

    // Costruttori, Getter, Setter per PredictionResponse
    public String getCircuit() { return circuit; }
    public void setCircuit(String circuit) { this.circuit = circuit; }
    public List<TyrePrediction> getPredictions() { return predictions; }
    public void setPredictions(List<TyrePrediction> predictions) { this.predictions = predictions; }

    public static class TyrePrediction {
        private String compound;
        private double base_time;
        private double degradation_rate;

        // Costruttori, Getter, Setter per TyrePrediction
        public String getCompound() { return compound; }
        public void setCompound(String compound) { this.compound = compound; }
        public double getBase_time() { return base_time; }
        public void setBase_time(double base_time) { this.base_time = base_time; }
        public double getDegradation_rate() { return degradation_rate; }
        public void setDegradation_rate(double degradation_rate) { this.degradation_rate = degradation_rate; }
    }
}