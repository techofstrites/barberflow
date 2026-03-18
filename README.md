# BarberFlow

> Plataforma SaaS para barbearias brasileiras com chatbot no WhatsApp, agendamento inteligente e IA de retenção de clientes.

![Stack](https://img.shields.io/badge/Kotlin-Spring%20Boot%203.4-blue)
![Stack](https://img.shields.io/badge/Next.js-15-black)
![Stack](https://img.shields.io/badge/PostgreSQL-16-336791)
![Stack](https://img.shields.io/badge/Redis-7-red)
![Stack](https://img.shields.io/badge/Kafka-Redpanda-orange)

---

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Módulos do backend](#módulos-do-backend)
- [Frontend](#frontend)
- [Pré-requisitos](#pré-requisitos)
- [Rodando localmente](#rodando-localmente)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [API — Rotas](#api--rotas)
- [Multi-tenancy](#multi-tenancy)
- [Planos e billing](#planos-e-billing)
- [Chatbot WhatsApp](#chatbot-whatsapp)
- [Estrutura do projeto](#estrutura-do-projeto)

---

## Visão geral

O BarberFlow é um SaaS multi-tenant que oferece:

- **Chatbot no WhatsApp** — clientes agendam por linguagem natural sem intervenção humana
- **Agenda inteligente** — gestão de profissionais, serviços e horários disponíveis
- **IA de retenção** — identifica clientes inativos e dispara mensagens proativas
- **Backoffice web** — painel para gerenciar agendamentos, clientes, profissionais e serviços
- **Billing integrado** — planos Starter, Growth e Enterprise com controle de uso

---

## Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                        Monólito Modular                      │
│                                                             │
│  ┌──────┐  ┌──────┐  ┌──────────┐  ┌───────┐  ┌────────┐  │
│  │ IAM  │  │Sched │  │ Customer │  │Chatbot│  │Billing │  │
│  └──────┘  └──────┘  └──────────┘  └───────┘  └────────┘  │
│  ┌──────────────┐                                           │
│  │Recommendation│          core (DDD shared kernel)         │
│  └──────────────┘                                           │
└──────────────────────────┬──────────────────────────────────┘
                           │ eventos (Kafka/Redpanda)
              ┌────────────┼────────────┐
           PostgreSQL     Redis      Redpanda
```

**Padrões:**
- **Arquitetura Hexagonal** (Ports & Adapters) por módulo
- **DDD** — Aggregates, Domain Events, Value Objects
- **Event-Driven** — módulos se comunicam via Kafka
- **Multi-tenant** — schema único com `tenant_id` em todas as tabelas

---

## Módulos do backend

| Módulo | Responsabilidade |
|--------|-----------------|
| `core` | `AggregateRoot`, `DomainEvent`, `TenantId` — kernel compartilhado |
| `modules/iam` | Tenants, usuários, autenticação JWT, RBAC |
| `modules/scheduling` | Agendamentos, catálogo de serviços, profissionais, detecção de no-show |
| `modules/customer` | Cadastro de clientes, métricas de comportamento, preferências |
| `modules/chatbot` | Máquina de estados da conversa, webhook e gateway WhatsApp |
| `modules/recommendation` | Heurística de janela de retenção, job diário de proatividade |
| `modules/billing` | Assinaturas, planos, controle de uso mensal |
| `app` | Bootstrap Spring Boot, Flyway migrations, SecurityConfig, KafkaConfig |

---

## Frontend

Next.js 15 (App Router) com TypeScript, Tailwind CSS e shadcn/ui.

**Páginas:**

| Rota | Descrição |
|------|-----------|
| `/` | Landing page pública |
| `/register` | Cadastro de nova barbearia (3 etapas) |
| `/login` | Login com email + senha + Tenant ID |
| `/dashboard` | Agenda do dia com ações de completar/cancelar |
| `/customers` | Lista e cadastro de clientes |
| `/professionals` | Lista e cadastro de profissionais |
| `/services` | Lista e cadastro de serviços |

---

## Pré-requisitos

- Docker e Docker Compose
- JDK 21 (para rodar o backend sem Docker)
- Node.js 20+ (para rodar o frontend sem Docker)

---

## Rodando localmente

### Opção 1 — Tudo via Docker Compose

```bash
docker compose up --build
```

Serviços disponíveis:

| Serviço | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| API | http://localhost:8080 |
| Redpanda Console | http://localhost:8090 |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |

---

### Opção 2 — Infra no Docker, backend e frontend locais

**1. Subir a infra:**
```bash
docker compose -f docker-compose.infra.yml up -d
```

**2. Rodar o backend:**
```bash
./gradlew :app:bootRun --args='--spring.profiles.active=local'
```

**3. Rodar o frontend:**
```bash
cd frontend
npm install
npm run dev
```

---

### Primeiro uso — criar uma barbearia

1. Acesse http://localhost:3000 e clique em **"Cadastrar barbearia"**
2. Preencha o nome e o identificador (slug)
3. Crie o email e senha do administrador
4. **Guarde o Tenant ID** exibido na tela de sucesso — você precisará dele para fazer login

> Para recuperar o Tenant ID via banco:
> ```bash
> docker exec -it barberflow-postgres psql -U barberflow -d barberflow \
>   -c "SELECT id, name, slug, created_at FROM tenants ORDER BY created_at DESC LIMIT 5;"
> ```

---

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/barberflow` | URL do PostgreSQL |
| `DATABASE_USER` | `barberflow` | Usuário do banco |
| `DATABASE_PASSWORD` | `barberflow` | Senha do banco |
| `REDIS_URL` | `redis://localhost:6379` | URL do Redis |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Broker Kafka |
| `JWT_SECRET` | `barberflow-secret-...` | Segredo JWT (mude em produção) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Origens permitidas no CORS |
| `WHATSAPP_API_TOKEN` | _(vazio)_ | Token da API do WhatsApp Business |
| `WHATSAPP_PHONE_NUMBER_ID` | _(vazio)_ | Phone Number ID do WhatsApp |
| `WHATSAPP_WEBHOOK_VERIFY_TOKEN` | `barberflow_verify` | Token de verificação do webhook |
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | URL da API (frontend) |

---

## API — Rotas

> Todas as rotas autenticadas exigem:
> - `Authorization: Bearer <token>` — obtido no login
> - `X-Tenant-Id: <uuid>` — ID do tenant retornado no cadastro

### Auth & Tenants (público)

```
POST /api/v1/tenants          Criar nova barbearia (cadastro)
POST /api/v1/auth/login       Login — retorna accessToken e refreshToken
```

**Resposta do login:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "userId": "uuid",
  "tenantId": "uuid",
  "role": "ADMIN"
}
```

### Agendamentos

```
GET    /api/v1/appointments?from=<ISO>&to=<ISO>   Listar por intervalo de data
POST   /api/v1/appointments                        Criar agendamento
POST   /api/v1/appointments/{id}/complete          Marcar como concluído
DELETE /api/v1/appointments/{id}                   Cancelar
```

**Exemplo — criar agendamento:**
```json
{
  "customerId": "uuid",
  "professionalId": "uuid",
  "serviceIds": ["uuid"],
  "startAt": "2026-03-25T10:00:00-03:00"
}
```

**Status possíveis:** `PENDING_CONFIRMATION` · `CONFIRMED` · `IN_PROGRESS` · `COMPLETED` · `CANCELLED` · `NO_SHOW`

### Clientes

```
GET  /api/v1/customers                              Listar todos os clientes
POST /api/v1/customers                              Criar cliente
GET  /api/v1/customers/by-phone?phone=+5511999...   Buscar por telefone (E.164)
```

**Exemplo — criar cliente:**
```json
{
  "phone": "+5511999990001",
  "name": "João da Silva",
  "consentGiven": true
}
```

### Profissionais

```
GET  /api/v1/professionals   Listar profissionais ativos
POST /api/v1/professionals   Criar profissional
```

**Exemplo:**
```json
{
  "name": "Carlos Barbeiro",
  "specialties": ["Corte", "Barba"]
}
```

### Serviços

```
GET  /api/v1/services   Listar serviços ativos
POST /api/v1/services   Criar serviço
```

**Exemplo:**
```json
{
  "name": "Corte masculino",
  "price": 35.00,
  "durationMinutes": 30
}
```

### Billing

```
GET  /api/v1/billing/subscription   Consultar assinatura atual
POST /api/v1/billing/trial          Iniciar trial de 14 dias
POST /api/v1/billing/activate       Ativar plano pago
```

**Exemplo — ativar plano:**
```json
{
  "planTier": "GROWTH",
  "externalPaymentId": "pay_abc123"
}
```

**Planos disponíveis:** `STARTER` · `GROWTH` · `ENTERPRISE`

### Chatbot — Webhook WhatsApp (público)

```
GET  /api/v1/webhooks/whatsapp   Verificação do webhook (Meta)
POST /api/v1/webhooks/whatsapp   Receber mensagens do WhatsApp
```

### Health

```
GET /actuator/health   Status da aplicação
```

---

## Multi-tenancy

Cada barbearia é um **tenant** isolado. O `tenant_id` é um UUID gerado no cadastro e deve ser enviado em **todo request autenticado** via header `X-Tenant-Id`.

O `TenantFilter` resolve o tenant a partir do JWT e o armazena em `ThreadLocal`, garantindo isolamento entre tenants em todas as queries.

---

## Planos e billing

| Plano | Preço | Mensagens/mês | Profissionais |
|-------|-------|--------------|---------------|
| **Starter** | R$ 97/mês | 100 | 1 |
| **Growth** | R$ 197/mês | 500 | 5 |
| **Enterprise** | R$ 397/mês | Ilimitado | Ilimitado |

Todos os planos incluem 14 dias de trial gratuito.

---

## Chatbot WhatsApp

O chatbot usa uma **máquina de estados finitos** para conduzir a conversa:

```
IDLE → GREETING → COLLECTING_SERVICE → COLLECTING_PROFESSIONAL
     → COLLECTING_DATE → CONFIRMING → CONFIRMED / CANCELLED
```

**Modo stub:** quando `WHATSAPP_API_TOKEN` não está configurado, o gateway opera em modo stub (logs apenas, sem envio real). Útil para desenvolvimento local.

**Configurar webhook no Meta:**
1. Configure a URL `https://seu-dominio.com/api/v1/webhooks/whatsapp`
2. Use o `WHATSAPP_WEBHOOK_VERIFY_TOKEN` como token de verificação
3. Assine os eventos `messages`

---

## Estrutura do projeto

```
barberflow/
├── app/                          # Spring Boot main + configs
│   └── src/main/resources/
│       └── db/migration/
│           ├── system/           # Migrations do schema público (V1–V9)
│           └── tenant/           # Migrations por tenant (V1–V5)
├── core/                         # Shared kernel (AggregateRoot, TenantId)
├── modules/
│   ├── iam/                      # Auth, tenants, usuários
│   ├── scheduling/               # Agendamentos, serviços, profissionais
│   ├── customer/                 # Clientes
│   ├── chatbot/                  # WhatsApp FSM
│   ├── recommendation/           # Retenção IA
│   └── billing/                  # Assinaturas
├── frontend/                     # Next.js 15 backoffice
├── docker-compose.yml            # Stack completa (infra + api + frontend)
├── docker-compose.infra.yml      # Só infra (postgres + redis + redpanda)
├── Dockerfile                    # Build do backend
└── barberflow.http               # Coleção de requests (IntelliJ HTTP Client)
```
