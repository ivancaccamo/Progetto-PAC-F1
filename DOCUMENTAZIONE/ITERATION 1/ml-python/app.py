from flask import Flask, request, jsonify
import pandas as pd
import pickle
import os

app = Flask(__name__)

# Carichiamo il modello all'avvio
MODEL_FILE = 'model.pkl'
model = None

if os.path.exists(MODEL_FILE):
    with open(MODEL_FILE, 'rb') as f:
        model = pickle.load(f)
    print("Modello ML caricato correttamente.")
else:
    print("ATTENZIONE: Modello non trovato. Esegui train_model.py")

@app.route('/predict', methods=['POST'])
def predict():
    """
    Endpoint per predire degrado e tempo base.
    Input JSON atteso:
    {
        "circuit_name": "Bahrain Grand Prix",
        "air_temp": 25.0,
        "track_temp": 30.0,
        "compounds": ["SOFT", "MEDIUM", "HARD"]
    }
    """
    if not model:
        return jsonify({"error": "Model not loaded"}), 500

    data = request.get_json()
    
    circuit = data.get('circuit_name')
    air_temp = data.get('air_temp', 25.0)   # Valore default se manca
    track_temp = data.get('track_temp', 35.0)
    compounds = data.get('compounds', [])
    team = data.get('team', 'Red Bull Racing')      # Default generico
    driver = data.get('driver', 'Verstappen')       # Default generico
    year = data.get('year', 2024)                   # Anno corrente

    results = []

    # Facciamo una predizione per ogni mescola richiesta
    for comp in compounds:
        # Creiamo un DataFrame con una sola riga per la predizione
        input_df = pd.DataFrame({
            'circuit_name': [circuit],
            'compound': [comp],
            'air_temp': [air_temp],
            'track_temp': [track_temp],
            'team': [team],         # <--- NUOVO
            'driver': [driver],     # <--- NUOVO
            'year': [year]          # <--- NUOVO
        })
        
        # Il modello restituisce [[base_time, degradation]]
        pred = model.predict(input_df)[0]
        
        results.append({
            "compound": comp,
            "base_time": round(pred[0], 3),        # Secondi
            "degradation_rate": round(pred[1], 4)  # Secondi persi al giro
        })

    return jsonify({
        "circuit": circuit,
        "predictions": results
    })

if __name__ == '__main__':
    # Avvia il server sulla porta 5000
    app.run(host='0.0.0.0', port=5000, debug=True)