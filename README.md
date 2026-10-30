# SeppyLingo
## Eine Sprachlern-App, die wirklich Spass macht

Das Ziel unserer Web Application ist zu zeigen dass Sprachen lernen nicht nur harte Arbeit sein muss, sondern dass man auch Spass dabei haben kann.

Um dies zu zeigen haben wir uns auf das den Ansatz der Gamification konzentriert
## Unser Tech Stack



Infrastruktur:[Docker](https://www.docker.com/), [PostgresSQL](https://www.postgresql.org/)

Backend: [Java 21](https://www.oracle.com/java/technologies/downloads/), [Spring Boot](https://spring.io/projects/spring-boot)

Frontend: [Angular 20](https://v20.angular.dev/), [Phaser](https://phaser.io/), [RxJS](https://rxjs.dev/)

KI Integration: [Google Gemini API](https://ai.google.dev/gemini-api/docs)

Security: [Spring Security](https://spring.io/projects/spring-security), [JWT](https://www.jwt.io/) (JSON Web Tokens)
## Setup

### Voraussetzungen
- [ ]  [JDK](https://www.oracle.com/java/technologies/downloads/) 21 oder hoeher
- [ ]  [npm](https://www.npmjs.com/) v11.7.0 oder hoeher
- [ ]  [Node.js](https://nodejs.org/en) v22.20.0 oder hoeher
- [ ]  [Docker Desktop](https://www.docker.com/) 
- [ ]  [Angular CLI](https://v20.angular.dev/)  20.3.13 oder hoeher
- [ ]  [Gemini API Key](https://ai.google.dev/gemini-api/docs)

### Deployment
Dank Docker kann das gesamte System (Frontend, Backend & Datenbank) mit nur einem Befehl gestartet werden:

```bash
docker compose up -d
```
Einzelne Dienste starten:

Frontend ```docker compose up -d frontend```

Backend```docker compose up -d backend```

Database```docker compose up -d db```

## Benutzung
Sobald die Container laufen, ist die App im Browser erreichbar
```localhost:3000/```

Einstufung: Absolviere den Einstufungstest

Dashboard: Wähle deinen bevorzugten Lerntyp.

Arcade-Bereich: Spiele Games mit integrierten Lerninhalten.

Fragen? Frag einfach "Seppy" im Chat

## KI Funktionen
Damit das Backend vollständig funktioniert, muss in der Datei ```backend/src/main/resources/application.properties```
ein API Key eingetragen werden
```
gemini.api.key=DEIN_API_KEY
```
***





## Autoren **Team T05**
Galatidis, Giorgos | Kavakpinar, Emirhan | Klemm, Julian | Pabiasz, Nadian | Wißenberg, Roman

## Lizenz
**The "Works on my Machine" License (WOMM)**

Dieses Projekt ist lizenziert unter der WOMM-Lizenz. 
Die Autoren garantierten, dass der Code zum Zeitpunkt der Abgabe auf allen (den meisten) Rechnern
einwandfrei funktioniert hat. Für explodierende Datenbanken oder Angular-Fehlermeldungen
auf anderen Rechnern wird keine Haftung übernommen.
