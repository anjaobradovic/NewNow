# NewNow

A platform for discovering and managing events in a city or across the country —
undergraduate project combining the Client-Side and Server-Side Web Technologies
courses, then extended with a third (UES) course covering full-text search and
object storage.

## Stack

- **Backend** — Spring Boot 3.5.6, Java 21, Spring Data JPA, Spring Security (JWT),
  Spring Data Elasticsearch, MinIO Java SDK, Apache Tika.
- **Frontend** — Angular 20, Tailwind, ng2-charts (Chart.js), ngx-toastr.
- **Persistence** — MySQL 8 (source of truth), Elasticsearch 8.18 (search index),
  MinIO (S3-compatible object storage for images and PDFs).
- **Infra** — Docker Compose, single-node ES and MinIO for development.

## Architecture in one paragraph

The relational DB is the **system of record** — every write goes there first. A
separate Elasticsearch index (`places`) is a denormalized projection of `Location`
kept in sync from the service layer (`SearchIndexService.reindexAfterCommit(...)`
hooks fire after every `LocationService` / `ReviewService` write). Image and PDF
binaries live in **MinIO**; the DB only stores the object key. Reading them goes
through a backend proxy endpoint, never directly from the browser to MinIO.

## Features

### Functional

- Account requests, admin approve/reject (email on both paths)
- JWT auth, in-memory token blacklist on logout, audit log on critical actions
- Place + Event CRUD, manager assignment per place, role-aware authz
- Reviews per regular past event with per-category ratings
  (performance, sound & light, space, overall — each 1–10, each optional)
- Manager moderation: hide (still counted in rating) vs remove (voided)
- Replies as arbitrarily-deep threads
- Analytics dashboard per place (weekly / monthly / yearly / custom range, with
  charts)
- Profile editing, avatar upload, password change (email notification)

### UES (Elasticsearch + MinIO)

- **Place index** with custom analyzer `newnow_text`: case-folding +
  Cyrillic→Latin transliteration + asciifolding (no ES plugin required)
- **Place search** (`GET /api/search/places`) over:
  - `name`, `description` (UI-entered), `pdfDescription` (Tika-extracted)
  - `reviewCount` range, four per-category rating ranges
  - **`operator=AND|OR`** between fields (BooleanQuery must / should)
  - Per-field syntax: `"phrase"`, `prefix*`, `~fuzzy` (Auto edit distance)
  - **Sort by name** (analyzer-folded `name.keyword`)
  - **Highlighter** (`<mark>…</mark>` on name + description + PDF fragments)
- **More-like-this**: `GET /api/search/places/{id}/similar`
- **PDF upload + parse + index**: PUT/GET/DELETE
  `/api/locations/{id}/description-pdf` — stored in MinIO under
  `places/{id}/description.pdf`, text extracted by Tika, indexed into
  `pdfDescription`
- **Admin reindex**: `POST /api/admin/search/reindex` rebuilds the index from
  MySQL + MinIO (also runs on startup when
  `newnow.search.bootstrap-on-startup=true`)
- Images served via `GET /uploads/**` (streamed from MinIO by
  `UploadProxyController`)

### Cross-cutting

- Externalised secrets — every credential, JWT secret, mail config, admin seed
  password is `${ENV_VAR:dev-default}`
- Rolling file logger (`logback-spring.xml`) to `/app/logs`
- AuditLog entity actively written on approve/reject, manager assign/remove,
  password change, review hide/remove, login success/failure, logout

## Running it

Requires Docker + Docker Compose.

```bash
git clone <this repo>
cd NewNow
docker compose up --build -d
```

Five containers come up:

| Service | Host port | Purpose |
|---|---:|---|
| `newnow-frontend`     | 4200 | Angular SPA (nginx) |
| `newnow-backend`      | 8082 | Spring Boot REST |
| `newnow-db`           | 3307 | MySQL 8 |
| `newnow-elasticsearch`| 9200 | ES 8.18 single-node |
| `newnow-minio`        | 9000 / 9001 | MinIO API / console |

Open http://localhost:4200.

> The non-default backend port (`8082`) is because `8080` is commonly busy.
> Override via the `ports:` section of `docker-compose.yml` if you prefer.

### Seed credentials

| Role | Email | Password |
|---|---|---|
| Admin     | `anja.obradovic@newnow.com`      | `admin123` |
| Admin     | `marko.gordic@newnow.com`        | `admin123` |
| Manager   | `manager.novisad@newnow.com`     | `manager123` |
| Manager   | `manager.bijelopolje@newnow.com` | `manager123` |
| Manager   | `manager.budapest@newnow.com`    | `manager123` |
| User      | `petar.petrovic@gmail.com`       | `user123` |
| User      | `jelena.jovic@gmail.com`         | `user123` |
| User      | `milan.nikolic@gmail.com`        | `user123` |
| User      | `ana.kovacevic@gmail.com`        | `user123` |

Override the admin passwords in production by setting
`ADMIN_PRIMARY_PASSWORD` / `ADMIN_SECONDARY_PASSWORD`.

### MinIO console

http://localhost:9001 — log in with `minioadmin` / `minioadmin`. The `newnow`
bucket is auto-created on first start. Uploaded images and PDFs appear under
`places/`, `events/`, `avatars/`.

## Useful endpoints

```bash
# Search by Cyrillic, case-insensitive, finds the Latin-named place
curl 'http://localhost:8082/api/search/places?name=арена'

# Phrase, prefix, fuzzy
curl 'http://localhost:8082/api/search/places?name=%22Novi+Sad+Arena%22'
curl 'http://localhost:8082/api/search/places?name=Bud*'
curl 'http://localhost:8082/api/search/places?name=~areena'

# AND vs OR between fields
curl 'http://localhost:8082/api/search/places?name=arena&description=cultural&operator=OR'

# Sort + highlight (highlight is always on)
curl 'http://localhost:8082/api/search/places?pdf=seminarski&sortBy=name&sortDir=asc'

# More like this
curl 'http://localhost:8082/api/search/places/2/similar?size=5'

# Per-category rating range
curl 'http://localhost:8082/api/search/places?avgPerformanceFrom=8&avgVenueTo=6'
```

## Layout

```
backend/
  src/main/java/rs/ftn/newnow/
    config/         DataLoader, SecurityConfig, MinioConfig, SearchBootstrap, ...
    controller/     REST endpoints (Auth, Location, Event, Review, Analytics,
                    PlaceSearch, AdminSearch, LocationPdf, UploadProxy, ...)
    dto/            Request / response DTOs
    model/          JPA entities (Location, Event, Review, Rate, Manages, ...)
    repository/     Spring Data JPA repositories
    security/       JwtUtil, JwtAuthenticationFilter, TokenBlacklistService
    service/        Business logic (LocationService, ReviewService, ...)
    search/         Elasticsearch layer:
      index/        LocationIndex (@Document) — the "Index Unit"
      repository/   LocationIndexRepository (Spring Data ES)
      PlaceSearchService, PlaceSearchCriteria, SearchSyntax, BoolOperator,
      CategoryAverages, PdfTextExtractor, SearchIndexService
    storage/        ObjectStorageService (MinIO wrapper)
  src/main/resources/
    application.properties
    logback-spring.xml
    elasticsearch/place-index-settings.json   # analyzer + normalizer

frontend/
  src/app/
    pages/          Components by feature (place-search, location-edit, ...)
    services/       HTTP clients
    components/     Shared UI (navbar, ...)
    guards/         Auth / admin guards
    interceptors/   Bearer token attachment
    models/         TypeScript types
    app.routes.ts
docker-compose.yml
```

## Tests

```bash
docker run --rm -v "$PWD/backend":/app -v "$HOME/.m2":/root/.m2 \
  -w /app maven:3.9-eclipse-temurin-21 mvn test
```

The search layer in particular is exercised by `SearchSyntaxTest`,
`CategoryAveragesTest`, and `PlaceSearchServiceTest` (33 tests covering query
shape across phrase / prefix / fuzzy / plain, AND/OR, sort, highlight, per
category averages, and the hidden-vs-removed rule).

## Credits

Coursework project (Software Engineering at FTN, Novi Sad). The project brief
is in `ProjekatSVT2025.pdf` and the deployment diagram is in
`Novi-Sad-2024_25.drawio`.
