## Project topology

- `social-be`: Spring Boot API (monolithic) with REST + WebSocket support.
- `social-fe`: Vite/React single-page client.
- `auth-node`: Lightweight Node.js auth microservice that shares the same MySQL schema.

## Proposed folder layout

```
mini-social-main/
├─ apps/
│  ├─ backend/        # move current social-be
│  ├─ frontend/       # move current social-fe
│  └─ auth-service/   # move current auth-node
├─ packages/
│  └─ shared-config/  # env templates, eslint/prettier, TypeScript types, etc.
├─ docker/
│  ├─ mysql/
│  └─ services/       # compose files per environment
└─ docs/
   └─ ARCHITECTURE.md
```

This layout keeps each deployable inside `apps/` (easy for Nx/Turbo/Just builds) and isolates any reusable config/scripts under `packages/`.

## Backend refinements

1. **Feature modules**  
   Inside `apps/backend/src/main/java/com/example/social/`, introduce feature modules (`friend/`, `post/`, `message/`, `auth/`) and keep controller/service/repository under each. Spring supports component scanning per package, so moving classes does not require extra configuration.

2. **API contracts**  
   Promote DTOs to `apps/backend/src/main/java/com/example/social/contracts/<feature>` so both controllers and messaging components reuse the same payload objects.

3. **Configuration segregation**  
   Split `application.yml` by profile (`application-dev.yml`, `application-prod.yml`) and move sensitive overrides into `apps/backend/config/` to be mounted via environment variables or Docker secrets.

## Shared tooling

- Create `packages/shared-config/eslint.config.js` and have both `social-fe` and `auth-node` extend it.
- Add `packages/shared-config/.env.example` referenced by every app. This avoids desynchronised environment defaults.

## Deployment pipeline

- Move Docker assets under `docker/` (one compose file for local dev, another for prod with overrides). Point services to the `apps/*` directories after the move.
- Add `Makefile` or `justfile` at repo root to orchestrate `install`, `lint`, `test`, `build`, and `compose up` targets across apps.

## Migration plan

1. Create the `apps/` and `packages/` directories without moving code yet.
2. Update all relative paths (npm scripts, Maven wrapper, README).
3. Move each project into its new subdirectory; verify builds/tests per app.
4. Deduplicate configs into `packages/shared-config`.
5. Update CI pipelines (GitHub Actions / Jenkins) to call the new paths.

Following this plan keeps git history intact (via `git mv`) and surfaces a clear separation between deployables, making future scaling easier.


