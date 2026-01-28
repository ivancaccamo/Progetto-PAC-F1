# SPS-F1 🏎️
### Strategic Prediction System for Formula 1

**SPS-F1** è un sistema di supporto decisionale progettato per ottimizzare le strategie di gara in Formula 1. Il progetto combina tecniche di **Machine Learning** (per la predizione del degrado gomme) e algoritmi di **Programmazione Dinamica** (per il calcolo della strategia ottimale dei pit-stop) in un'architettura a microservizi.

Progetto realizzato per il corso di *Progettazione, Algoritmi e Computabilità* - Laurea Magistrale in Ingegneria Informatica.

---

## Technology Stack

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

## Prerequisiti

Per eseguire il progetto in locale assicurarsi di avere installato:

* **Java JDK 17+**
* **Apache Maven 3.6+**
* **Python 3.9+** (con `pip`)
* Browser Web (Chrome, Firefox, Edge)

---

## Quick Start (Avvio Automatico)

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

## Installazione Manuale

Se preferisci avviare i servizi manualmente, segui questi passaggi in due terminali separati.

### 1. Avvio Microservizio Python (ML)
Questo modulo gestisce le predizioni di Machine Learning.

Spostati nella cartella del modulo Python
cd code/ml-python

Installa le dipendenze necessarie
pip install -r requirements.txt

Avvia il server Flask
python app.py
Il servizio sarà attivo su: http://localhost:5000

### 2. Avvio Backend Java (App)
Questo modulo gestisce la logica di business, l'algoritmo di ottimizzazione e l'interfaccia web.

Apri un NUOVO terminale e spostati nella cartella Java
cd code/backend-java

Avvia l'applicazione con Maven
mvn spring-boot:run
L'applicazione sarà attiva su: http://localhost:8080

## 🧪 Testing e Qualità Software

Il progetto è stato validato attraverso diverse metodologie di testing per garantire affidabilità, robustezza e manutenibilità del codice:

* **Unit Testing (JUnit 5):**
    È stata sviluppata una suite di test unitari per verificare la logica di business del backend, con particolare attenzione al core algoritmico (`OptimizationEngine`).
    Per eseguire i test automatizzati:
    ```
    mvn test
    ```

* **API Testing (Postman):**
    La corretta esposizione degli endpoint REST e l'integrazione tra il Backend Java e il servizio Python sono state verificate tramite collection di test Postman (verifica status code 200, payload JSON corretti e gestione errori).

* **Analisi Statica (JDepend & Linting):**
    * **JDepend:** Utilizzato per analizzare le metriche architetturali, garantendo l'assenza di dipendenze cicliche e un corretto bilanciamento tra astrattezza e instabilità dei package.
    * **Linting:** Analisi statica integrata per assicurare la conformità agli standard di *Clean Code* (naming convention, rimozione codice morto).

* **Code Coverage (JaCoCo):**
    L'analisi della copertura del codice ha raggiunto livelli >90% sulle componenti critiche del Service layer. Per generare il report di copertura:
    ```
    mvn jacoco:report
    ```

---

## 👥 Autori

Progetto realizzato dal **Team SPS-F1**:

* **Andrea Birolini** (Matr. *1087070*)
* **Ivan Caccamo** (Matr. *1085892*)
* **Luca Rossi** (Matr. *1086223*)

---

### 🎓 Riferimenti Accademici

**Università degli Studi di Bergamo**
Dipartimento di Ingegneria Gestionale, dell'Informazione e della Produzione  
Corso di Laurea Magistrale in Ingegneria Informatica    

**Corso:** Progettazione, Algoritmi e Computabilità (38090-MOD1)  
**Docente:** Prof.ssa Patrizia Scandurra  
**Anno Accademico:** 2025/2026  
