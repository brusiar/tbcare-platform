# 🧪 O Que Está Disponível Para Testes - v1.0

## 📦 BACKEND - API REST (100% Funcional)

### ✅ Health Check
```
GET /api/health
```

### ✅ Pacientes (CRUD Completo)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/patients` | Listar todos os pacientes ativos |
| POST | `/api/patients` | Criar novo paciente |
| GET | `/api/patients/{id}` | Buscar paciente por ID |
| PUT | `/api/patients/{id}` | Atualizar paciente |
| DELETE | `/api/patients/{id}` | Desativar paciente (soft delete) |

**Campos disponíveis:**
- `name` (obrigatório)
- `dateOfBirth` (opcional)
- `phone` (opcional)
- `email` (opcional)
- `notes` (opcional)

### ✅ Consultas (CRUD Completo)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/appointments?start=...&end=...` | Listar consultas por período |
| POST | `/api/appointments` | Criar nova consulta |
| GET | `/api/appointments/{id}` | Buscar consulta por ID |
| PUT | `/api/appointments/{id}` | Atualizar consulta |

**Campos disponíveis:**
- `patientId` (obrigatório - UUID)
- `userId` (obrigatório - UUID)
- `scheduledAt` (obrigatório - ISO DateTime)
- `durationMin` (opcional - padrão: 60)
- `status` (opcional - SCHEDULED, CONFIRMED, CANCELLED, COMPLETED)
- `notes` (opcional)

---

## 🎨 FRONTEND - Interface Visual (Sem Integração)

### ✅ Páginas Disponíveis

| Página | URL | Status |
|--------|-----|--------|
| Login | `http://localhost:3003/login` | ⚠️ Visual apenas |
| Dashboard | `http://localhost:3003/dashboard` | ⚠️ Sem dados reais |
| Agenda | `http://localhost:3003/agenda` | ⚠️ Sem integração |
| Pacientes | `http://localhost:3003/pacientes` | ⚠️ Sem integração |

### ✅ Componentes UI
- Sidebar com navegação funcional
- Header
- Botões (Button)
- Cards
- Badges
- Identidade visual aplicada (roxo #5a3c78)

---

## 💾 BANCO DE DADOS

### ✅ Tabelas Criadas
- `tenants` (com 1 tenant padrão)
- `users`
- `patients`
- `appointments`
- `flyway_schema_history`

### ✅ Tenant Padrão
- **ID:** `00000000-0000-0000-0000-000000000001`
- **Nome:** TB Care Demo
- **Slug:** tbcare-demo

---

## 🧪 Exemplos de Testes

### 1. Health Check

```bash
curl http://localhost:8082/api/health
```

**Resposta esperada:**
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "tbcare-platform"
  },
  "timestamp": "2024-01-01T00:00:00"
}
```

### 2. Criar Paciente

```bash
curl -X POST http://localhost:8082/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Silva",
    "dateOfBirth": "1985-03-20",
    "phone": "11987654321",
    "email": "maria@example.com",
    "notes": "Paciente com histórico de tuberculose"
  }'
```

### 3. Listar Pacientes

```bash
curl http://localhost:8082/api/patients
```

### 4. Buscar Paciente por ID

```bash
curl http://localhost:8082/api/patients/{UUID_DO_PACIENTE}
```

### 5. Atualizar Paciente

```bash
curl -X PUT http://localhost:8082/api/patients/{UUID_DO_PACIENTE} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Silva Santos",
    "phone": "11999999999"
  }'
```

### 6. Desativar Paciente

```bash
curl -X DELETE http://localhost:8082/api/patients/{UUID_DO_PACIENTE}
```

### 7. Criar Consulta

```bash
curl -X POST http://localhost:8082/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "UUID_DO_PACIENTE",
    "userId": "00000000-0000-0000-0000-000000000001",
    "scheduledAt": "2024-12-25T10:00:00",
    "durationMin": 60,
    "status": "SCHEDULED",
    "notes": "Consulta de acompanhamento"
  }'
```

### 8. Listar Consultas por Período

```bash
curl "http://localhost:8082/api/appointments?start=2024-12-01T00:00:00&end=2024-12-31T23:59:59"
```

### 9. Buscar Consulta por ID

```bash
curl http://localhost:8082/api/appointments/{UUID_DA_CONSULTA}
```

### 10. Atualizar Consulta

```bash
curl -X PUT http://localhost:8082/api/appointments/{UUID_DA_CONSULTA} \
  -H "Content-Type: application/json" \
  -d '{
    "scheduledAt": "2024-12-25T14:00:00",
    "status": "CONFIRMED"
  }'
```

---

## ⚠️ O Que NÃO Está Disponível (Próximas Etapas)

- ❌ Autenticação JWT
- ❌ Login funcional
- ❌ Proteção de rotas
- ❌ Integração frontend ↔ backend
- ❌ Formulários funcionais
- ❌ Validação de dados (Bean Validation)
- ❌ Paginação
- ❌ Filtros e busca
- ❌ Dashboard com dados reais
- ❌ Agenda interativa
- ❌ CRUD visual de pacientes
- ❌ Testes automatizados

---

## 📝 Resumo

| Componente | Status | Observação |
|------------|--------|------------|
| **Backend** | ✅ 100% funcional | API REST completa |
| **Frontend** | ⚠️ Visual apenas | Sem integração com API |
| **Banco** | ✅ Funcional | Estrutura completa |
| **Docker** | ✅ Funcional | Tudo containerizado |

### 🎯 Foco dos Testes
**API REST (backend)** - Use Postman, Insomnia, curl ou qualquer cliente HTTP

### 🎨 Frontend
**Navegação visual** - Acesse http://localhost:3003 para ver a interface

---

## 🚀 Como Testar

1. Subir o projeto:
   ```bash
   docker compose up -d --build
   ```

2. Aguardar containers ficarem healthy:
   ```bash
   docker compose ps
   ```

3. Testar health check:
   ```bash
   curl http://localhost:8082/api/health
   ```

4. Criar alguns pacientes e consultas usando os exemplos acima

5. Acessar o frontend:
   ```
   http://localhost:3003
   ```

---

## 📚 Documentação Adicional

- `README.md` - Visão geral do projeto
- `QUICKSTART.md` - Guia de início rápido
- `ARCHITECTURE.md` - Decisões arquiteturais
- `DEPLOY.md` - Guia de deploy no servidor
- `PORTS.md` - Configuração de portas
