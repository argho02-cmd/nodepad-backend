# Nodepad Backend

Spring Boot backend for the Nodepad full-stack notes app.

## Features

- JWT-based authentication
- Email verification during registration
- Per-user note storage
- PostgreSQL persistence
- CORS support for the Vercel frontend

## Required environment variables

```env
DB_URL=jdbc:postgresql://...
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...
MAIL_FROM=ghoshargho06@gmail.com
BREVO_API_KEY=...
CORS_ALLOWED_ORIGINS=https://nodepad-react.vercel.app
```

## Optional variables

```env
MAIL_FROM=ghoshargho06@gmail.com
```

## Local development

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The frontend should use `http://localhost:8080` during local development.

## Main API routes

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/verify`
- `POST /api/v1/auth/authenticate`
- `GET /api/notes`
- `POST /api/notes`
- `PUT /api/notes/{noteId}`
- `DELETE /api/notes/{noteId}`
- `GET /api/health`
