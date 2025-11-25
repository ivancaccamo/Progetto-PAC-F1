import pandas as pd
import pickle
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.metrics import r2_score

# Configurazione
INPUT_FILE = 'training_data.csv'
MODEL_FILE = 'model.pkl'

def evaluate():
    print("--- VALUTAZIONE VISIVA MODELLO ---")
    
    # 1. Carica dati e modello
    df = pd.read_csv(INPUT_FILE)
    with open(MODEL_FILE, 'rb') as f:
        model = pickle.load(f)

    # 2. Fai le predizioni su TUTTO il dataset
    X = df[['circuit_name', 'compound', 'air_temp', 'track_temp']]
    y_true = df['degradation_rate']
    
    # Il modello restituisce [base_time, degradation], prendiamo la colonna 1 (degrado)
    y_pred = model.predict(X)[:, 1]

    # 3. Calcola R2
    r2 = r2_score(y_true, y_pred)
    print(f"R2 Score Totale: {r2:.3f}")

    # 4. Genera il Grafico
    plt.figure(figsize=(10, 6))
    
    # Disegna i punti (Realtà vs Predizione) colorati per mescola
    sns.scatterplot(x=y_true, y=y_pred, hue=df['compound'], alpha=0.6)
    
    # Disegna la linea rossa tratteggiata (la perfezione ideale)
    plt.plot([y_true.min(), y_true.max()], [y_true.min(), y_true.max()], 'r--', lw=2, label='Predizione Perfetta')
    
    plt.xlabel('Degrado REALE (misurato)')
    plt.ylabel('Degrado PREDETTO (dal modello)')
    plt.title(f'Verifica Modello: Realtà vs Predizione (R2={r2:.2f})')
    plt.legend()
    plt.grid(True)
    
    # Salva e mostra
    plt.savefig('valutazione_modello.png')
    print("Grafico salvato come 'valutazione_modello.png'")
    plt.show()

if __name__ == "__main__":
    evaluate()