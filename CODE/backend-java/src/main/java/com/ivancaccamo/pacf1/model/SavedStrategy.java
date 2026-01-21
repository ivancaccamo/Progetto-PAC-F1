package com.ivancaccamo.pacf1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entità JPA che rappresenta una strategia di gara salvata nel database.
 * <p>
 * Questa classe mappa la tabella delle strategie archiviate. Viene utilizzata
 * per persistere i risultati delle simulazioni che l'utente decide di conservare
 * nello storico per consultazioni future.
 * </p>
 *
 * @author Team SPS-F1
 */
@Entity
public class SavedStrategy {

    /**
     * Identificativo univoco (Primary Key) della strategia salvata.
     * Generato automaticamente dal database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Il nome del circuito associato alla strategia (es. "Bahrain Grand Prix").
     */
    private String circuit;

    /**
     * Il tempo totale stimato di gara in secondi.
     */
    private double totalTime;

    /**
     * Il numero di soste ai box previste.
     */
    private int pitStops;

    /**
     * Una descrizione testuale sintetica della sequenza di stint.
     * Esempio: "Soft (18 giri) -> Medium (25 giri) -> Hard (14 giri)".
     * Utile per la visualizzazione rapida nell'archivio senza dover ricostruire l'oggetto completo.
     */
    private String stintsDescription;

    /**
     * Timestamp di creazione del record, formattato come stringa "dd-MM-yyyy HH:mm".
     */
    private String createdAt;

    /**
     * Costruttore vuoto richiesto dalle specifiche JPA.
     */
    public SavedStrategy() {
    }

    /**
     * Costruttore per creare una nuova istanza di strategia da salvare.
     *
     * @param circuit           Il nome del circuito.
     * @param totalTime         Il tempo totale in secondi.
     * @param pitStops          Il numero di soste.
     * @param stintsDescription La descrizione leggibile degli stint.
     */
    public SavedStrategy(String circuit, double totalTime, int pitStops, String stintsDescription) {
        this.circuit = circuit;
        this.totalTime = totalTime;
        this.pitStops = pitStops;
        this.stintsDescription = stintsDescription;
        this.createdAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    /**
     * Metodo di callback JPA eseguito prima della persistenza dell'entità.
     * <p>
     * Garantisce che il campo {@code createdAt} venga valorizzato con la data
     * e l'ora correnti se non è stato già impostato manualmente.
     * </p>
     */
    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        }
    }

    // ---------- GETTER & SETTER ----------

    /**
     * Restituisce l'ID del record.
     * @return L'identificativo univoco.
     */
    public Long getId() {
        return id;
    }

    /**
     * Imposta l'ID del record.
     * @param id L'identificativo univoco.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Restituisce il nome del circuito.
     * @return Il nome del circuito.
     */
    public String getCircuit() {
        return circuit;
    }

    /**
     * Imposta il nome del circuito.
     * @param circuit Il nome del circuito.
     */
    public void setCircuit(String circuit) {
        this.circuit = circuit;
    }

    /**
     * Restituisce il tempo totale di gara salvato.
     * @return Il tempo in secondi.
     */
    public double getTotalTime() {
        return totalTime;
    }

    /**
     * Imposta il tempo totale di gara.
     * @param totalTime Il tempo in secondi.
     */
    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * Restituisce il numero di pit-stop salvati.
     * @return Il numero di soste.
     */
    public int getPitStops() {
        return pitStops;
    }

    /**
     * Imposta il numero di pit-stop.
     * @param pitStops Il numero di soste.
     */
    public void setPitStops(int pitStops) {
        this.pitStops = pitStops;
    }

    /**
     * Restituisce la descrizione testuale degli stint.
     * @return La stringa descrittiva.
     */
    public String getStintsDescription() {
        return stintsDescription;
    }

    /**
     * Imposta la descrizione testuale degli stint.
     * @param stintsDescription La stringa descrittiva.
     */
    public void setStintsDescription(String stintsDescription) {
        this.stintsDescription = stintsDescription;
    }

    /**
     * Restituisce la data di creazione formattata.
     * @return La data come stringa.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Imposta la data di creazione.
     * @param createdAt La data formattata come stringa.
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}