package com.ivancaccamo.pacf1.model;

/**
 * Rappresenta un singolo stint di gara.
 * <p>
 * Uno stint è definito come una sequenza continua di giri percorsi con lo stesso
 * set di pneumatici. Questa classe traccia quale mescola viene utilizzata,
 * il giro di inizio, il giro di fine e la durata totale in giri.
 * È l'unità fondamentale che compone una {@link RaceStrategy}.
 * </p>
 *
 * @author Team SPS-F1
 */
public class Stint {

    /**
     * Il nome o codice della mescola utilizzata (es. "SOFT", "MEDIUM", "HARD", "C3").
     */
    private String compound;

    /**
     * Il numero del giro in cui inizia lo stint (es. dopo un pit-stop o alla partenza).
     */
    private int startLap;

    /**
     * Il numero del giro in cui termina lo stint (es. rientro ai box o bandiera a scacchi).
     */
    private int endLap;

    /**
     * La durata dello stint espressa in numero di giri percorsi.
     */
    private int laps;

    /**
     * Costruttore vuoto.
     * Necessario per la serializzazione/deserializzazione JSON e per l'uso con framework.
     */
    // Costruttore vuoto
    public Stint() {}

    /**
     * Costruttore completo per inizializzare un nuovo stint.
     * <p>
     * Calcola automaticamente il numero di giri (`laps`) basandosi sulla differenza
     * tra il giro di fine e quello di inizio.
     * </p>
     *
     * @param compound La mescola utilizzata.
     * @param startLap Il giro di inizio.
     * @param endLap   Il giro di fine.
     */
    // Costruttore completo
    public Stint(String compound, int startLap, int endLap) {
        this.compound = compound;
        this.startLap = startLap;
        this.endLap = endLap;
        this.laps = endLap - startLap + 1;
    }

    // Getters e Setters

    /**
     * Restituisce la mescola dello stint.
     * @return Il nome della mescola.
     */
    public String getCompound() { return compound; }

    /**
     * Imposta la mescola dello stint.
     * @param compound Il nome della mescola.
     */
    public void setCompound(String compound) { this.compound = compound; }

    /**
     * Restituisce il giro di inizio dello stint.
     * @return Il numero del giro.
     */
    public int getStartLap() { return startLap; }

    /**
     * Imposta il giro di inizio.
     * @param startLap Il numero del giro.
     */
    public void setStartLap(int startLap) { this.startLap = startLap; }

    /**
     * Restituisce il giro di fine dello stint.
     * @return Il numero del giro.
     */
    public int getEndLap() { return endLap; }

    /**
     * Imposta il giro di fine.
     * @param endLap Il numero del giro.
     */
    public void setEndLap(int endLap) { this.endLap = endLap; }

    /**
     * Restituisce la durata totale dello stint.
     * @return Il numero di giri.
     */
    public int getLaps() { return laps; }

    /**
     * Imposta la durata dello stint.
     * @param laps Il numero di giri.
     */
    public void setLaps(int laps) { this.laps = laps; }
}