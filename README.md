# Marketplace de Projetos Digitais

Monorepo organizado em duas pastas principais:
- `backend/`: API Spring Boot com autenticação, RBAC, assinatura, antifraude, auditoria, geolocalização e integrações externas.
- `frontend/`: aplicação Next.js com fluxos de cadastro, login, assinatura, dashboard e marketplace público.

## Stack
- Backend: Java 25, Spring Boot, Spring Security, JPA/Hibernate, PostgreSQL, JWT + refresh token, WebSocket, Redis-ready, Docker
- Frontend: Next.js (TypeScript), TailwindCSS, Axios

## Segurança implementada
- JWT access token + refresh token persistido
- RBAC com `ADMIN`, `SELLER` e `BUYER`
- Headers de segurança contra XSS/clickjacking
- CSRF token para chamadas mutáveis do frontend
- Sanitização de entrada para reduzir risco de XSS
- Busca sanitizada e uso de JPA para reduzir risco de SQL Injection
- Rate limiting por rota/ator
- Logs de auditoria de requests e ações críticas
- Validação completa de CPF/CNPJ no cadastro
- Antifraude básica em ofertas e compras

## Integrações implementadas
- ViaCEP: busca de endereço por CEP no backend
- ReceitaWS: busca de dados empresariais por CNPJ no backend
- OpenStreetMap/Nominatim: reverse geocoding para converter latitude/longitude em cidade/estado
- Endpoint público de validação documental (`CPF` e `CNPJ`)
- Frontend com auto-preenchimento de endereço e razão/nome fantasia durante o cadastro
- Validação documental em tempo real na tela de registro
- Marketplace com filtros geográficos e busca com base na localização do usuário

## Geolocalização
- O frontend solicita autorização explícita ao navegador para acessar a localização do usuário.
- A localização pode ser usada no cadastro para salvar cidade/UF/latitude/longitude do usuário.
- O marketplace pode buscar projetos por cidade/UF manualmente ou a partir da geolocalização atual.
- O backend mantém índices em usuários/projetos para otimizar as queries geográficas por cidade/estado/status.

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
- Usuário sem assinatura não pode publicar nem atualizar projeto para publicação.
- Se a assinatura expira, os projetos do seller são ocultados automaticamente da listagem pública.
- Sistema valida assinatura em cada ação crítica de publicação.
- Cadastro valida CPF/CNPJ antes de criar conta.
- Antifraude bloqueia auto-compra, auto-negociação e ofertas excessivamente baixas.


## Sistema financeiro
- Comissão da plataforma de 10% por venda
- Estados da transação: `PENDING`, `HELD`, `RELEASED`, `REFUNDED`
- Escrow: após pagamento confirmado pelo Stripe, a transação fica em `HELD` até a confirmação do comprador
- Stripe: criação de checkout e webhook com validação de assinatura
- Refund e liberação de escrow auditados no backend


## Sistema de negociação
- Propostas formais com status e proponente identificado
- Contra-propostas encadeadas por negociação (`negotiationKey`)
- Aceite/rejeição apenas pela contraparte
- Histórico completo de ações (`created`, `countered`, `accepted`, `rejected`, `message_sent`)
- Chat integrado por negociação com validação de participantes
- Logs de auditoria e bloqueios contra manipulação de ofertas fechadas ou por usuários não autorizados


## Chat em tempo real
- WebSocket/STOMP no backend com persistência de mensagens
- Histórico completo por negociação
- Identificação de remetente/destinatário nas mensagens
- Frontend integrado no dashboard com atualização em tempo real


## Inteligência de projetos
- Score calculado dinamicamente com base em MRR, múltiplo de preço e completude do anúncio
- Ranking público ordenado por score
- Sugestão de preço automática baseada em múltiplo de receita
- Heurística antifraude que bloqueia anúncios com preço muito fora da faixa sugerida
