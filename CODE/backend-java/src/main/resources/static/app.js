// Mapping circuiti a immagini
const circuitData = {
    'Bahrain Grand Prix': {
        image: 'Bahrain_Circuit.avif'
    },
    'Saudi Arabian Grand Prix': {
        image: 'Jeddah_Circuit.avif'
    },
    'Monaco Grand Prix': {
        image: 'Monaco_Circuit.avif'
    },
    'Italian Grand Prix': {
        image: 'Monza_Circuit.avif'
    },
    'Spanish Grand Prix': {
        image: 'Barcellona_Circuit.avif'
    }
};

// Aggiorna i valori dei range slider in tempo reale
document.getElementById('trackTemp').oninput = function() {
    document.getElementById('trackTempVal').innerText = this.value + "°C";
}
document.getElementById('airTemp').oninput = function() {
    document.getElementById('airTempVal').innerText = this.value + "°C";
}

async function calculateStrategy() {
    // Mostra loading
    document.getElementById('loading').classList.remove('d-none');
    document.getElementById('strategiesContainer').innerHTML = '';

    const circuit = document.getElementById('circuitSelect').value;
    const laps = document.getElementById('lapsInput').value;
    const track = document.getElementById('trackTemp').value;
    const air = document.getElementById('airTemp').value;

    try {
        // Chiamata al Backend Java
        const response = await fetch(`/api/strategy?circuit=${circuit}&laps=${laps}&airTemp=${air}&trackTemp=${track}`);
        const strategies = await response.json();

        renderStrategies(strategies);
    } catch (error) {
        console.error("Errore:", error);
        alert("Errore di connessione col backend!");
    } finally {
        document.getElementById('loading').classList.add('d-none');
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
        // Usa lo stesso stile della card principale ma con modifiche tramite .strategy-small
        small.className = 'strategy-card strategy-small mb-2 d-flex align-items-center';

        const totalSeconds = strat.totalTime;
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = (totalSeconds % 60).toFixed(2);
        const rankLabel = index === 0 ? '<span class="badge bg-success mb-2">RACCOMANDATA</span>' : `<span class="badge bg-secondary mb-2">OPZIONE ${index + 1}</span>`;

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
                        let color = s.compound === 'SOFT' ? '#ff3b30' : (s.compound === 'MEDIUM' ? '#ffcc00' : '#ffffff');
                        let width = (s.laps / totalLaps) * 100;
                        return `<div class="progress-bar" role="progressbar" style="width: ${width}%; background-color: ${color}; color: black; font-weight:bold;">${s.compound.charAt(0)}</div>`;
                    }).join('')}
                </div>
            </div>
        `;

        small.style.cursor = 'pointer';
        small.addEventListener('click', () => showStrategyDetail(strat, totalLaps, index));

        listWrapper.appendChild(small);
    });

    container.appendChild(listWrapper);

    // Non avviare l'animazione dell'auto nella pagina principale
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
                <div class="track" style="background-image: url('images/${circuitInfo.image}'); background-size: cover; background-position: center;">
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

function animateCarOnTrack(strategy, carId, totalLaps) {
    const carElement = document.getElementById(carId);
    if (!carElement) return;

    let currentLap = 0;
    let currentStintIndex = 0;
    let lapsInCurrentStint = 0;

    // Calcola la durata totale dell'animazione in base ai giri e agli stint
    const totalStintLaps = strategy.stints.reduce((sum, s) => sum + s.laps, 0);
    const timePerLap = 2000 / totalStintLaps; // 2 secondi per giro totale della pista

    function updateCarPosition() {
        currentLap++;
        lapsInCurrentStint++;

        // Verifica se cambiamo stint (pit stop)
        if (lapsInCurrentStint > strategy.stints[currentStintIndex].laps) {
            lapsInCurrentStint = 1;
            if (currentStintIndex < strategy.stints.length - 1) {
                currentStintIndex++;
                // Simula la sosta ai box (car diventa gialla)
                carElement.classList.add('pit-stop');
                setTimeout(() => {
                    carElement.classList.remove('pit-stop');
                }, 800);
            }
        }

        // Calcola la percentuale di progresso intorno alla pista
        const lapProgress = currentLap / totalLaps;
        updateCarAnimation(carElement, lapProgress);

        // Aggiorna i contatori
        const lapDisplayId = carElement.id.replace('circuit-', 'lap-display-');
        const stintDisplayId = carElement.id.replace('circuit-', 'stint-display-');
        const lapDisplay = document.getElementById(lapDisplayId);
        const stintDisplay = document.getElementById(stintDisplayId);

        if (lapDisplay) {
            lapDisplay.innerText = `${currentLap}/${totalLaps}`;
        }
        if (stintDisplay) {
            stintDisplay.innerText = strategy.stints[currentStintIndex]?.compound || '';
        }

        if (currentLap < totalLaps) {
            setTimeout(updateCarPosition, timePerLap);
        }
    }

    // Avvia l'animazione
    updateCarPosition();
}

function updateCarAnimation(carElement, progress) {
    // Percorso circolare: 0 = top, 0.25 = right, 0.5 = bottom, 0.75 = left
    const angle = progress * Math.PI * 2;
    const radius = 45; // percentuale dal centro
    
    const x = 50 + radius * Math.sin(angle);
    const y = 50 - radius * Math.cos(angle);
    
    carElement.style.left = x + '%';
    carElement.style.top = y + '%';
}