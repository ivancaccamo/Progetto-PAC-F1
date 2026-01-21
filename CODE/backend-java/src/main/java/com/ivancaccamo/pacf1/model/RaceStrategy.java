package com.ivancaccamo.pacf1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una strategia di gara completa calcolata dal sistema.
 * <p>
 * Questa classe è il risultato finale dell'algoritmo di ottimizzazione.
 * Contiene la sequenza ordinata degli stint (frazioni di gara) da percorrere,
 * il numero totale di soste previste e il tempo complessivo stimato per completare la gara.
 * </p>
 *
 * @author Team SPS-F1
 */
public class RaceStrategy {

    /**
     * Il tempo totale stimato per completare la gara seguendo questa strategia.
     * Include i tempi di percorrenza in pista e i tempi persi in pit-lane.
     */
    private double totalTime;

    /**
     * Il numero totale di pit-stop previsti in questa strategia.
     */
    private int pitStops;

    /**
     * La lista sequenziale degli stint che compongono la strategia.
     * Ogni stint definisce la mescola da usare e il numero di giri da percorrere.
     */
    private List<Stint> stints = new ArrayList<>();

    /**
     * Costruttore vuoto di default.
     * Inizializza la lista degli stint come un ArrayList vuoto.
     */
    // Costruttore vuoto
    public RaceStrategy() {}

    // Getters e Setters

    /**
     * Restituisce il tempo totale di gara previsto.
     * @return Il tempo in secondi.
     */
    public double getTotalTime() { return totalTime; }

    /**
     * Imposta il tempo totale di gara.
     * @param totalTime Il tempo in secondi.
     */
    public void setTotalTime(double totalTime) { this.totalTime = totalTime; }

    /**
     * Restituisce il numero di soste ai box.
     * @return Il numero di pit-stop.
     */
    public int getPitStops() { return pitStops; }

    /**
     * Imposta il numero di soste ai box.
     * @param pitStops Il numero di pit-stop.
     */
    public void setPitStops(int pitStops) { this.pitStops = pitStops; }

    /**
     * Restituisce la lista degli stint.
     * @return Una lista di oggetti {@link Stint}.
     */
    public List<Stint> getStints() { return stints; }

    /**
     * Imposta la lista degli stint della strategia.
     * @param stints La lista di stint.
     */
    public void setStints(List<Stint> stints) { this.stints = stints; }
}