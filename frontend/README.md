# React Frontend mit Clean Architecture

Dieses Verzeichnis ist eine eigenständige React-Frontend-Basis, getrennt vom bestehenden Java-Code im Projekt.

## Struktur

```text
frontend/
  src/
    domain/
    application/
    infrastructure/
    presentation/
```

## Abhängigkeitsregel

- `presentation` darf `application` verwenden.
- `application` darf `domain` verwenden.
- `infrastructure` implementiert Verträge aus `domain`.
- `domain` kennt keine React-, HTTP- oder Framework-Details.

## Start

```bash
mvn spring-boot:run

cd frontend
npm install
npm run dev
```

Danach spricht das Frontend mit dem Backend unter `http://localhost:8080/api`.
Swagger UI liegt unter `http://localhost:8080/swagger-ui/index.html`.

## Nächste sinnvolle Schritte

- Feature-Module ergänzen, zum Beispiel `auth`, `users` oder `orders`.
- Bei wachsender App pro Feature eigene Unterordner in allen Schichten anlegen.
