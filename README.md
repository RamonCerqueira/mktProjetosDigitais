# Marketplace de Projetos Digitais

Monorepo organizado em duas pastas principais:
- `backend/`: API Spring Boot com autenticação, RBAC, assinatura, antifraude, auditoria, geolocalização, observabilidade e integrações externas.
- `frontend/`: aplicação Next.js com fluxos de cadastro, login, assinatura, dashboard, marketplace público e páginas legais.

## Stack
- Backend: Java 25, Spring Boot, Spring Security, JPA/Hibernate, PostgreSQL, Redis cache, RabbitMQ, Actuator, Prometheus metrics, eventos assíncronos, WebSocket, Docker
- Frontend: Next.js (TypeScript), TailwindCSS, Axios, Jest

## Observabilidade e rastreamento
- Logs estruturados em formato logfmt via `logback-spring.xml`
- `X-Trace-Id` por requisição para correlação ponta a ponta
- Eventos publicados em envelope com `traceId`, tipo, timestamp e payload
- Endpoints de monitoramento expostos via Spring Boot Actuator: `health`, `info`, `metrics`, `prometheus`
- Filas RabbitMQ separadas para auditoria, notificações e integrações

## Infra completa
- `backend/Dockerfile` e `frontend/Dockerfile` para builds isolados
- `docker-compose.yml` com PostgreSQL, Redis, RabbitMQ, backend e frontend
- `.env.example`, `backend/.env.example` e `frontend/.env.example` para configuração por ambiente
- Scripts utilitários: `scripts/build.sh` e `scripts/up.sh`

## Documentação legal e operacional
- Termos de uso: `/terms`
- Política de privacidade: `/privacy`
- Regras do marketplace: `/marketplace-rules`

## Como rodar com Docker
```bash
cp .env.example .env
docker compose --env-file .env up --build
```

## Como rodar com scripts
```bash
./scripts/build.sh
./scripts/up.sh
```

## Variáveis de ambiente principais
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`
- `SPRING_RABBITMQ_HOST`, `SPRING_RABBITMQ_PORT`, `SPRING_RABBITMQ_USERNAME`, `SPRING_RABBITMQ_PASSWORD`
- `APP_JWT_SECRET`, `APP_JWT_ACCESS_EXPIRATION_MS`, `APP_JWT_REFRESH_EXPIRATION_MS`
- `APP_SUBSCRIPTION_PLAN_PRICE`, `APP_SUBSCRIPTION_DURATION_DAYS`, `APP_EVENTS_ENABLED`
- `NEXT_PUBLIC_API_URL`
