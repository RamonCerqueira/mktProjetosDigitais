# Marketplace de Projetos Digitais

Monorepo com backend Spring Boot + frontend Next.js para um marketplace de micro-SaaS com monetização por assinatura mensal.

## Stack
- Backend: Java 25, Spring Boot, Spring Security, JPA/Hibernate, PostgreSQL, JWT + refresh token, WebSocket, Redis-ready, Docker
- Frontend: Next.js (TypeScript), TailwindCSS, Axios

## Como rodar com Docker
```bash
docker compose up --build
```

## Como rodar localmente
### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Regras de negócio implementadas
- Apenas usuários SELLER com assinatura ACTIVE e não expirada podem publicar projetos.
- Se a assinatura expira, os projetos do seller são ocultados automaticamente da listagem pública.
- Validação de assinatura em ações críticas no backend.
- Assinatura mockada com plano único de R$ 9,90/mês.
