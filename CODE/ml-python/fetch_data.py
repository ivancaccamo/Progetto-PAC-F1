import fastf1
import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
import os

# Configurazione cache (creerà una cartella per salvare i dati scaricati)
if not os.path.exists('cache'):
    os.makedirs('cache')

fastf1.Cache.enable_cache('cache') 

OUTPUT_FILE = 'training_data.csv'

def get_season_data(year):
    print(f"--- SCARICAMENTO DATI STAGIONE {year} ---")
    all_stints = []
    
    # Scarichiamo il calendario
    schedule = fastf1.get_event_schedule(year)
    
    # Filtriamo solo le gare disputate (escludiamo quelle future o cancellate)
    races = schedule[schedule['EventFormat'] == 'conventional'] # Solo gare standard per ora
    
    for i, race in races.iterrows():
        try:
            print(f"Elaborazione: {race['EventName']}...")
            
            # Carichiamo la sessione di Gara
            session = fastf1.get_session(year, race['RoundNumber'], 'R')
            session.load(weather=True, telemetry=False, messages=False) # Telemetry False per velocità
            
            laps = session.laps
            
            # Filtriamo giri validi (non in safety car, non pit in/out)
            # Teniamo solo i giri lanciati puliti per calcolare il degrado puro
            laps = laps.pick_quicklaps()
            
            # Raggruppiamo per Pilota e Stint
            for driver in session.drivers:
                driver_laps = laps.pick_driver(driver)
                
                # Identifichiamo gli stint (FastF1 lo fa in automatico!)
                for stint_number, stint_laps in driver_laps.groupby('Stint'):
                    if len(stint_laps) < 5: continue
                    
                    # Dati sulle gomme (REALI!)
                    compound = stint_laps['Compound'].iloc[0]
                    tyre_life = stint_laps['TyreLife']
                    lap_times = stint_laps['LapTime'].dt.total_seconds()
                    
                    # Calcolo Degrado (Regressione Lineare su questo stint)
                    # X = Vita Gomma, Y = Tempo sul giro
                    X = tyre_life.values.reshape(-1, 1)
                    y = lap_times.values
                    
                    # Se ci sono NaN, saltiamo
                    if np.isnan(y).any() or np.isnan(X).any(): continue

                    model = LinearRegression()
                    model.fit(X, y)
                    
                    base_time = model.predict([[1]])[0] # Tempo al 1° giro di vita
                    degradation = model.coef_[0] # Secondi persi per giro
                    
                    # Filtri di qualità
                    if degradation < 0: degradation = 0 # Ignoriamo degrado negativo (giri che migliorano troppo)
                    if degradation > 0.5: continue # Degrado > 0.5s al giro è spesso errore/traffico
                    
                    all_stints.append({
                        'circuit_name': race['EventName'],
                        'circuitId': race['RoundNumber'], # Usiamo il round number come ID semplificato
                        'compound': compound,
                        'air_temp': session.weather_data['AirTemp'].mean(), # Meteo REALE
                        'track_temp': session.weather_data['TrackTemp'].mean(),
                        'base_time': base_time,
                        'degradation_rate': degradation
                    })
                    
        except Exception as e:
            print(f"Errore su {race['EventName']}: {e}")
            continue

    return pd.DataFrame(all_stints)

if __name__ == "__main__":
    # Scarichiamo dati 2023 (ultimo anno completo)
    df = get_season_data(2023)
    
    print("\n--- ESEMPIO DATI REALI ---")
    print(df.head())
    print(df['compound'].value_counts())
    
    df.to_csv(OUTPUT_FILE, index=False)
    print(f"Dataset salvato in {OUTPUT_FILE}")