package com.ivancaccamo.pacf1.model;

public class Stint {
    private String compound;
    private int startLap;
    private int endLap;
    private int laps;

    // Costruttore vuoto
    public Stint() {}

    // Costruttore completo
    public Stint(String compound, int startLap, int endLap) {
        this.compound = compound;
        this.startLap = startLap;
        this.endLap = endLap;
        this.laps = endLap - startLap + 1;
    }

    // Getters e Setters
    public String getCompound() { return compound; }
    public void setCompound(String compound) { this.compound = compound; }

    public int getStartLap() { return startLap; }
    public void setStartLap(int startLap) { this.startLap = startLap; }

    public int getEndLap() { return endLap; }
    public void setEndLap(int endLap) { this.endLap = endLap; }

    public int getLaps() { return laps; }
    public void setLaps(int laps) { this.laps = laps; }
}