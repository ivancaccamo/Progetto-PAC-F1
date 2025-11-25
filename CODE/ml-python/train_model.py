import pandas as pd
import pickle
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.metrics import mean_squared_error, r2_score

# Configurazione
INPUT_FILE = 'training_data.csv'
MODEL_FILE = 'model.pkl'

def train():
    print("--- TRAINING MODELLO ML (DATI REALI) ---")
    
    # 1. Carica Dati
    if not pd.io.common.file_exists(INPUT_FILE):
        print(f"ERRORE: {INPUT_FILE} non trovato. Esegui prima fetch_real_data.py")
        return

    df = pd.read_csv(INPUT_FILE)
    print(f"Dataset caricato: {len(df)} stint totali.")

    # 2. Definisci Features (X) e Target (y)
    # Ora usiamo anche il METEO e il NOME DEL CIRCUITO (più preciso dell'ID)
    X = df[['circuit_name', 'compound', 'air_temp', 'track_temp', 'humidity', 'raining']]
    y = df[['base_time', 'degradation_rate']]

    # 3. Pipeline di Preprocessing
    # - OneHotEncoder per le categorie (circuito, mescola)
    # - StandardScaler per i numeri (temperature) per aiutare il modello
    preprocessor = ColumnTransformer(
        transformers=[
            ('cat', OneHotEncoder(handle_unknown='ignore'), ['circuit_name', 'compound']),
            ('num', StandardScaler(), ['air_temp', 'track_temp'])
        ]
    )

    # 4. Modello: Random Forest è robusto per questi dati
    pipeline = Pipeline(steps=[
        ('preprocessor', preprocessor),
        ('regressor', RandomForestRegressor(n_estimators=100, random_state=100))
    ])

    # 5. Split Train/Test
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.1, random_state=100)

    # 6. Addestramento
    print("Addestramento in corso...")
    pipeline.fit(X_train, y_train)

    # 7. Valutazione
    predictions = pipeline.predict(X_test)
    
    # Calcoliamo metriche per il Degrado (che è la parte difficile)
    y_test_deg = y_test['degradation_rate'].values
    pred_deg = predictions[:, 1]
    
    mse = mean_squared_error(y_test_deg, pred_deg)
    r2 = r2_score(y_test_deg, pred_deg)
    
    print(f"\n--- RISULTATI ---")
    print(f"Errore Medio (RMSE) sul degrado: {mse**0.5:.4f} sec/giro")
    print(f"R2 Score (Accuratezza): {r2:.2f} (1.0 è perfetto)")

    # 8. Salvataggio
    with open(MODEL_FILE, 'wb') as f:
        pickle.dump(pipeline, f)
    print(f"Modello salvato in: {MODEL_FILE}")

if __name__ == "__main__":
    train()