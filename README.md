# Shift Scheduler

Full-stack shift scheduling application — React frontend + Spring Boot backend in one repo.

## 📁 Project Structure

```
shift-scheduler/
├── backend/                    # Spring Boot API
│   ├── src/                    # Java source code
│   ├── pom.xml
│   ├── scripts/                # Build & start scripts
│   ├── docs/                   # Documentation
│   └── deployment/
│       ├── Dockerfile          # Backend production image
│       ├── docker-compose.yml  # Backend + DB only (dev)
│       ├── docker-compose.prod.yml  # Backend + DB only (prod)
│       └── nginx/nginx.conf    # Reverse-proxy config (prod)
├── frontend/                   # React application
│   ├── src/
│   ├── public/
│   ├── Dockerfile              # Dev image (react-scripts)
│   ├── Dockerfile.prod         # Prod image (nginx + built assets)
│   └── nginx.conf              # nginx config for serving React
├── docker-compose.yml          # Full stack — dev  (all services)
├── docker-compose.prod.yml     # Full stack — prod (all services)
└── .env.example                # Environment variable reference
```

## 🚀 Quick Start (Docker — recommended)

### 1. Configure environment

```bash
cp .env.example .env
# Edit .env — at minimum set: DB_PASSWORD, JWT_SECRET
```

### 2. Run everything — development

```bash
docker-compose up --build
```

| Service  | URL                    |
|----------|------------------------|
| Frontend | http://localhost:3000  |
| Backend  | http://localhost:8080  |
| Postgres | localhost:5432         |

> The React dev server proxies API calls to the backend container automatically via `REACT_APP_API_URL`.

### 3. Run everything — production

```bash
# Set DB_USERNAME, DB_PASSWORD, JWT_SECRET, REACT_APP_API_URL in .env
docker-compose -f docker-compose.prod.yml up --build -d
```

Everything is served through nginx on port 80:

| Path    | Routes to                     |
|---------|-------------------------------|
| `/api/` | Spring Boot backend (:8080)   |
| `/*`    | React static build (nginx)    |

---

## 🛠 Local Development (without Docker)

### Prerequisites
- Java 17+, Maven 3.6+
- Node.js 18+
- PostgreSQL 15+

### Backend

```bash
cd backend
# Start PostgreSQL first, then:
mvn spring-boot:run
# API available at http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm start
# UI available at http://localhost:3000
```

---

## 🐳 Backend-only Docker (no frontend)

```bash
cd backend/deployment
docker-compose up --build -d
```
