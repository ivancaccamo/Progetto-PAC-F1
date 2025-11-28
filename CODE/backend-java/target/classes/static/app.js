// Mapping circuiti a immagini
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
let deleteContext = { row: null, id: null };

document.addEventListener('DOMContentLoaded', () => {
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    if (!confirmBtn) return;

    confirmBtn.addEventListener('click', async () => {
        const { row, id } = deleteContext;
        if (!id) return;

        try {
            const delResp = await fetch(`/api/history/${id}`, {
                method: 'DELETE'
            });

            if (delResp.ok) {
                // rimuovo la riga dalla tabella
                if (row && row.parentNode) {
                    row.parentNode.removeChild(row);
                }

                // pulisco il contesto
                deleteContext = { row: null, id: null };

                // chiudo la modale
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

// funzione helper per aprire la modale di conferma
function showDeleteConfirm(rowEl, id) {
    deleteContext.row = rowEl;
    deleteContext.id = id;

    const modalEl = document.getElementById('confirmDeleteModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
}

// Aggiorna i valori dei range slider in tempo reale
document.getElementById('trackTemp').oninput = function() {
    document.getElementById('trackTempVal').innerText = this.value + "°C";
}
document.getElementById('airTemp').oninput = function() {
    document.getElementById('airTempVal').innerText = this.value + "°C";
}

async function calculateStrategy() {
    const loading = document.getElementById('loading');
    const container = document.getElementById('strategiesContainer');
    
    // Pulisci risultati precedenti
    container.innerHTML = '';

    const circuit = document.getElementById('circuitSelect').value;
    const laps = parseInt(document.getElementById('lapsInput').value); // Convertiamo in numero intero
    const track = document.getElementById('trackTemp').value;
    const air = document.getElementById('airTemp').value;

    // --- CONTROLLO CIRCUITO MANCANTE ---
    if (!circuit || circuit === "") {
        // Nascondi loading se era attivo
        if(typeof loading !== 'undefined') loading.classList.add('d-none');

        const toastEl = document.getElementById('circuitToast');
        // Mostra il toast per 4 secondi
        const toast = new bootstrap.Toast(toastEl, { delay: 4000 }); 
        toast.show();
        
        return; // Ferma tutto
    }
    

    // --- CONTROLLO TOAST ---
    if (laps < 15) {
        if(typeof loading !== 'undefined') loading.classList.add('d-none'); // Safe check
        
        const toastEl = document.getElementById('errorToast');
        
        // Inizializza con opzioni: animazione attiva, nascondi dopo 5s
        const toast = new bootstrap.Toast(toastEl, { delay: 5000 }); 
        toast.show();
        
        return;
    }

    loading.classList.remove('d-none');

    try {
        // Chiamata API
        const response = await fetch(`/api/strategy?circuit=${encodeURIComponent(circuit)}&laps=${laps}&airTemp=${air}&trackTemp=${track}`);
        
        if (!response.ok) throw new Error("Errore API Java");
        
        const strategies = await response.json();
        
        if (strategies.length === 0) {
            container.innerHTML = '<div class="alert alert-warning">Nessuna strategia trovata. Riprova con parametri diversi.</div>';
        } else {
            renderStrategies(strategies, laps);
        }

    } catch (error) {
        console.error("Errore:", error);
        container.innerHTML = `<div class="alert alert-danger">Errore di comunicazione col server: ${error.message}</div>`;
    } finally {
        loading.classList.add('d-none');
    }
}

function renderStrategies(strategies) {
    const container = document.getElementById('strategiesContainer');
    const totalLaps = parseInt(document.getElementById('lapsInput').value);

    // Mostriamo soltanto le 3 card delle strategie migliori (compact), ciascuna con miniatura del circuito
    container.innerHTML = '';
    const circuit = document.getElementById('circuitSelect').value;
    const circuitInfo = circuitData[circuit] || circuitData['Bahrain Grand Prix'];

    const listWrapper = document.createElement('div');
    listWrapper.className = 'strategy-list d-flex flex-column';

    // Limitiamo a 3 strategie (se presenti)
    strategies.slice(0, 3).forEach((strat, index) => {
        const small = document.createElement('div');
        small.className = 'strategy-card strategy-small mb-2 d-flex align-items-center';

        const totalSeconds = strat.totalTime;
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = (totalSeconds % 60).toFixed(2);
        const rankLabel = index === 0
            ? '<span class="badge bg-success mb-2">RACCOMANDATA</span>'
            : `<span class="badge bg-secondary mb-2">OPZIONE ${index + 1}</span>`;

        // ⬇️ QUI NON C'È PIÙ l'onclick con JSON.stringify
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
        const saveBtn = small.querySelector('.btn-save-f1');
        saveBtn.addEventListener('click', (event) => {
            event.stopPropagation();          // non aprire il dettaglio
            saveStrategyToDB(strat, circuit); // strat è un oggetto, NON una stringa
        });

        small.style.cursor = 'pointer';
        small.addEventListener('click', () => showStrategyDetail(strat, totalLaps, index));

        listWrapper.appendChild(small);
    });

    container.appendChild(listWrapper);
}


// Mostra la schermata di dettaglio per una strategia selezionata
function showStrategyDetail(strategy, totalLaps, index) {
    // Crea overlay full-screen
    const overlay = document.createElement('div');
    overlay.className = 'strategy-detail-overlay';

    const detail = document.createElement('div');
    detail.className = 'strategy-detail-card';

    // Header con bottone close
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
            <!-- Card della strategia sotto -->
            <div class="strategy-title">Tempo Totale</div>
            <div class="total-time mb-2">${Math.floor(strategy.totalTime/60)}m ${(strategy.totalTime%60).toFixed(2)}s</div>
            <div>Stint: ${strategy.stints.map(s => s.compound + ' (' + s.laps + ')').join(', ')}</div>
            <div class="mt-2">Soste: ${strategy.pitStops}</div>
        </div>
    `;

    overlay.appendChild(detail);
    document.body.appendChild(overlay);

    // Close handler
    document.getElementById('detail-close').addEventListener('click', () => {
        // stop any running simulation by removing overlay
        document.body.removeChild(overlay);
    });

    // Avvia simulazione della progress bar (mostra cambi colore al cambiare del compound)
    simulateStrategyProgress(strategy, totalLaps, 'strategy-progress', {
        onUpdate: (lap, compound, status, lapTime) => {
            const lapEl = document.getElementById('detail-lap');
            const lapDisplayEl = document.getElementById('lap-display-detail');
            const stintEl = document.getElementById('detail-stint');
            const stintDisplayEl = document.getElementById('stint-display-detail');
            const statusEl = document.getElementById('detail-status');
            const progEl = document.getElementById('strategy-progress');
            if (lapEl) lapEl.innerText = `Giro ${lap}/${totalLaps}`;
            if (lapDisplayEl) lapDisplayEl.innerText = `${lap}/${totalLaps}`;
            if (stintEl) stintEl.innerText = `Stint: ${compound}`;
            if (stintDisplayEl) stintDisplayEl.innerText = compound;
            if (statusEl) statusEl.innerText = `Status: ${status}`;

            // Gestione effetto lampeggiante durante pit stop
            if (status === 'Pit Stop') {
                if (statusEl) statusEl.classList.add('pit-blink');
                if (progEl) progEl.classList.add('progress-pulse');
                // Rimuovi l'effetto dopo la durata stimata del giro (lapTime)
                setTimeout(() => {
                    if (statusEl) statusEl.classList.remove('pit-blink');
                    if (progEl) progEl.classList.remove('progress-pulse');
                }, Math.max(600, lapTime));
            } else {
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

    // Espandi gli stint in una lista di compound per ogni giro
    const lapCompounds = [];
    strategy.stints.forEach(s => {
        for (let i = 0; i < s.laps; i++) lapCompounds.push(s.compound);
    });

    // Calcola i giri in cui ci sono pit stop (dopo ogni stint tranne l'ultimo)
    const pitLaps = new Set();
    let cum = 0;
    for (let i = 0; i < strategy.stints.length - 1; i++) {
        cum += strategy.stints[i].laps;
        pitLaps.add(cum); // pit after this lap
    }

    let currentLapIndex = 0; // 0-based

    // Funzione ricorsiva con tempi variabili
    function step() {
        // Se overlay chiuso o elemento rimosso, fermiamo la simulazione
        if (!document.body.contains(progressEl)) return;

        const lapNumber = currentLapIndex + 1;
        const compound = lapCompounds[Math.min(currentLapIndex, lapCompounds.length - 1)];

        // Percentuale
        const pct = Math.min(100, (lapNumber / totalLaps) * 100);

        // Colore per compound
        const color = compound === 'SOFT' ? '#ff3b30' : (compound === 'MEDIUM' ? '#ffcc00' : '#ffffff');
        progressEl.style.backgroundColor = color;
        progressEl.style.width = pct + '%';

        // Callback di aggiornamento testuale
        // Tempo per questo giro: base + random
        // Applichiamo un'accelerazione 10x sulla parte "in pista" (base+jitter)
        // ma manteniamo la durata della sosta (pit extra) invariata.
        const baseMs = 400 + Math.max(100, 800 - (compound === 'SOFT' ? 100 : (compound === 'MEDIUM' ? 50 : 0)) );
        const jitter = Math.floor(Math.random() * 200) - 100; // +/-100ms
        const baseLap = Math.max(200, baseMs + jitter);

        // Se questo giro è immediatamente seguito da un pit stop, calcola durata della sosta
        const isPit = pitLaps.has(lapNumber);
        const pitExtra = isPit ? (1200 + Math.floor(Math.random() * 800)) : 0; // pit stop pause 1.2-2s

        // Riduci la parte "in pista" di 10x, mantieni pitExtra invariato
        const acceleratedLap = Math.max(50, Math.floor(baseLap / 10));
        let lapTime = acceleratedLap + pitExtra;

        // Callback di aggiornamento testuale con durata del giro
        if (opts.onUpdate) opts.onUpdate(lapNumber, compound, isPit ? 'Pit Stop' : 'In pista', lapTime);

        currentLapIndex++;
        if (currentLapIndex <= totalLaps - 1) {
            setTimeout(step, lapTime);
        }
    }

    // Start leggermente ritardato
    setTimeout(step, 300);
}

function generateCircuitVisualization(strategy, totalLaps, strategyIndex, opts = {}) {
    const circuit = document.getElementById('circuitSelect').value;
    const circuitInfo = circuitData[circuit] || circuitData['Bahrain Grand Prix'];
    const showPit = opts.showPit !== undefined ? opts.showPit : true;
    const showCar = opts.showCar !== undefined ? opts.showCar : true;

    // Calcola le posizioni dei pit stop (solo se dovremo mostrarle)
    let pitStopHtml = '';
    if (showPit) {
        let currentLap = 0;
        const pitStopPositions = [];
        strategy.stints.forEach((stint, stintIndex) => {
            currentLap += stint.laps;
            if (stintIndex < strategy.stints.length - 1) {
                pitStopPositions.push({
                    lapAfter: currentLap,
                    stintIndex: stintIndex
                });
            }
        });

        pitStopPositions.forEach(pit => {
            const pitPercentage = (pit.lapAfter / totalLaps) * 100;
            pitStopHtml += `<div class="pit-stop-zone" style="width: 15%; height: 8%; top: 5%; left: ${pitPercentage - 7.5}%; z-index: ${5 - pit.stintIndex};">PIT</div>`;
        });
    }

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



async function saveStrategyToDB(strategy, circuitName) {
    // Convertiamo gli stint in una stringa leggibile
    const desc = strategy.stints.map(s => `${s.compound} (${s.laps})`).join(" -> ");
    
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

async function loadHistory() {
    try {
        const response = await fetch('/api/history');
        const history = await response.json();
        
        const tbody = document.getElementById('historyTableBody');
        tbody.innerHTML = '';

        history.reverse().forEach(item => { // Mostra i più recenti prima
            const totalSec = Number(item.totalTime || 0);
            const min = Math.floor(totalSec / 60);
            const sec = Math.floor(totalSec % 60);

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
        tbody.querySelectorAll('.btn-delete-strategy').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-id');
                const row = btn.closest('tr');
                showDeleteConfirm(row, id);   // usa la nostra modale custom
            });
        });

        // Apri la modale Bootstrap
        const modal = new bootstrap.Modal(document.getElementById('historyModal'));
        modal.show();

    } catch (e) {
        console.error(e);
        alert("Impossibile caricare lo storico.");
    }
}

