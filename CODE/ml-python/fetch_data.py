import fastf1
import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
import os

# Configurazione cache
if not os.path.exists('cache'):
    os.makedirs('cache')

fastf1.Cache.enable_cache('cache') 

OUTPUT_FILE = 'training_data.csv'
# Definiamo gli anni da analizzare
YEARS = [2021, 2022, 2023, 2024]

def get_season_data(year):
    print(f"\n=========================================")
    print(f"--- SCARICAMENTO DATI STAGIONE {year} ---")
    print(f"=========================================")
    
    all_stints = []
    
    # Scarichiamo il calendario
    try:
        schedule = fastf1.get_event_schedule(year)
    except Exception as e:
        print(f"Errore scaricamento calendario {year}: {e}")
        return pd.DataFrame()
    
    # Filtriamo solo le gare disputate
    # Escludiamo i test pre-stagionali (RoundNumber 0)
    races = schedule[schedule['RoundNumber'] > 0]
    
    for i, race in races.iterrows():
        # Saltiamo le gare future (se stiamo analizzando l'anno corrente)
        if not hasattr(race, 'Session5Date') or pd.isna(race['Session5Date']): 
             continue

        try:
            print(f"Elaborazione: {race['EventName']} (Round {race['RoundNumber']})...")
            
            # Carichiamo la sessione di Gara ('R')
            session = fastf1.get_session(year, race['RoundNumber'], 'R')
            
            # Carichiamo meteo e giri. Telemetry=False per velocità, ma i dati base ci sono.
            session.load(weather=True, telemetry=False, messages=False)
            
            laps = session.laps
            
            # Se non ci sono giri (gara cancellata o errore), saltiamo
            if laps.empty:
                print("  -> Nessun giro trovato (Sessione vuota o cancellata).")
                continue

            # --- METEO MEDIO DELLA SESSIONE ---
            # Calcoliamo il meteo medio della gara per assegnarlo agli stint
            # (Sarebbe più preciso farlo per stint, ma rallenta molto. La media è ok per questo scopo)
            avg_air_temp = session.weather_data['AirTemp'].mean() if not session.weather_data.empty else 25.0
            avg_track_temp = session.weather_data['TrackTemp'].mean() if not session.weather_data.empty else 35.0
            avg_humidity = session.weather_data['Humidity'].mean() if not session.weather_data.empty else 50.0
            is_raining = session.weather_data['Rainfall'].any() if not session.weather_data.empty else False

            # Filtriamo giri validi (Quicklaps rimuove in/out laps e giri troppo lenti/safety car)
            laps = laps.pick_quicklaps()
            
            # Iteriamo su ogni pilota
            for driver in session.drivers:
                try:
                    driver_info = session.get_driver(driver)
                    driver_laps = laps.pick_driver(driver)
                    team_name = driver_info['TeamName']
                except:
                    continue # Salta se info pilota mancanti
                
                # Raggruppiamo per Stint
                for stint_number, stint_laps in driver_laps.groupby('Stint'):
                    # Ignoriamo stint troppo corti (meno di 5 giri validi)
                    if len(stint_laps) < 5: continue
                    
                    # Dati sulle gomme
                    compound = stint_laps['Compound'].iloc[0]
                    
                    # Ignoriamo gomme da bagnato (INTERMEDIATE, WET) per il modello di degrado
                    if compound in ['INTERMEDIATE', 'WET', 'UNKNOWN']: continue
                    
                    tyre_life = stint_laps['TyreLife']
                    lap_times = stint_laps['LapTime'].dt.total_seconds()
                    
                    # --- CALCOLO DEGRADO (Regressione Lineare) ---
                    # X = Vita Gomma, Y = Tempo sul giro
                    X = tyre_life.values.reshape(-1, 1)
                    y = lap_times.values
                    
                    # Se ci sono NaN, saltiamo
                    if np.isnan(y).any() or np.isnan(X).any(): continue

                    model = LinearRegression()
                    model.fit(X, y)
                    
                    base_time = model.predict([[1]])[0] # Tempo teorico al 1° giro di vita
                    degradation = model.coef_[0]        # Secondi persi per ogni giro (pendenza)
                    
                    # --- FILTRI DI QUALITÀ ---
                    # 1. Degrado negativo: significa che il pilota sta andando più veloce man mano che la gomma invecchia.
                    #    Possibile per alleggerimento carburante, ma per il degrado gomma lo settiamo a 0 o scartiamo.
                    if degradation < 0: degradation = 0 
                    
                    # 2. Degrado eccessivo: > 0.5s al giro è solitamente un crollo o un errore nel dato
                    if degradation > 0.5: continue 
                    
                    all_stints.append({
                        'year': year,
                        'circuit_name': race['EventName'],
                        'circuitId': race['RoundNumber'],
                        'driver': driver,          # Feature Nuova
                        'team': team_name,         # Feature Nuova
                        'compound': compound,
                        'air_temp': avg_air_temp,
                        'track_temp': avg_track_temp,
                        'humidity': avg_humidity,  # Feature Nuova
                        'raining': is_raining,     # Feature Nuova
                        'base_time': round(base_time, 3),
                        'degradation_rate': round(degradation, 5)
                    })
                    
        except Exception as e:
            print(f"  Errore generico su {race['EventName']}: {e}")
            continue

    return pd.DataFrame(all_stints)

if __name__ == "__main__":
    
    all_seasons_data = []

    # Iteriamo su tutti gli anni richiesti
    for year in YEARS:
        df_season = get_season_data(year)
        if not df_season.empty:
            all_seasons_data.append(df_season)
            print(f" -> Stagione {year} completata: {len(df_season)} stint trovati.")
    
    # Uniamo tutto in un unico DataFrame
    if all_seasons_data:
        final_df = pd.concat(all_seasons_data, ignore_index=True)
        
        print("\n--- STATISTICHE DATASET FINALE ---")
        print(final_df.info())
        print("\nConteggio per Mescola:")
        print(final_df['compound'].value_counts())
        print("\nConteggio per Anno:")
        print(final_df['year'].value_counts())
        
        final_df.to_csv(OUTPUT_FILE, index=False)
        print(f"\nDataset completo salvato in: {OUTPUT_FILE}")
    else:
        print("Nessun dato trovato.")