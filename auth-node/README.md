# Mini Social Auth (Node)

This lightweight Express service mirrors the Java login flow so other clients
can authenticate against the same MySQL database.

## Getting started

```bash
cp example.env .env
npm install
npm run dev
```

The service exposes the following REST endpoints:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`

The payloads match the Java backend, so the same clients/mobile apps can swap
between either stack without changes.

