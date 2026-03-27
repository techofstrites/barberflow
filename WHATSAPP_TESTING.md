# Guia de Testes — Chatbot WhatsApp

## Como o chatbot funciona

O BarberFlow usa uma **máquina de estados finitos (FSM)** para conduzir a conversa. Cada cliente tem uma sessão de conversa com estados que avançam conforme as mensagens trocadas.

```
IDLE ──► GREETING ──► COLLECTING_SERVICE ──► COLLECTING_PROFESSIONAL
                                                        │
                                          COLLECTING_DATE ◄──────┘
                                                  │
                                          CONFIRMING ──► CONFIRMED
                                                  └────► CANCELLED
```

---

## Modo STUB (sem WhatsApp real)

Quando `WHATSAPP_API_TOKEN` não está configurado, o gateway opera em **modo stub**: as respostas do bot são apenas logadas, sem envio real. Você pode ver tudo nos logs:

```bash
docker logs barberflow-api -f | grep -i "STUB\|stub"
```

---

## Pré-requisito: criar tenant e dados base

Antes de testar o bot, você precisa de um tenant com profissional e serviço cadastrado. Se já fez isso, pule para a próxima seção.

```bash
# 1. Criar tenant
curl -X POST http://localhost:8080/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "minha-barbearia",
    "name": "Minha Barbearia",
    "adminEmail": "admin@barbearia.com",
    "adminPassword": "senha1234"
  }'
# Anote o tenantId retornado!

# 2. Login para obter token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@barbearia.com","password":"senha1234"}'
# Anote o accessToken e tenantId

# Defina variáveis (substitua pelos valores reais)
TOKEN="seu_access_token_aqui"
TENANT="seu_tenant_id_aqui"

# 3. Criar profissional
curl -X POST http://localhost:8080/api/v1/professionals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT" \
  -d '{"name":"Carlos Barbeiro","specialties":["Corte","Barba"]}'

# 4. Criar serviço
curl -X POST http://localhost:8080/api/v1/services \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT" \
  -d '{"name":"Corte masculino","price":35.00,"durationMinutes":30}'
```

---

## Testando o chatbot localmente (modo stub)

Substitua `SEU_TENANT_ID` pelo UUID do seu tenant em todos os comandos.

O campo `phone_number_id` no payload é usado para identificar o tenant — **use o UUID do seu tenant aqui**.

---

### Fluxo completo — Agendamento

#### Passo 1: Cliente manda "oi"

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/whatsapp \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "5511980635589",
            "type": "text",
            "text": { "body": "oi" }
          }],
          "metadata": { "phone_number_id": "c5fb74b3-ea63-40c6-be4d-985ffc4d33b5" }
        }
      }]
    }]
  }'
```

**Bot responde (nos logs):**
> "Olá! Bem-vindo. O que você gostaria de fazer? [Agendar horário | Cancelar agendamento | Informações]"

---

#### Passo 2: Cliente quer agendar

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/whatsapp \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "5511999990001",
            "type": "text",
            "text": { "body": "quero agendar" }
          }],
          "metadata": { "phone_number_id": "SEU_TENANT_ID" }
        }
      }]
    }]
  }'
```

**Bot responde:**
> "Ótimo! Qual serviço você deseja?"

---

#### Passo 3: Cliente escolhe o serviço

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/whatsapp \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "5511999990001",
            "type": "text",
            "text": { "body": "corte" }
          }],
          "metadata": { "phone_number_id": "SEU_TENANT_ID" }
        }
      }]
    }]
  }'
```

**Bot responde:**
> "Com qual profissional você prefere? ..."

---

#### Passo 4: Cliente escolhe o profissional

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/whatsapp \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "5511999990001",
            "type": "text",
            "text": { "body": "Carlos" }
          }],
          "metadata": { "phone_number_id": "SEU_TENANT_ID" }
        }
      }]
    }]
  }'
```

**Bot responde:**
> "Qual data e horário você prefere? (ex: amanhã às 10h)"

---

#### Passo 5: Cliente informa a data

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/whatsapp \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "5511999990001",
            "type": "text",
            "text": { "body": "quinta às 14h" }
          }],
          "metadata": { "phone_number_id": "SEU_TENANT_ID" }
        }
      }]
    }]
  }'
```

**Bot responde:**
> "Confirmar agendamento: Corte masculino com Carlos, quinta às 14h? [Confirmar | Cancelar]"

---

#### Passo 6: Cliente confirma

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/whatsapp \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "5511999990001",
            "type": "interactive",
            "interactive": {
              "button_reply": { "id": "confirm", "title": "Confirmar" }
            }
          }],
          "metadata": { "phone_number_id": "SEU_TENANT_ID" }
        }
      }]
    }]
  }'
```

---

### Fluxo — Cancelamento

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/whatsapp \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "5511999990001",
            "type": "text",
            "text": { "body": "quero cancelar meu agendamento" }
          }],
          "metadata": { "phone_number_id": "SEU_TENANT_ID" }
        }
      }]
    }]
  }'
```

---

## Visualizar logs em tempo real

```bash
# Todas as mensagens do bot (stub)
docker logs barberflow-api -f 2>&1 | grep -i "stub\|orchestrator\|intent"

# Conversa completa de um número específico
docker logs barberflow-api -f 2>&1 | grep "5511999990001"

# Ver estado atual das conversas no banco
docker exec -it barberflow-postgres psql -U barberflow -d barberflow \
  -c "SELECT customer_phone, state, created_at FROM conversations ORDER BY created_at DESC LIMIT 10;"
```

---

## Configurar WhatsApp real (Meta Business API)

### 1. Pré-requisitos

- Conta Meta Business verificada
- Número de telefone aprovado no WhatsApp Business
- App criado em [developers.facebook.com](https://developers.facebook.com)

### 2. Configurar variáveis de ambiente

```bash
# No .env ou nas variáveis do servidor
WHATSAPP_API_TOKEN=EAAxxxxxxxxxxxxxxx      # Token permanente ou temporário
WHATSAPP_PHONE_NUMBER_ID=1234567890       # ID do número no Meta
WHATSAPP_WEBHOOK_VERIFY_TOKEN=meu_token_secreto
```

### 3. Expor o endpoint publicamente (desenvolvimento)

Use [ngrok](https://ngrok.com) para expor o localhost:

```bash
ngrok http 8080
# Anote a URL: https://xxxx.ngrok.io
```

### 4. Configurar webhook no Meta

1. Acesse **Meta Developer Console → Seu App → WhatsApp → Configuração**
2. Em **Webhook**, clique em **Editar**
3. URL do callback: `https://xxxx.ngrok.io/api/v1/webhooks/whatsapp`
4. Token de verificação: o valor de `WHATSAPP_WEBHOOK_VERIFY_TOKEN`
5. Assine o evento: `messages`
6. Clique em **Verificar e salvar**

### 5. Mapear phone_number_id → tenant

O webhook usa o `phone_number_id` do Meta para identificar qual tenant deve receber a mensagem. Configure o `phone_number_id` real do Meta no campo correspondente do tenant:

```bash
docker exec -it barberflow-postgres psql -U barberflow -d barberflow \
  -c "UPDATE tenants SET whatsapp_phone_number_id = '1234567890' WHERE id = 'SEU_TENANT_ID';"
```

> **Nota:** O tenant é resolvido pelo campo `phone_number_id` do payload. Sem esse mapeamento, o bot não saberá a qual barbearia a mensagem pertence.

---

## Solução de problemas

| Sintoma | Causa | Solução |
|---------|-------|---------|
| `400 Bad Request` no webhook | Payload malformado | Verificar estrutura do JSON |
| Bot não responde | Tenant não encontrado pelo `phone_number_id` | Conferir se o UUID no payload corresponde ao tenantId |
| `[WhatsApp STUB]` nos logs | Token não configurado | Normal em desenvolvimento — configure `WHATSAPP_API_TOKEN` para envio real |
| Mensagens duplicadas | Meta reenvia se não receber 200 em 20s | A API sempre retorna 200 OK, verificar se o container está saudável |
| Conversa travada num estado | Estado salvo no banco | `DELETE FROM conversations WHERE customer_phone = '55...' AND tenant_id = '...';` |
