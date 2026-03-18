# TestJava1

Local startup for the ASTERIX integration:

```powershell
.\build-backend.ps1
.\start-backend.ps1
.\start-frontend.ps1
```

Start everything together in two new windows:

```powershell
.\start-dev.ps1
```

Notes:

- The backend uses the project's `mvnw.cmd` first and only falls back to local Maven if needed.
- If `JAVA_HOME` is not set, the backend uses the default local IntelliJ JDK configured on the machine.
- ASTERIX definition files and sample files are fully stored in the Java project under `src/resources`.
- Included samples contain large `.ast` files and the full `jasterix` `.bin` examples from `testdata\jasterix`.
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Backend upload limit: `1GB`
- Preview limit per category page: `10` records via `asterix.decoder.preview-limit`
- Upload endpoint: `POST http://localhost:8080/api/asterix/analyze`
- Sample list: `GET http://localhost:8080/api/asterix/samples`
- Sample analysis: `POST http://localhost:8080/api/asterix/samples/{sampleId}/analyze`
