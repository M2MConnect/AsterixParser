# TestJava1

Lokaler Start fuer die ASTERIX-Integration:

```powershell
.\build-backend.ps1
.\start-backend.ps1
.\start-frontend.ps1
```

Alles zusammen in zwei neuen Fenstern starten:

```powershell
.\start-dev.ps1
```

Hinweise:

- Das Backend nutzt zuerst `mvnw.cmd` aus dem Projekt und faellt nur bei Bedarf auf lokales Maven zurueck.
- Das Backend nutzt standardmaessig das lokale IntelliJ-JDK unter `C:\Users\Martin.Mueller\.jdks\openjdk-25.0.2`, falls `JAVA_HOME` nicht gesetzt ist.
- ASTERIX-Definitionsdateien und Beispiel-Dateien liegen vollstaendig im Java-Projekt unter `src/resources`.
- Eingebundene Samples umfassen die grossen `.ast`-Dateien sowie die kompletten `jasterix`-`.bin`-Beispiele aus `testdata\jasterix`.
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Upload-Limit Backend: `1GB`
- Vorschau-Limit pro Kategorie-Seite: `10` Records ueber `asterix.decoder.preview-limit`
- Upload-Endpoint: `POST http://localhost:8080/api/asterix/analyze`
- Sample-Liste: `GET http://localhost:8080/api/asterix/samples`
- Sample-Analyse: `POST http://localhost:8080/api/asterix/samples/{sampleId}/analyze`
