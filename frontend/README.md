# React Frontend with Clean Architecture

This directory is an independent React frontend base, separated from the existing Java code in the project.

## Structure

```text
frontend/
  src/
    domain/
    application/
    infrastructure/
    presentation/
```

## Dependency Rule

- `presentation` may use `application`.
- `application` may use `domain`.
- `infrastructure` implements contracts from `domain`.
- `domain` has no React, HTTP, or framework details.

## Run

```bash
mvn spring-boot:run

cd frontend
npm install
npm run dev
```

After that, the frontend communicates with the backend at `http://localhost:8080/api`.
Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

## Next Useful Steps

- Add feature modules such as `auth`, `users`, or `orders`.
- As the app grows, create feature-specific subfolders across all layers.
