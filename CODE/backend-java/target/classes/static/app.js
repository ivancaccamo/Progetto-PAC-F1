/**
 * CONFIGURAZIONE ASSETS E UTILITY UI
 * In questo file gestiamo il mapping statico delle immagini dei circuiti
 * e la logica per la modale di conferma eliminazione (UX).
 */

// Mapping circuiti a immagini
// Dizionario che associa il nome del GP (esatto, come arriva dal Backend/Select)
// al nome del file immagine presente nella cartella resources/static/images.
const circuitData = {

    'Bahrain Grand Prix': {
        image: 'Bahrain_Circuit.avif'
    },
    'Saudi Arabian Grand Prix': {
        image: 'Jeddah_Circuit.avif'
    },
    'Australian Grand Prix': {
        image: 'Australia_Circuit.avif'
    },
    'Japanese Grand Prix': {
        image: 'Japan_Circuit.avif'
    },
    'Chinese Grand Prix': {
        image: 'China_Circuit.avif'   // se il file si chiama "Cina_Circuit.avif" cambia qui
    },
    'Miami Grand Prix': {
        image: 'Miami_Circuit.avif'
    },
    'Emilia Romagna Grand Prix': {
        image: 'Emilia_Romagna_Circuit.avif'
    },
    'Monaco Grand Prix': {
        image: 'Monaco_Circuit.avif'
    },
    'Canadian Grand Prix': {
        image: 'Canada_Circuit.avif'
    },
    'Spanish Grand Prix': {
        image: 'Barcellona_Circuit.avif'   // come avevi già messo tu
    },
    'Austrian Grand Prix': {
        image: 'Austria_Circuit.avif'
    },
    'British Grand Prix': {
        image: 'Great_Britain_Circuit.avif'
    },
    'Hungarian Grand Prix': {
        image: 'Hungary_Circuit.avif'
    },
    'Belgian Grand Prix': {
        image: 'Belgium_Circuit.avif'
    },
    'Dutch Grand Prix': {
        image: 'Netherlands_Circuit.avif'
    },
    'Italian Grand Prix': {
        image: 'Monza_Circuit.avif'
    },
    'Azerbaijan Grand Prix': {
        image: 'Baku_Circuit.avif'
    },
    'Singapore Grand Prix': {
        image: 'Singapore_Circuit.avif'
    },
    'United States Grand Prix': {
        image: 'USA_Circuit.avif'
    },
    'Mexico City Grand Prix': {
        image: 'Mexico_Circuit.avif'
    },
    'São Paulo Grand Prix': {
        image: 'Brazil_Circuit.avif'
    },
    'Las Vegas Grand Prix': {
        image: 'Las_Vegas_Circuit.avif'
    },
    'Qatar Grand Prix': {
        image: 'Qatar_Circuit.avif'
    },
    'Abu Dhabi Grand Prix': {
        image: 'Abu_Dhabi_Circuit.avif'
    }

};

// === Gestione popup custom di conferma eliminazione ===

// Variabile di appoggio per memorizzare quale riga stiamo per cancellare.
// Serve perché il bottone "Elimina" è nella tabella, ma il bottone "Conferma" è nella modale,
// quindi dobbiamo passare i dati da una parte all'altra.
let deleteContext = { row: null, id: null };

document.addEventListener('DOMContentLoaded', () => {
    // Cerchiamo il bottone rosso "Conferma Eliminazione" dentro la modale Bootstrap
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    if (!confirmBtn) return;

    confirmBtn.addEventListener('click', async () => {
        // Recuperiamo i dati salvati quando l'utente ha cliccato il cestino
        const { row, id } = deleteContext;
        if (!id) return;

        try {
            // Chiamata DELETE al nostro controller Java
            const delResp = await fetch(`/api/history/${id}`, {
                method: 'DELETE'
            });

            if (delResp.ok) {
                // Se il backend conferma l'eliminazione (status 200),
                // rimuoviamo visivamente la riga dalla tabella HTML per evitare un refresh della pagina.
                if (row && row.parentNode) {
                    row.parentNode.removeChild(row);
                }

                // Resettiamo il contesto per evitare errori futuri
                deleteContext = { row: null, id: null };

                // Chiudiamo programmaticamente la modale usando le API di Bootstrap
                const modalEl = document.getElementById('confirmDeleteModal');
                const modalInstance = bootstrap.Modal.getInstance(modalEl);
                if (modalInstance) modalInstance.hide();
            } else {
                alert("Errore durante l'eliminazione.");
            }
        } catch (err) {
            console.error(err);
            alert("Errore di connessione durante l'eliminazione.");
        }
    });
});

/**
 * LOGICA DI CALCOLO E VISUALIZZAZIONE STRATEGIE
 * Gestisce l'interazione con il form di input, la chiamata al backend per il calcolo
 * delle strategie e il rendering dinamico dei risultati nella pagina.
 */

// funzione helper per aprire la modale di conferma
// Utilizzata quando si clicca sul cestino nell'archivio. Imposta il contesto globale
// (riga e ID) e mostra il popup Bootstrap.
function showDeleteConfirm(rowEl, id) {
    deleteContext.row = rowEl;
    deleteContext.id = id;

    const modalEl = document.getElementById('confirmDeleteModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
}

// Aggiorna i valori dei range slider in tempo reale
// Permette all'utente di vedere subito il valore numerico mentre trascina la barra.
document.getElementById('trackTemp').oninput = function() {
    document.getElementById('trackTempVal').innerText = this.value + "°C";
}
document.getElementById('airTemp').oninput = function() {
    document.getElementById('airTempVal').innerText = this.value + "°C";
}

/**
 * Funzione principale attivata dal pulsante "CALCOLA STRATEGIA".
 * Esegue la validazione dei dati, chiama l'API Java e gestisce gli stati di caricamento/errore.
 */
async function calculateStrategy() {
    const loading = document.getElementById('loading');
    const container = document.getElementById('strategiesContainer');

    // NASCONDI IL BOTTONE E IL GRAFICO ALL'INIZIO DI UN NUOVO CALCOLO
    // Reset dell'interfaccia per evitare di mostrare dati vecchi durante il nuovo calcolo.
    document.getElementById('btnToggleChart').classList.add('d-none');
    document.getElementById('chartContainer').classList.add('d-none');
    document.getElementById('chartContainer').classList.remove('show');
    
    // Pulisci risultati precedenti
    container.innerHTML = '';

    // Recupero valori dal form
    const circuit = document.getElementById('circuitSelect').value;
    const laps = parseInt(document.getElementById('lapsInput').value); // Convertiamo in numero intero
    const track = document.getElementById('trackTemp').value;
    const air = document.getElementById('airTemp').value;

    // --- CONTROLLO CIRCUITO MANCANTE ---
    // Se l'utente non ha selezionato nulla, mostriamo un Toast di avviso invece del semplice alert.
    if (!circuit || circuit === "") {
        // Nascondi loading se era attivo
        if(typeof loading !== 'undefined') loading.classList.add('d-none');

        const toastEl = document.getElementById('circuitToast');
        // Mostra il toast per 4 secondi
        const toast = new bootstrap.Toast(toastEl, { delay: 4000 }); 
        toast.show();
        
        return; // Ferma tutto
    }
    

    // --- CONTROLLO VALIDITÀ GIRI ---
    // Le gare troppo brevi (< 15 giri) non hanno senso strategico. Blocchiamo e avvisiamo.
    if (laps < 15) {
        if(typeof loading !== 'undefined') loading.classList.add('d-none'); // Safe check
        
        const toastEl = document.getElementById('errorToast');
        
        // Inizializza con opzioni: animazione attiva, nascondi dopo 5s
        const toast = new bootstrap.Toast(toastEl, { delay: 5000 }); 
        toast.show();
        
        return;
    }

    // Mostra spinner di caricamento
    loading.classList.remove('d-none');

    try {
        // Chiamata API al Backend Java
        // Passiamo i parametri in query string (GET)
        const response = await fetch(`/api/strategy?circuit=${encodeURIComponent(circuit)}&laps=${laps}&airTemp=${air}&trackTemp=${track}`);
        
        if (!response.ok) throw new Error("Errore API Java");
        
        const strategies = await response.json();
        
        if (strategies.length === 0) {
            container.innerHTML = '<div class="alert alert-warning">Nessuna strategia trovata. Riprova con parametri diversi.</div>';
        } else {
            // Renderizza le card delle strategie
            renderStrategies(strategies, laps);
            
            // MOSTRA IL BOTTONE DEL GRAFICO
            // Ora che abbiamo i dati, abilitiamo il pulsante per vedere la telemetria
            document.getElementById('btnToggleChart').classList.remove('d-none');
            
            // Genera il grafico (ma rimane nascosto finché non clicchi)
            drawStrategyChart(strategies, laps);
        }

    } catch (error) {
        console.error("Errore:", error);
        container.innerHTML = `<div class="alert alert-danger">Errore di comunicazione col server: ${error.message}</div>`;
    } finally {
        // Nascondi spinner indipendentemente dall'esito
        loading.classList.add('d-none');
    }
}

/**
 * Genera l'HTML per le card delle strategie (Top 3).
 * @param {Array} strategies - Lista di oggetti strategia restituiti dal backend.
 */
function renderStrategies(strategies) {
    const container = document.getElementById('strategiesContainer');
    const totalLaps = parseInt(document.getElementById('lapsInput').value);

    // Mostriamo soltanto le 3 card delle strategie migliori (compact), ciascuna con miniatura del circuito
    container.innerHTML = '';
    const circuit = document.getElementById('circuitSelect').value;
    // Fallback immagine Bahrain se non trovata
    const circuitInfo = circuitData[circuit] || circuitData['Bahrain Grand Prix'];

    const listWrapper = document.createElement('div');
    listWrapper.className = 'strategy-list d-flex flex-column';

    // Limitiamo a 3 strategie (se presenti)
    strategies.slice(0, 3).forEach((strat, index) => {
        const small = document.createElement('div');
        small.className = 'strategy-card strategy-small mb-2 d-flex align-items-center';

        // Formattazione tempo totale (minuti:secondi)
        const totalSeconds = strat.totalTime;
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = (totalSeconds % 60).toFixed(2);
        
        // Etichetta visuale: Verde per la migliore, Grigio per le altre
        const rankLabel = index === 0
            ? '<span class="badge bg-success mb-2">RACCOMANDATA</span>'
            : `<span class="badge bg-secondary mb-2">OPZIONE ${index + 1}</span>`;

        // Costruzione HTML della Card
        // Include: immagine circuito, tempi, numero soste e barra visiva degli stint colorata
        small.innerHTML = `
            <img class="circuit-thumb me-3" src="images/${circuitInfo.image}" alt="${circuit}" />
            <div style="flex:1;">
                ${rankLabel}
                <div class="strategy-title">Tempo Totale</div>
                <div class="total-time">${minutes}m ${seconds}s</div>
                <small>${strat.pitStops} Soste</small>
            </div>
            <div style="width:35%; margin-left:10px;">
                <label class="text-muted mb-1">Visualizzazione Stint</label>
                <div class="progress" style="height: 24px; background-color: #333;">
                    ${strat.stints.map(s => {
                        // Colore dinamico in base alla mescola (Soft=Rosso, Medium=Giallo, Hard=Bianco)
                        let color = s.compound === 'SOFT' ? '#ff3b30'
                                    : (s.compound === 'MEDIUM' ? '#ffcc00' : '#ffffff');
                        let width = (s.laps / totalLaps) * 100;
                        return `<div class="progress-bar" role="progressbar"
                                    style="width: ${width}%; background-color: ${color}; color: black; font-weight:bold;">
                                    ${s.compound.charAt(0)}
                                </div>`;
                    }).join('')}
                </div>
            </div>
            
            <div class="ms-4 d-flex align-items-center">
                <button class="btn-save-f1">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-floppy2-fill" viewBox="0 0 16 16">
                        <path d="M12 2h-2v3h2z"/>
                        <path d="M1.5 0A1.5 1.5 0 0 0 0 1.5v13A1.5 1.5 0 0 0 1.5 16h13a1.5 1.5 0 0 0 1.5-1.5V2.914a1.5 1.5 0 0 0-.44-1.06L14.06.44A1.5 1.5 0 0 0 13 0h-2v4.5A1.5 1.5 0 0 1 9.5 6h-3A1.5 1.5 0 0 1 5 4.5V0H1.5a.5.5 0 0 0-.5.5v13a.5.5 0 0 0 .5.5h13a.5.5 0 0 0 .5-.5V2.914a.5.5 0 0 0-.146-.353l-1.415-1.415A.5.5 0 0 0 13.086 1H13v2h-2V1zm4 12.5a.5.5 0 0 1 .5-.5h3a.5.5 0 0 1 0 1h-3a.5.5 0 0 1-.5-.5M2 2h10v3H2z"/>
                    </svg>
                    SALVA
                </button>
            </div>
        `;

        // ⬇️ Agganciamo il click al bottone SALVA con l'oggetto strat vero
        // Nota: usiamo stopPropagation() per evitare che il click sul bottone apra anche il dettaglio della card
        const saveBtn = small.querySelector('.btn-save-f1');
        saveBtn.addEventListener('click', (event) => {
            event.stopPropagation();          // non aprire il dettaglio
            saveStrategyToDB(strat, circuit); // strat è un oggetto, NON una stringa
        });

        // Click sull'intera card per aprire i dettagli (tabella stint giro per giro)
        small.style.cursor = 'pointer';
        small.addEventListener('click', () => showStrategyDetail(strat, totalLaps, index));

        listWrapper.appendChild(small);
    });

    container.appendChild(listWrapper);
}

/**
 * DETTAGLIO STRATEGIA E SIMULAZIONE GARA
 * Gestisce l'overlay a schermo intero che mostra i dettagli di una strategia
 * e simula visivamente l'avanzamento della gara giro per giro.
 */

// Mostra la schermata di dettaglio per una strategia selezionata
// Crea dinamicamente un overlay HTML sopra la pagina principale.
function showStrategyDetail(strategy, totalLaps, index) {
    // Crea overlay full-screen (sfondo scuro semi-trasparente)
    const overlay = document.createElement('div');
    overlay.className = 'strategy-detail-overlay';

    // Crea il contenitore della card centrale
    const detail = document.createElement('div');
    detail.className = 'strategy-detail-card';

    // Popolamento HTML della modale
    // Include: Header, visualizzazione del circuito, barra di progresso e riepilogo dati.
    detail.innerHTML = `
        <div class="detail-header d-flex justify-content-between align-items-center mb-3">
            <div><strong>Dettaglio Strategia</strong></div>
            <button class="btn btn-sm btn-outline-light" id="detail-close">Chiudi</button>
        </div>
        
        <div class="detail-circuit">
            ${generateCircuitVisualization(strategy, totalLaps, 'detail', { showCar: false })}
        </div>

        <div class="detail-progress mt-3">
            <div class="d-flex justify-content-between align-items-center mb-1">
                <div class="fw-bold">Progresso Gara</div>
                <div class="text-muted small" id="detail-lap">Giro 0/${totalLaps}</div>
            </div>
            <div class="progress" style="height: 28px; background-color: #333;">
                <div id="strategy-progress" class="progress-bar" role="progressbar" style="width:0%; background-color:#ff3b30;"></div>
            </div>
            <div class="mt-2 d-flex justify-content-between">
                <div class="text-muted small" id="detail-stint">Stint: -</div>
                <div class="text-muted small" id="detail-status">Status: In pista</div>
            </div>
        </div>

        <div class="detail-card mt-3">
            <div class="strategy-title">Tempo Totale</div>
            <div class="total-time mb-2">${Math.floor(strategy.totalTime/60)}m ${(strategy.totalTime%60).toFixed(2)}s</div>
            <div>Stint: ${strategy.stints.map(s => s.compound + ' (' + s.laps + ')').join(', ')}</div>
            <div class="mt-2">Soste: ${strategy.pitStops}</div>
        </div>
    `;

    overlay.appendChild(detail);
    document.body.appendChild(overlay);

    // Gestore chiusura modale
    document.getElementById('detail-close').addEventListener('click', () => {
        // Rimuovendo l'overlay dal DOM, la funzione step() della simulazione si accorgerà
        // che l'elemento non esiste più e fermerà il loop ricorsivo.
        document.body.removeChild(overlay);
    });

    // Avvia simulazione della progress bar (animazione giro per giro)
    simulateStrategyProgress(strategy, totalLaps, 'strategy-progress', {
        // Callback eseguita ad ogni "giro" simulato per aggiornare i testi
        onUpdate: (lap, compound, status, lapTime) => {
            const lapEl = document.getElementById('detail-lap');
            const lapDisplayEl = document.getElementById('lap-display-detail');
            const stintEl = document.getElementById('detail-stint');
            const stintDisplayEl = document.getElementById('stint-display-detail');
            const statusEl = document.getElementById('detail-status');
            const progEl = document.getElementById('strategy-progress');
            
            // Aggiorna contatori e stato
            if (lapEl) lapEl.innerText = `Giro ${lap}/${totalLaps}`;
            if (lapDisplayEl) lapDisplayEl.innerText = `${lap}/${totalLaps}`;
            if (stintEl) stintEl.innerText = `Stint: ${compound}`;
            if (stintDisplayEl) stintDisplayEl.innerText = compound;
            if (statusEl) statusEl.innerText = `Status: ${status}`;

            // Gestione effetto visivo (lampeggio) durante il Pit Stop
            if (status === 'Pit Stop') {
                if (statusEl) statusEl.classList.add('pit-blink');
                if (progEl) progEl.classList.add('progress-pulse');
                
                // Rimuovi l'effetto al termine della sosta
                setTimeout(() => {
                    if (statusEl) statusEl.classList.remove('pit-blink');
                    if (progEl) progEl.classList.remove('progress-pulse');
                }, Math.max(600, lapTime));
            } else {
                // Pulizia preventiva per stati normali
                if (statusEl) statusEl.classList.remove('pit-blink');
                if (progEl) progEl.classList.remove('progress-pulse');
            }
        }
    });
}

// Simula la progressione della gara: aggiorna la larghezza della progress bar e il colore in base al compound
function simulateStrategyProgress(strategy, totalLaps, progressBarId, opts = {}) {
    const progressEl = document.getElementById(progressBarId);
    if (!progressEl) return;

    // 1. Preparazione Dati: Espandiamo gli stint in un array piatto [Giro1=Soft, Giro2=Soft, ..., GiroN=Medium]
    const lapCompounds = [];
    strategy.stints.forEach(s => {
        for (let i = 0; i < s.laps; i++) lapCompounds.push(s.compound);
    });

    // 2. Identificazione Giri di Sosta: Calcoliamo in quali giri avviene il cambio gomme
    const pitLaps = new Set();
    let cum = 0;
    // Iteriamo fino al penultimo stint perché dopo l'ultimo non c'è pit stop
    for (let i = 0; i < strategy.stints.length - 1; i++) {
        cum += strategy.stints[i].laps;
        pitLaps.add(cum); // il pit stop avviene alla fine di questo giro
    }

    let currentLapIndex = 0; // Contatore giri (0-based)

    // Funzione ricorsiva che simula il passaggio del tempo
    function step() {
        // Controllo di sicurezza: se l'utente ha chiuso la modale, fermiamo tutto
        if (!document.body.contains(progressEl)) return;

        const lapNumber = currentLapIndex + 1;
        const compound = lapCompounds[Math.min(currentLapIndex, lapCompounds.length - 1)];

        // Aggiornamento visuale Barra
        const pct = Math.min(100, (lapNumber / totalLaps) * 100);
        // Cambio colore dinamico in base alla gomma attuale
        const color = compound === 'SOFT' ? '#ff3b30' : (compound === 'MEDIUM' ? '#ffcc00' : '#ffffff');
        progressEl.style.backgroundColor = color;
        progressEl.style.width = pct + '%';

        // Calcolo Durata Simulazione Giro
        // Base: ~400ms + variazione in base alla mescola (Soft è più veloce)
        const baseMs = 400 + Math.max(100, 800 - (compound === 'SOFT' ? 100 : (compound === 'MEDIUM' ? 50 : 0)) );
        const jitter = Math.floor(Math.random() * 200) - 100; // Aggiungiamo un po' di casualità (+/-100ms)
        const baseLap = Math.max(200, baseMs + jitter);

        // Se è un giro di Pit Stop, aggiungiamo un ritardo extra per simulare la sosta
        const isPit = pitLaps.has(lapNumber);
        const pitExtra = isPit ? (1200 + Math.floor(Math.random() * 800)) : 0; // pausa di 1.2-2.0 secondi

        // Accelerazione Temporale: Per non annoiare l'utente, i giri normali sono accelerati 10x
        // ma il Pit Stop rimane lento per enfatizzare l'evento.
        const acceleratedLap = Math.max(50, Math.floor(baseLap / 10));
        let lapTime = acceleratedLap + pitExtra;

        // Notifica l'aggiornamento alla UI tramite callback
        if (opts.onUpdate) opts.onUpdate(lapNumber, compound, isPit ? 'Pit Stop' : 'In pista', lapTime);

        // Pianifica il prossimo passo
        currentLapIndex++;
        if (currentLapIndex <= totalLaps - 1) {
            setTimeout(step, lapTime);
        }
    }

    // Avvio iniziale con leggero ritardo per permettere il rendering del DOM
    setTimeout(step, 300);
}

/**
 * VISUALIZZAZIONE GRAFICA E GESTIONE DATI (SALVATAGGIO/STORICO)
 * Contiene le funzioni per generare l'HTML visuale del circuito,
 * salvare le strategie nel DB e caricare la tabella dello storico.
 */

// Genera l'HTML per la visualizzazione del circuito nella modale di dettaglio.
// Calcola dinamicamente dove posizionare i marker "PIT" sulla timeline visiva.
function generateCircuitVisualization(strategy, totalLaps, strategyIndex, opts = {}) {
    const circuit = document.getElementById('circuitSelect').value;
    const circuitInfo = circuitData[circuit] || circuitData['Bahrain Grand Prix'];
    const showPit = opts.showPit !== undefined ? opts.showPit : true;
    const showCar = opts.showCar !== undefined ? opts.showCar : true;

    // Calcola le posizioni dei pit stop (solo se dovremo mostrarle)
    // Determina la posizione percentuale (left: X%) dei box in base al giro in cui avvengono.
    let pitStopHtml = '';
    if (showPit) {
        let currentLap = 0;
        const pitStopPositions = [];
        strategy.stints.forEach((stint, stintIndex) => {
            currentLap += stint.laps;
            // Se non è l'ultimo stint, alla fine di questo c'è un pit stop
            if (stintIndex < strategy.stints.length - 1) {
                pitStopPositions.push({
                    lapAfter: currentLap,
                    stintIndex: stintIndex
                });
            }
        });

        // Genera i div per i marker "PIT" sovrapposti all'immagine
        pitStopPositions.forEach(pit => {
            const pitPercentage = (pit.lapAfter / totalLaps) * 100;
            pitStopHtml += `<div class="pit-stop-zone" style="width: 15%; height: 8%; top: 5%; left: ${pitPercentage - 7.5}%; z-index: ${5 - pit.stintIndex};">PIT</div>`;
        });
    }

    // Ritorna il template string HTML completo
    return `
        <div class="circuit-visualization">
            <div class="circuit-title">Visualizzazione Circuito - ${circuit} (${totalLaps} Giri)</div>
            <div class="track-container">
                <div class="track" style="background-image: url('images/${circuitInfo.image}');">
                    <div class="overlay-track">
                        ${pitStopHtml}
                        ${showCar ? `<div class="car" id="circuit-${strategyIndex}"></div>` : ''}
                    </div>
                </div>
            </div>
            <div class="lap-counter">
                <div class="lap-counter-item">
                    <div class="lap-number" id="lap-display-${strategyIndex}">1/${totalLaps}</div>
                    <div class="lap-status">Giro Corrente</div>
                </div>
                <div class="lap-counter-item">
                    <div class="lap-number" id="stint-display-${strategyIndex}">${strategy.stints[0]?.compound}</div>
                    <div class="lap-status">Gomme Attuali</div>
                </div>
                <div class="lap-counter-item">
                    <div class="lap-number" id="pitstop-display-${strategyIndex}">${strategy.pitStops}</div>
                    <div class="lap-status">Soste Totali</div>
                </div>
            </div>
        </div>
    `;
}


/**
 * Invia la strategia corrente al backend per salvarla nel database.
 * Viene chiamata quando si clicca "SALVA" su una card.
 */
async function saveStrategyToDB(strategy, circuitName) {
    // Convertiamo gli stint (Array di oggetti) in una stringa leggibile per l'anteprima (es. "SOFT (18) -> MEDIUM (20)")
    const desc = strategy.stints.map(s => `${s.compound} (${s.laps})`).join(" -> ");
    
    // Creiamo il payload JSON da inviare
    const payload = {
        circuit: circuitName,
        totalTime: strategy.totalTime,
        pitStops: strategy.pitStops,
        stintsDescription: desc
    };

    try {
        const response = await fetch('/api/history', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            // --- MOSTRA TOAST SUCCESSO ---
            // Feedback visivo non intrusivo (piccola notifica in basso a destra)
            const toastEl = document.getElementById('successToast');
            const toast = new bootstrap.Toast(toastEl, { delay: 3000 }); // Sparisce dopo 3 secondi
            toast.show();
        } else {
            alert("Errore nel salvataggio.");
        }
    } catch (e) {
        console.error(e);
        alert("Errore di connessione.");
    }
}

/**
 * Carica lo storico delle strategie salvate dal database e popola la tabella nella modale "Archivio".
 * Gestisce anche l'associazione degli eventi ai pulsanti di eliminazione generati dinamicamente.
 */
async function loadHistory() {
    try {
        // GET al backend per recuperare la lista
        const response = await fetch('/api/history');
        const history = await response.json();
        
        const tbody = document.getElementById('historyTableBody');
        tbody.innerHTML = '';

        // Inversione array: vogliamo vedere le strategie salvate più di recente in alto
        history.reverse().forEach(item => { // Mostra i più recenti prima
            
            // Conversione secondi -> mm:ss per la visualizzazione
            const totalSec = Number(item.totalTime || 0);
            const min = Math.floor(totalSec / 60);
            const sec = Math.floor(totalSec % 60);

            // Creazione riga tabella
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.createdAt || '-'}</td>
                <td>${item.circuit || '-'}</td>
                <td class="text-danger fw-bold">${min}m ${sec}s</td>
                <td>${item.pitStops != null ? item.pitStops : '-'}</td>
                <td class="small text-muted">${item.stintsDescription || '-'}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-danger btn-delete-strategy" data-id="${item.id}">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash3-fill" viewBox="0 0 16 16">
                            <path d="M11 1.5v1h3.5a.5.5 0 0 1 0 1h-.538l-.853 10.66A2 2 0 0 1 11.115 16h-6.23a2 2 0 0 1-1.994-1.84L2.038 3.5H1.5a.5.5 0 0 1 0-1H5v-1A1.5 1.5 0 0 1 6.5 0h3A1.5 1.5 0 0 1 11 1.5Zm-5 0v1h4v-1a.5.5 0 0 0-.5-.5h-3a.5.5 0 0 0-.5.5ZM5.5 5.5a.5.5 0 0 0-1 .034l.5 7a.5.5 0 1 0 .998-.068l-.5-7Zm3 .034a.5.5 0 0 0-.998-.034l-.5 7a.5.5 0 0 0 .998.068l.5-7Zm2-.034a.5.5 0 0 1 .466.534l-.5 7a.5.5 0 1 1-.998-.068l.5-7a.5.5 0 0 1 .532-.466Z"/>
                        </svg>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });

        // Aggancia i listener ai pulsanti CESTINO (aprono la modale custom)
        // Dobbiamo farlo DOPO aver creato le righe perché sono elementi dinamici
        tbody.querySelectorAll('.btn-delete-strategy').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-id');
                const row = btn.closest('tr');
                showDeleteConfirm(row, id);   // usa la nostra modale custom definita prima
            });
        });

        // Apri la modale Bootstrap dell'archivio
        const modal = new bootstrap.Modal(document.getElementById('historyModal'));
        modal.show();

    } catch (e) {
        console.error(e);
        alert("Impossibile caricare lo storico.");
    }
}

/**
 * LOGICA DISEGNO GRAFICO E INTERAZIONE UI
 * Gestione della libreria Chart.js per renderizzare il grafico di comparazione
 * delle strategie e logica per l'apertura/chiusura del pannello (toggle).
 */

// --- LOGICA DISEGNO GRAFICO ---
// Funzione che inizializza o aggiorna il grafico a linee usando Chart.js
function drawStrategyChart(strategies, totalLaps) {
    const canvas = document.getElementById('strategyChart');
    if (!canvas) return; // Sicurezza se il canvas non esiste (sanity check)
    
    const ctx = canvas.getContext('2d');

    // CORREZIONE CRITICA: Controlla se strategyChart è un'istanza valida di Chart prima di distruggere
    // Chart.js riutilizza il canvas. Se proviamo a disegnare su un canvas già usato senza
    // distruggere la vecchia istanza, otteniamo errori grafici o crash.
    if (strategyChart instanceof Chart) {
        strategyChart.destroy();
    }

    // Preparazione dei dataset per le prime 3 strategie.
    // Simuliamo l'andamento del tempo sul giro per creare una curva verosimile.
    const datasets = strategies.slice(0, 3).map((strat, index) => {
        const dataPoints = [];
        let degPerLap = 0.05; // Degrado fittizio per la visualizzazione
        let baseLap = 90;     // Tempo base fittizio
        
        strat.stints.forEach((stint, sIdx) => {
            // Fattore di degrado: le Soft partono veloci ma degradano prima, le Hard il contrario.
            let factor = stint.compound === 'SOFT' ? 1.5 : (stint.compound === 'HARD' ? 0.8 : 1.0);
            
            for (let i = 0; i < stint.laps; i++) {
                // Formula lineare semplificata per il grafico: Tempo = Base + (Degrado * Giri)
                let time = baseLap + (degPerLap * factor * i);
                
                // Se siamo all'ultimo giro dello stint (e non è l'ultimo stint della gara),
                // aggiungiamo il tempo del Pit Stop (spike nel grafico).
                if (i === stint.laps - 1 && sIdx < strat.stints.length - 1) time += 20; 
                
                dataPoints.push(time);
            }
        });

        return {
            label: index === 0 ? 'Raccomandata' : `Opzione ${index+1}`,
            data: dataPoints,
            // Colori distintivi: Rosso (Best), Giallo (2nd), Grigio (3rd)
            borderColor: ['#ff3b30', '#ffcc00', '#aaaaaa'][index],
            borderWidth: 2, 
            tension: 0.3, // Leggera curvatura della linea per estetica
            pointRadius: 0 // Nascondiamo i punti, mostriamo solo la linea pulita
        };
    });

    // Istanziazione del grafico Chart.js con configurazione Dark Mode
    strategyChart = new Chart(ctx, {
        type: 'line',
        data: { 
            labels: Array.from({length: totalLaps}, (_, i) => `L${i+1}`), // Etichette asse X (L1, L2, ...)
            datasets: datasets 
        },
        options: { 
            responsive: true, 
            maintainAspectRatio: false,
            plugins: { legend: { labels: { color: 'white' } } }, 
            scales: { 
                y: { title: { display:true, text:'Tempo Giro (s)', color:'#aaa'}, grid: { color: '#333' }, ticks: { color: '#eee' } }, 
                x: { grid: { display: false }, ticks: { color: '#aaa', maxTicksLimit: 10 } } 
            } 
        }
    });
}

// Funzione per mostrare/nascondere il grafico
// Gestisce le classi CSS per l'animazione e cambia dinamicamente l'icona del bottone.
function toggleChart() {
    const chartContainer = document.getElementById('chartContainer');
    const btn = document.getElementById('btnToggleChart');
    
    // Controlliamo lo stato attuale tramite la classe 'd-none' (display: none)
    if (chartContainer.classList.contains('d-none')) {
        // --- APRI ---
        chartContainer.classList.remove('d-none');
        // Piccolo ritardo (10ms) per permettere al browser di renderizzare il DOM
        // prima di applicare la classe 'show', attivando così la transizione CSS (fade-in).
        setTimeout(() => chartContainer.classList.add('show'), 10);
        
        // Cambia testo bottone in "CHIUDI" e icona X
        btn.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x-lg" viewBox="0 0 16 16">
                <path d="M2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708.708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8 2.146 2.854Z"/>
            </svg>
            CHIUDI GRAFICO
        `;
        btn.style.borderColor = '#fff';
        btn.style.color = '#fff';
    } else {
        // --- CHIUDI ---
        chartContainer.classList.remove('show');
        chartContainer.classList.add('d-none');
        
        // Ripristina bottone allo stato iniziale "VISUALIZZA"
        btn.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-graph-up" viewBox="0 0 16 16">
                <path fill-rule="evenodd" d="M0 0h1v15h15v1H0V0Zm14.817 3.113a.5.5 0 0 1 .07.704l-4.5 5.5a.5.5 0 0 1-.74.037L7.06 6.767l-3.656 5.027a.5.5 0 0 1-.808-.588l4-5.5a.5.5 0 0 1 .758-.06l2.609 2.61 4.15-5.073a.5.5 0 0 1 .704-.07Z"/>
            </svg>
            VISUALIZZA GRAFICO
        `;
        btn.style.borderColor = ''; // Ripristina stile CSS originale
        btn.style.color = '';
    }
}