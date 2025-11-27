package com.ivancaccamo.pacf1.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity // Dice a Java: "Questa è una tabella del database"
@Data
public class SavedStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String circuit;
    private double totalTime;
    private int pitStops;
    
    // Salviamo la sequenza gomme come testo semplice per comodità
    // Es: "SOFT (20) -> HARD (33)"
    private String stintsDescription; 
    
    private String createdAt;

    // Costruttore vuoto obbligatorio per JPA
    public SavedStrategy() {}

    // Costruttore utile
    public SavedStrategy(String circuit, double totalTime, int pitStops, String stintsDescription) {
        this.circuit = circuit;
        this.totalTime = totalTime;
        this.pitStops = pitStops;
        this.stintsDescription = stintsDescription;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}