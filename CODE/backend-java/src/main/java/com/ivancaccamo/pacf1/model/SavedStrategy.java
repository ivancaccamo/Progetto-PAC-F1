package com.ivancaccamo.pacf1.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Data
public class SavedStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String circuit;
    private double totalTime;
    private int pitStops;
    private String stintsDescription; 
    private String createdAt;

    public SavedStrategy() {}

    public SavedStrategy(String circuit, double totalTime, int pitStops, String stintsDescription) {
        this.circuit = circuit;
        this.totalTime = totalTime;
        this.pitStops = pitStops;
        this.stintsDescription = stintsDescription;
        this.createdAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    // 🎯 NUOVO: se non usi il costruttore "utile", qui settiamo comunque createdAt
    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        }
    }
}
