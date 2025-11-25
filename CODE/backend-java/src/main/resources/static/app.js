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

    strategies.forEach((strat, index) => {
        // Creiamo la card HTML
        const card = document.createElement('div');
        card.className = 'strategy-card';
        
        // Calcoliamo i dati per il grafico (stint visuali)
        const stintHtml = strat.stints.map(s => {
            let colorClass = s.compound === 'SOFT' ? 'badge-soft' : (s.compound === 'MEDIUM' ? 'badge-medium' : 'badge-hard');
            // Larghezza % basata sui giri totali (approx)
            let widthPct = (s.laps / 57) * 100; 
            return `<span class="badge ${colorClass} me-1" style="width:${widthPct}%; display:inline-block; text-align:center;">${s.compound} (${s.laps} giri)</span>`;
        }).join('');

        // Tempo convertito (ore:min:sec)
        const totalSeconds = strat.totalTime;
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = (totalSeconds % 60).toFixed(2);
        
        const rankLabel = index === 0 ? '<span class="badge bg-success mb-2">RACCOMANDATA</span>' : `<span class="badge bg-secondary mb-2">OPZIONE ${index + 1}</span>`;

        card.innerHTML = `
            <div class="row align-items-center">
                <div class="col-md-3">
                    ${rankLabel}
                    <div class="strategy-title">Tempo Totale</div>
                    <div class="total-time">${minutes}m ${seconds}s</div>
                    <small>${strat.pitStops} Soste</small>
                </div>
                <div class="col-md-9">
                    <label class="text-muted mb-1">Visualizzazione Stint</label>
                    <div class="progress" style="height: 30px; background-color: #333;">
                        ${strat.stints.map(s => {
                            let color = s.compound === 'SOFT' ? '#ff3b30' : (s.compound === 'MEDIUM' ? '#ffcc00' : '#ffffff');
                            let width = (s.laps / 60) * 100; // Normalizzato su 60 giri per semplicità
                            return `<div class="progress-bar" role="progressbar" style="width: ${width}%; background-color: ${color}; color: black; font-weight:bold;">${s.compound.charAt(0)}</div>`;
                        }).join('')}
                    </div>
                    <div class="mt-2 d-flex justify-content-between text-muted" style="font-size: 0.8em;">
                        ${strat.stints.map(s => `<span>${s.laps} giri</span>`).join('')}
                    </div>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}