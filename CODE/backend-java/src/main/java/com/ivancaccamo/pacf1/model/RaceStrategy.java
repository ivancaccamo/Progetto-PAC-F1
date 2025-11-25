package com.ivancaccamo.pacf1.model;

import java.util.ArrayList;
import java.util.List;

public class RaceStrategy {
    private double totalTime;
    private int pitStops;
    private List<Stint> stints = new ArrayList<>();

    // Costruttore vuoto
    public RaceStrategy() {}

    // Getters e Setters
    public double getTotalTime() { return totalTime; }
    public void setTotalTime(double totalTime) { this.totalTime = totalTime; }

    public int getPitStops() { return pitStops; }
    public void setPitStops(int pitStops) { this.pitStops = pitStops; }

    public List<Stint> getStints() { return stints; }
    public void setStints(List<Stint> stints) { this.stints = stints; }
}