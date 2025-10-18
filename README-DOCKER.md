# Docker Setup za NewNow Aplikaciju

## Struktura

- **Backend**: Spring Boot (Java 21) na portu 8080
- **Frontend**: Angular + Nginx na portu 4200 (mapiran na 80 u kontejneru)
- **Database**: MySQL 8.0 na portu 3306

## Kako pokrenuti

### 1. Pokreni sve servise

```bash
docker-compose up -d
```

### 2. Proveri status kontejnera

```bash
docker-compose ps
```

### 3. Prati logove

```bash
# Svi servisi
docker-compose logs -f

# Samo backend
docker-compose logs -f backend

# Samo frontend
docker-compose logs -f frontend

# Samo baza
docker-compose logs -f db
```

### 4. Zaustavi servise

```bash
docker-compose down
```

### 5. Zaustavi i obriši volumes (baza podataka)

```bash
docker-compose down -v
```

## Pristup aplikaciji

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api
- **MySQL**: localhost:3306
  - Database: `newnow`
  - Username: `newnow_user`
  - Password: `newnow_password`
  - Root Password: `root_password`

## Development

### Rebuild kontejnera nakon promena

```bash
# Rebuild svih servisa
docker-compose up -d --build

# Rebuild samo backend-a
docker-compose up -d --build backend

# Rebuild samo frontend-a
docker-compose up -d --build frontend
```

### Pristup kontejneru

```bash
# Backend
docker exec -it newnow-backend sh

# Frontend
docker exec -it newnow-frontend sh

# Database
docker exec -it newnow-db psql -U newnow_user -d newnow
```

### Provera baze podataka

```bash
docker exec -it newnow-db mysql -u newnow_user -pnewnow_password newnow

# SQL komande:
SHOW TABLES;              # Lista tabela
DESCRIBE table_name;      # Struktura tabele
SELECT * FROM users;
```

## Troubleshooting

### Backend ne može da se poveže na bazu

```bash
# Proveri health baze
docker-compose logs db

# Restartuj backend
docker-compose restart backend
```

### Frontend ne može da se poveže na backend

Proveri da li backend radi:

```bash
curl http://localhost:8080/api/health
```

### Greška pri build-u

```bash
# Očisti Docker cache
docker system prune -a

# Rebuild bez cache-a
docker-compose build --no-cache
```

## Production Build

Za production, izmeni `docker-compose.yml`:

1. Ukloni mapiranje portova (samo Nginx na 80)
2. Dodaj environment variables za production
3. Koristi Docker secrets za lozinke

## Korisni Docker Comandе

```bash
# Pregledaj sve Docker procese
docker ps -a

# Pregledaj sve Docker images
docker images

# Očisti nekorišćene images
docker image prune

# Pregledaj volumes
docker volume ls

# Očisti nekorišćene volumes
docker volume prune

# Pregledaj mreže
docker network ls
```

## PostgreSQL umesto MySQL

Ako želiš PostgreSQL, izmeni `docker-compose.yml`:

```yaml
db:
  image: postgres:16-alpine
  environment:
    POSTGRES_DB: newnow
    POSTGRES_USER: newnow_user
    POSTGRES_PASSWORD: newnow_password
  ports:
    - "5432:5432"
  volumes:
    - postgres_data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U newnow_user -d newnow"]
  # ... ostalo
```

I u backend servisu:

```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/newnow
    # ... ostalo
```
