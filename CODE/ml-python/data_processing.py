import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
import random
import os

# Configurazione
DATA_DIR = 'data'
OUTPUT_FILE = 'training_data.csv'

def load_and_merge():
    print("--- 1. CARICAMENTO DATI ---")
    lap_times = pd.read_csv(os.path.join(DATA_DIR, 'lap_times.csv'))
    pit_stops = pd.read_csv(os.path.join(DATA_DIR, 'pit_stops.csv'))
    races = pd.read_csv(os.path.join(DATA_DIR, 'races.csv'))
    
    # Uniamo per avere info sulla gara e circuito
    df = pd.merge(lap_times, races[['raceId', 'year', 'circuitId']], on='raceId', how='left')
    
    # Filtriamo gare recenti (es. dal 2018) per avere vetture simili
    df = df[df['year'] >= 2018]
    print(f"Giri totali caricati (post-2018): {len(df)}")
    return df, pit_stops

def identify_stints(df, pit_stops):
    print("--- 2. IDENTIFICAZIONE STINT ---")
    # Ordiniamo per processare sequenzialmente
    df = df.sort_values(by=['raceId', 'driverId', 'lap'])
    
    # Creiamo una colonna 'Stint' inizializzata a 0
    df['stint_id'] = 0
    
    # Logica semplificata: ogni pit stop incrementa il contatore dello stint per quel pilota
    # (Per un progetto reale si userebbe groupby complesso, qui simuliamo)
    
    # Creiamo un identificativo unico per stint
    # Un stint cambia se cambia il driver, la gara, o se c'è un pit stop
    # Usiamo i pit stops per marcare i giri di cambio
    pit_flags = df.merge(pit_stops[['raceId', 'driverId', 'lap']], on=['raceId', 'driverId', 'lap'], how='left', indicator=True)
    df['is_pit_in'] = (pit_flags['_merge'] == 'both')
    
    # Calcolo cumulativo degli stint
    # Ogni volta che c'è un pit stop o cambia pilota/gara, incrementiamo stint_id
    df['stint_change'] = df['is_pit_in'].astype(int) + \
                         (df['raceId'] != df['raceId'].shift(1)).astype(int) + \
                         (df['driverId'] != df['driverId'].shift(1)).astype(int)
    
    df['global_stint_id'] = df['stint_change'].cumsum()
    
    return df

def calculate_degradation(df):
    print("--- 3. CALCOLO DEGRADO (REGRESSIONE SU STINT) ---")
    stint_stats = []
    
    # Raggruppiamo per stint identificato
    grouped = df.groupby('global_stint_id')
    
    for stint_id, group in grouped:
        if len(group) < 5: continue  # Ignoriamo stint troppo corti
        
        # Pulizia Outliers (Safety Car, giri lenti)
        # Teniamo solo giri entro il 105% della mediana dello stint
        median_time = group['milliseconds'].median()
        clean_group = group[group['milliseconds'] < median_time * 1.05]
        
        if len(clean_group) < 3: continue
        
        # Prepariamo dati per regressione lineare: X=GiroStint, Y=Tempo
        # Dobbiamo ricalcolare il numero di giro RELATIVO allo stint (1, 2, 3...)
        clean_group = clean_group.copy()
        clean_group['rel_lap'] = clean_group['lap'] - clean_group['lap'].min()
        
        X = clean_group[['rel_lap']].values
        y = clean_group['milliseconds'].values
        
        model = LinearRegression()
        model.fit(X, y)
        
        base_time = model.intercept_  # Tempo teorico al giro 0 dello stint
        degradation = model.coef_[0]  # Millisecondi persi per ogni giro
        
        # Filtri di sanità: degrado negativo (improbabile) o eccessivo
        if degradation < 0 or degradation > 5000: continue
        
        # ASSEGNAZIONE SINTETICA MESCOLA (Il dataset non la ha)
        # Logica euristica: stint lunghi (>25 giri) -> Hard, medi -> Medium, corti -> Soft
        stint_len = group['lap'].max() - group['lap'].min()
        if stint_len > 30: compound = 'Hard'
        elif stint_len > 15: compound = 'Medium'
        else: compound = 'Soft'
        
        stint_stats.append({
            'circuitId': group['circuitId'].iloc[0],
            'compound': compound,
            'air_temp': random.randint(20, 35), # Sintetico (o da unire con meteo se c'è)
            'base_time': base_time,
            'degradation_rate': degradation
        })
        
    return pd.DataFrame(stint_stats)

if __name__ == "__main__":
    df, pits = load_and_merge()
    df = identify_stints(df, pits)
    dataset = calculate_degradation(df)
    
    print(f"\nGenerato dataset di training con {len(dataset)} esempi di stint.")
    print(dataset.head())
    
    dataset.to_csv(OUTPUT_FILE, index=False)
    print(f"Salvato in {OUTPUT_FILE}")