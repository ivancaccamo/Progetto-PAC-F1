import pandas as pd
import pickle
import numpy as np
from sklearn.model_selection import train_test_split, RandomizedSearchCV
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import OneHotEncoder, StandardScaler, RobustScaler
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.metrics import mean_squared_error, r2_score

# Configurazione
INPUT_FILE = 'training_data.csv'
MODEL_FILE = 'model.pkl'

def train():
    print("--- TRAINING MODELLO ML AVANZATO ---")
    
    # 1. Carica Dati
    try:
        df = pd.read_csv(INPUT_FILE)
    except FileNotFoundError:
        print(f"ERRORE: {INPUT_FILE} non trovato. Esegui prima fetch_real_data.py")
        return

    print(f"Dataset caricato: {len(df)} stint totali.")
    
    # PULIZIA EXTRA: Rimuoviamo righe con valori nulli o infiniti
    df = df.replace([np.inf, -np.inf], np.nan).dropna()

    # 2. Definisci Features (X) e Target (y)
    # AGGIUNTA IMPORTANTE: Usiamo anche 'team' e 'driver'
    # 'year' è utile perché le macchine cambiano ogni anno
    features = ['circuit_name', 'compound', 'air_temp', 'track_temp', 'team', 'driver', 'year']
    
    # Controlliamo se nel CSV ci sono tutte le colonne (se hai usato il vecchio script fetch potrebbero mancare)
    missing_cols = [col for col in features if col not in df.columns]
    if missing_cols:
        print(f"ATTENZIONE: Mancano queste colonne nel CSV: {missing_cols}")
        print("Uso solo le feature disponibili (il modello sarà meno preciso).")
        features = [col for col in features if col in df.columns]

    X = df[features]
    y = df[['base_time', 'degradation_rate']]

    # 3. Pipeline di Preprocessing
    # Usiamo RobustScaler per le temperature (gestisce meglio i picchi anomali)
    # Usiamo OneHotEncoder per stringhe (Team, Driver, Circuit, Compound)
    categorical_features = [col for col in ['circuit_name', 'compound', 'team', 'driver'] if col in X.columns]
    numerical_features = [col for col in ['air_temp', 'track_temp', 'humidity', 'year'] if col in X.columns]

    preprocessor = ColumnTransformer(
        transformers=[
            ('cat', OneHotEncoder(handle_unknown='ignore', sparse_output=False), categorical_features),
            ('num', RobustScaler(), numerical_features)
        ]
    )

    # 4. Definizione Modello Base
    rf = RandomForestRegressor(random_state=42)

    pipeline = Pipeline(steps=[
        ('preprocessor', preprocessor),
        ('regressor', rf)
    ])

    # 5. Split Train/Test
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # 6. (OPZIONALE) Hyperparameter Tuning con RandomizedSearchCV
    # Cerca i parametri migliori invece di usare quelli di default
    print("Avvio ottimizzazione iperparametri (richiederà qualche minuto)...")
    
    param_grid = {
        'regressor__n_estimators': [100, 200],
        'regressor__max_depth': [10, 20, None],
        'regressor__min_samples_split': [2, 5, 10],
        'regressor__min_samples_leaf': [1, 2, 4]
    }
    
    # RandomizedSearchCV è più veloce di GridSearchCV
    search = RandomizedSearchCV(
        pipeline, 
        param_distributions=param_grid, 
        n_iter=10,       # Prova 10 combinazioni a caso
        cv=3,            # Cross validation a 3 folder
        verbose=1, 
        n_jobs=-1,       # Usa tutti i processori
        random_state=42,
        scoring='r2'
    )
    
    search.fit(X_train, y_train)
    
    print(f"Migliori parametri trovati: {search.best_params_}")
    best_model = search.best_estimator_

    # 7. Valutazione
    predictions = best_model.predict(X_test)
    
    # Metriche
    r2_base = r2_score(y_test['base_time'], predictions[:, 0])
    r2_deg = r2_score(y_test['degradation_rate'], predictions[:, 1])
    
    print(f"\n--- RISULTATI MIGLIORATI ---")
    print(f"R2 Score (Tempo Base): {r2_base:.3f}")
    print(f"R2 Score (Degrado): {r2_deg:.3f}")

    if r2_deg < 0.3:
        print("Nota: Il degrado è molto difficile da prevedere (molto rumore nei dati).")
        print("Tuttavia, finché le Soft degradano più delle Hard, l'algoritmo Java funzionerà.")

    # 8. Salvataggio
    with open(MODEL_FILE, 'wb') as f:
        pickle.dump(best_model, f)
    print(f"Modello ottimizzato salvato in: {MODEL_FILE}")

if __name__ == "__main__":
    train()