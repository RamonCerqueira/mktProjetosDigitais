# Marketplace Fullstack (Next.js + TypeScript)

Base consolidada em **Next.js fullstack** (Node + React) com API em Route Handlers.

## Stack
- Next.js App Router
- TypeScript
- TailwindCSS
- API em `/src/app/api/v1`
- Camadas server em `/src/server`

## O que já está implementado
- Auth básica: `register`, `login`, `forgot-password`, `reset-password`.
- Projetos: criação/listagem com paginação.
- Upload de assets por projeto (imagem + docs) com limite de tamanho e validação de MIME.
- Dashboard, admin e rotas de compatibilidade para manter o frontend funcional.
- Middleware com headers de segurança.
- Persistência local simples em JSON (`.data/db.json`) + arquivos em `.uploads/`.

## Endpoints (base atual)
- `GET /api/v1/health`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `GET /api/v1/projects`
- `POST /api/v1/projects`
- `GET /api/v1/projects/{id}/assets`
- `POST /api/v1/projects/{id}/assets`
- `GET /api/v1/dashboard`
- `GET /api/v1/subscription`
- `POST /api/v1/subscription/activate-mock`
- `POST /api/v1/subscription/cancel`
- `POST /api/v1/subscription/renew`
- Rotas admin de compatibilidade em `/api/v1/admin/**`

## Lacunas para produção (diagnóstico)
Para um go-live enterprise, ainda faltam:
1. Banco transacional real (Postgres) com migrações e constraints.
2. Sessões/JWT robustos (refresh token, revogação, rotação de chaves).
3. RBAC/ABAC completo em middleware server-side.
4. Rate limit distribuído + antifraude com trilha de auditoria persistente.
5. Filas/eventos assíncronos para notificações/pagamentos/webhooks.
6. Observabilidade completa (logs estruturados, tracing, métricas, alertas).
7. Fluxo financeiro de produção (gateway, idempotência, reconciliação).
8. Testes automatizados de integração/E2E cobrindo fluxos críticos.

## Sugestões de novas funcionalidades
- Escrow transacional e disputa com SLA.
- Reputação avançada de seller com score de confiança explicável.
- Busca semântica + recomendações por similaridade de projetos.
- Moderação assistida por IA para fraude/spam em anúncios e mensagens.
- Multi-tenant + billing por workspace.
- Painel financeiro com MRR, churn, cohort e LTV.
- Notificações omnichannel (in-app, email, WhatsApp) com preferências.
- Assinatura e checkout localizados (PIX/cartão/boleto) para Brasil.

## Variáveis de ambiente
Ver `.env.example`:
- `NEXT_PUBLIC_API_URL`
- `DATABASE_URL`
- `JWT_SECRET`
- `NEXT_PUBLIC_UPLOAD_MAX_BYTES`

## Rodar local
```bash
cp .env.example .env
./scripts/build.sh
./scripts/up.sh
```
