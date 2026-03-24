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
- `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`
- `APP_NOTIFICATIONS_EMAIL_ENABLED`, `APP_NOTIFICATIONS_EMAIL_FROM`
- `APP_FRONTEND_BASE_URL`, `APP_PASSWORD_RESET_EXPIRATION_MINUTES`

## SMTP Gmail (produção)
Para usar Gmail SMTP com senha de app:
1. Ative verificação em 2 etapas na conta Google.
2. Gere uma **App Password**.
3. Configure no `.env`:
   - `SPRING_MAIL_HOST=smtp.gmail.com`
   - `SPRING_MAIL_PORT=587`
   - `SPRING_MAIL_USERNAME=seu-email@gmail.com`
   - `SPRING_MAIL_PASSWORD=SUA_APP_PASSWORD`
   - `SPRING_MAIL_SMTP_AUTH=true`
   - `SPRING_MAIL_SMTP_STARTTLS=true`

## Fluxos de e-mail implementados
- Confirmação de cadastro: enviada após `POST /api/auth/register`.
- Recuperação de senha:
  - Solicitar: `POST /api/auth/forgot-password`
  - Redefinir: `POST /api/auth/reset-password`
- Notificações de negociação: oferta/mensagem/aceite/rejeição.
- Notificações de pagamento: confirmação de pagamento.


## Painel MASTER
- Rota frontend: `/admin`
- Endpoints backend protegidos: `/api/admin/**`
- Recursos: gestão de usuários, métricas, financeiro, moderação de projetos, ranking de sellers e auditoria

## Documentação interna
- OpenAPI JSON: `/api/docs/api`
- Swagger UI: `/api/docs/swagger`

## Documentação para usuários
- Guia central: `/help`
- Termos de uso: `/terms`
- Política de privacidade: `/privacy`
- Regras do marketplace: `/marketplace-rules`


## Login ADMIN inicial
- Rota do frontend: `/login`
- Endpoint do backend: `POST /api/auth/login`
- Credenciais iniciais padrão (configuráveis por variável de ambiente):
  - Email: `admin@marketplace.local`
  - Senha: `Admin123!`
- Essas credenciais são semeadas automaticamente no startup quando `APP_ADMIN_BOOTSTRAP_ENABLED=true`.

## Como inserir outros ADMINs
### Opção 1: pela própria área admin
- Faça login com o ADMIN inicial.
- Acesse `/admin`.
- Abra o detalhe de um usuário existente.
- Use a ação **Tornar ADMIN**.

### Opção 2: diretamente no PostgreSQL
#### Inserir um novo ADMIN
```sql
INSERT INTO users (
  name, email, document_number, document_type, password, role, city, state, created_at, active, blocked
) VALUES (
  'Novo Admin',
  'novo.admin@marketplace.local',
  '98765432100',
  'CPF',
  '$2a$10$wHcM4YWwYpV7t5fV0YzGQONnJm2p0Y9GmH0KQnJYyK6zA0xq0G4tK',
  'ADMIN',
  'São Paulo',
  'SP',
  NOW(),
  TRUE,
  FALSE
);

INSERT INTO user_roles (user_id, role_name)
SELECT id, 'ADMIN' FROM users WHERE email = 'novo.admin@marketplace.local';
```

#### Promover um usuário existente para ADMIN
```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'usuario@exemplo.com';

INSERT INTO user_roles (user_id, role_name)
SELECT id, 'ADMIN'
FROM users
WHERE email = 'usuario@exemplo.com'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = users.id AND ur.role_name = 'ADMIN'
  );
```

> Observação: a senha armazenada no banco precisa estar em BCrypt. O hash acima é apenas um exemplo de valor já criptografado.


## Curadoria e qualificação
- Apenas usuários com **CPF** podem ser `SELLER` e publicar projetos. Contas com **CNPJ** podem existir, mas não podem vender projetos.
- O score do projeto agora considera descrição, clareza, stack informada, MRR e usuários ativos.
- Classificação do projeto: `Baixo`, `Médio`, `Alto`.
- Reputação do dev: `Iniciante`, `Intermediário`, `Avançado`, `Top Seller`.
- Badges de confiança: `CPF verificado` e `Projeto verificado`.
- Antifraude básico: preço irreal, duplicação por seller e alerta administrativo.
