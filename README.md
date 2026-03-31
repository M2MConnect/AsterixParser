# AsterixParser

## Requirements

- Windows PowerShell
- Node.js and npm
- JDK 21 or newer

The backend start script resolves `JAVA_HOME` first. If `JAVA_HOME` is not set, it also looks for a local JDK installation under `C:\Program Files\Eclipse Adoptium\jdk-*`.

## First-time setup

Install frontend dependencies once:

```powershell
cd frontend
npm.cmd install
cd ..
```

`npm.cmd` is used instead of `npm` because some PowerShell configurations block `npm.ps1` when script execution is restricted.

## Local startup

Build the backend:

```powershell
.\build-backend.ps1
```

Start backend only:

```powershell
.\start-backend.ps1
```

Start frontend only:

```powershell
.\start-frontend.ps1
```

Start backend and frontend together in two separate PowerShell windows:

```powershell
.\start-dev.cmd
```

Use `start-dev.cmd` instead of `start-dev.ps1` directly. On systems with a restrictive PowerShell execution policy, direct `.ps1` execution can be blocked with a "not digitally signed" error. The `.cmd` launcher starts PowerShell with `-ExecutionPolicy Bypass` for this project only.

If you explicitly want to run the PowerShell script itself, use:

```powershell
powershell.exe -ExecutionPolicy Bypass -File .\start-dev.ps1
```

If PowerShell still blocks local files because they were downloaded, run:

```powershell
Unblock-File .\start-dev.ps1
Unblock-File .\start-backend.ps1
Unblock-File .\start-frontend.ps1
```

## Runtime notes

- The backend uses the project's `mvnw.cmd` first and only falls back to local Maven if needed.
- ASTERIX definition files and sample files are fully stored in the Java project under `src/resources`.
- Included samples contain large `.ast` files and the full `jasterix` `.bin` examples from `testdata\jasterix`.
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Backend upload limit: `1GB`
- Preview limit per category page: `10` records via `asterix.decoder.preview-limit`
- Upload endpoint: `POST http://localhost:8080/api/asterix/analyze`
- Sample list: `GET http://localhost:8080/api/asterix/samples`
- Sample analysis: `POST http://localhost:8080/api/asterix/samples/{sampleId}/analyze`

## Frontend (React)

The frontend is a React + TypeScript app (Vite) located in [frontend](./frontend).

- Tech stack: React 18, TypeScript, Vite
- Architecture: Clean Architecture style with `domain`, `application`, `infrastructure`, and `presentation` layers
- Dev server: `http://localhost:5173`
- Backend API base URL: `http://localhost:8080/api`

Run frontend only:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```
