package com.ivancaccamo.pacf1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class SavedStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String circuit;
    private double totalTime;
    private int pitStops;
    private String stintsDescription;
    private String createdAt;

    public SavedStrategy() {
    }

    public SavedStrategy(String circuit, double totalTime, int pitStops, String stintsDescription) {
        this.circuit = circuit;
        this.totalTime = totalTime;
        this.pitStops = pitStops;
        this.stintsDescription = stintsDescription;
        this.createdAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        }
    }

    // ---------- GETTER & SETTER ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCircuit() {
        return circuit;
    }

    public void setCircuit(String circuit) {
        this.circuit = circuit;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public int getPitStops() {
        return pitStops;
    }

    public void setPitStops(int pitStops) {
        this.pitStops = pitStops;
    }

    public String getStintsDescription() {
        return stintsDescription;
    }

    public void setStintsDescription(String stintsDescription) {
        this.stintsDescription = stintsDescription;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
