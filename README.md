# Marketplace Fullstack (Next.js + TypeScript)

Este repositório foi consolidado para **100% Next.js fullstack** (Node/React), removendo o backend Java.

## Stack
- Next.js App Router (frontend + backend em `/app/api`)
- TypeScript
- TailwindCSS
- Prisma
- Zod
- Componentização estilo shadcn (`src/components/ui`) + bloco visual estilo magicUI (`src/components/magic`)

## Estrutura principal
- `frontend/src/app/**`: páginas da aplicação
- `frontend/src/app/api/v1/**`: endpoints backend em TypeScript
- `frontend/src/server/**`: camadas server (db, auth, schemas, helpers)
- `frontend/prisma/schema.prisma`: modelo de dados consolidado

## Endpoints disponíveis (nova base)
- `GET /api/v1/health`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/projects`
- `POST /api/v1/projects`
- `GET /api/v1/projects/{id}/assets`
- `POST /api/v1/projects/{id}/assets`

## Upload de arquivos
Suporta:
- Imagens: JPEG, PNG, WEBP, GIF
- Documentos: ZIP, PDF, DOC, DOCX

Regras de segurança:
- Limite por arquivo via `NEXT_PUBLIC_UPLOAD_MAX_BYTES`
- Sanitização de nome de arquivo
- Armazenamento local em `.uploads/` (simulação S3)

## Variáveis de ambiente
Veja `.env.example`:
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

## Observação de migração
A base foi reorganizada para backend em Next.js Route Handlers e camadas server em TypeScript. O backend Java foi removido para evitar duplicidade arquitetural.
