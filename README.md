# SPS-F1 🏎️
### Strategic Prediction System for Formula 1

**SPS-F1** è un sistema di supporto decisionale progettato per ottimizzare le strategie di gara in Formula 1. Il progetto combina tecniche di **Machine Learning** (per la predizione del degrado gomme) e algoritmi di **Programmazione Dinamica** (per il calcolo della strategia ottimale dei pit-stop) in un'architettura a microservizi.

Progetto realizzato per il corso di *Progettazione, Algoritmi e Computabilità* - Laurea Magistrale in Ingegneria Informatica.

---

## 🛠️ Technology Stack

Il sistema è suddiviso in due moduli principali:

* **Backend & Core Logic:** ![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-green) ![Maven](https://img.shields.io/badge/Maven-3.6-blue)
    * Gestione API REST.
    * Motore di Ottimizzazione (Algoritmo di Bellman/Knapsack-like).
    * Persistenza dati (H2 Database / JPA).

* **Machine Learning Service:** ![Python](https://img.shields.io/badge/Python-3.9-yellow) ![Flask](https://img.shields.io/badge/Flask-2.0-lightgrey) ![Scikit-Learn](https://img.shields.io/badge/scikit--learn-F7931E)
    * Predizione del degrado gomme basata su dati storici e telemetria.
    * Esposizione API per il backend Java.

* **Frontend:** HTML5, CSS3, JavaScript (Vanilla).

---

## 📋 Prerequisiti

Per eseguire il progetto in locale assicurarsi di avere installato:

* **Java JDK 17+**
* **Apache Maven 3.6+**
* **Python 3.9+** (con `pip`)
* Browser Web (Chrome, Firefox, Edge)

---

## 🚀 Quick Start (Avvio Automatico)

Per facilitare l'avvio dei microservizi, è stato predisposto uno script di automazione per sistemi Windows.

1.  Clona il repository o scarica l'archivio.
2.  Posizionati nella cartella principale (root) del progetto.
3.  Fai doppio click sul file **`start.bat`** (o esegui `.\start.bat` da terminale).

Lo script eseguirà automaticamente:
* Creazione del virtual environment Python e installazione delle dipendenze (`requirements.txt`).
* Build del progetto Java tramite Maven (`mvn clean install`).
* Avvio parallelo del server Python (Porta `5000`) e del server Java (Porta `8080`).

> **Nota:** Al primo avvio l'operazione potrebbe richiedere qualche minuto per scaricare le librerie Maven e i pacchetti Python.

---

## ⚙️ Installazione Manuale

Se preferisci avviare i servizi manualmente, segui questi passaggi in due terminali separati.

### 1. Avvio Microservizio Python (ML)
```bash
cd code/ml-python
# (Opzionale) Attiva il venv
pip install -r requirements.txt
python app.py
