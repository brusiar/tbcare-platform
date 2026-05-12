# Exemplos de Uso da API

## Autenticação

### Login
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@tbcare.com",
    "password": "admin123"
  }'
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": "00000000-0000-0000-0000-000000000001",
    "name": "Admin User",
    "email": "admin@tbcare.com",
    "role": "ADMIN",
    "tenantId": "00000000-0000-0000-0000-000000000001"
  },
  "message": "Login successful",
  "timestamp": "2024-01-01T00:00:00"
}
```

### Obter Usuário Autenticado
```bash
TOKEN="seu-token-aqui"

curl http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "id": "00000000-0000-0000-0000-000000000001",
    "name": "Admin User",
    "email": "admin@tbcare.com",
    "role": "ADMIN",
    "tenantId": "00000000-0000-0000-0000-000000000001"
  },
  "timestamp": "2024-01-01T00:00:00"
}
```

---

## Pacientes

### Listar Pacientes
```bash
curl http://localhost:8082/api/patients \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "João Silva",
      "dateOfBirth": "1990-01-15",
      "phone": "(11) 98765-4321",
      "email": "joao@email.com",
      "notes": "Paciente regular",
      "active": true,
      "tenantId": "00000000-0000-0000-0000-000000000001"
    }
  ],
  "timestamp": "2024-01-01T00:00:00"
}
```

### Criar Paciente
```bash
curl -X POST http://localhost:8082/api/patients \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Santos",
    "dateOfBirth": "1985-05-20",
    "phone": "(11) 91234-5678",
    "email": "maria@email.com",
    "notes": "Primeira consulta"
  }'
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "id": "novo-uuid",
    "name": "Maria Santos",
    "dateOfBirth": "1985-05-20",
    "phone": "(11) 91234-5678",
    "email": "maria@email.com",
    "notes": "Primeira consulta",
    "active": true,
    "tenantId": "00000000-0000-0000-0000-000000000001"
  },
  "message": "Patient created successfully",
  "timestamp": "2024-01-01T00:00:00"
}
```

### Buscar Paciente por ID
```bash
PATIENT_ID="uuid-do-paciente"

curl http://localhost:8082/api/patients/$PATIENT_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Atualizar Paciente
```bash
curl -X PUT http://localhost:8082/api/patients/$PATIENT_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Santos Silva",
    "dateOfBirth": "1985-05-20",
    "phone": "(11) 91234-5678",
    "email": "maria.santos@email.com",
    "notes": "Paciente regular - atualizado"
  }'
```

### Desativar Paciente
```bash
curl -X DELETE http://localhost:8082/api/patients/$PATIENT_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

## Consultas

### Listar Consultas
```bash
# Listar todas
curl http://localhost:8082/api/appointments \
  -H "Authorization: Bearer $TOKEN"

# Filtrar por período
curl "http://localhost:8082/api/appointments?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59" \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "patientId": "patient-uuid",
      "userId": "user-uuid",
      "scheduledAt": "2024-01-15T10:00:00",
      "durationMin": 60,
      "status": "SCHEDULED",
      "notes": "Consulta de rotina",
      "tenantId": "00000000-0000-0000-0000-000000000001"
    }
  ],
  "timestamp": "2024-01-01T00:00:00"
}
```

### Criar Consulta
```bash
curl -X POST http://localhost:8082/api/appointments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "patient-uuid",
    "userId": "user-uuid",
    "scheduledAt": "2024-01-20T14:00:00",
    "durationMin": 60,
    "status": "SCHEDULED",
    "notes": "Consulta de retorno"
  }'
```

### Buscar Consulta por ID
```bash
APPOINTMENT_ID="uuid-da-consulta"

curl http://localhost:8082/api/appointments/$APPOINTMENT_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Atualizar Consulta
```bash
curl -X PUT http://localhost:8082/api/appointments/$APPOINTMENT_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "patient-uuid",
    "userId": "user-uuid",
    "scheduledAt": "2024-01-20T15:00:00",
    "durationMin": 90,
    "status": "CONFIRMED",
    "notes": "Consulta confirmada - horário alterado"
  }'
```

---

## Status da Consulta

Valores possíveis para `status`:
- `SCHEDULED` - Agendada
- `CONFIRMED` - Confirmada
- `COMPLETED` - Realizada
- `CANCELLED` - Cancelada
- `NO_SHOW` - Paciente não compareceu

---

## Tratamento de Erros

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized",
  "timestamp": "2024-01-01T00:00:00"
}
```

**Causa:** Token ausente, inválido ou expirado.

**Solução:** Fazer login novamente.

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid",
    "password": "Password is required"
  },
  "timestamp": "2024-01-01T00:00:00"
}
```

**Causa:** Dados inválidos na requisição.

**Solução:** Corrigir os dados conforme mensagens de erro.

### 404 Not Found
```json
{
  "success": false,
  "message": "Patient not found",
  "timestamp": "2024-01-01T00:00:00"
}
```

**Causa:** Recurso não existe ou não pertence ao tenant.

**Solução:** Verificar ID e tenant.

---

## Fluxo Completo

### 1. Login
```bash
# Login
RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@tbcare.com", "password": "admin123"}')

# Extrair token
TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Token: $TOKEN"
```

### 2. Criar Paciente
```bash
PATIENT_RESPONSE=$(curl -s -X POST http://localhost:8082/api/patients \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Carlos Oliveira",
    "dateOfBirth": "1992-08-10",
    "phone": "(11) 99999-8888",
    "email": "carlos@email.com"
  }')

PATIENT_ID=$(echo $PATIENT_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)

echo "Patient ID: $PATIENT_ID"
```

### 3. Criar Consulta
```bash
USER_ID="00000000-0000-0000-0000-000000000001"

curl -X POST http://localhost:8082/api/appointments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"patientId\": \"$PATIENT_ID\",
    \"userId\": \"$USER_ID\",
    \"scheduledAt\": \"2024-01-25T10:00:00\",
    \"durationMin\": 60,
    \"status\": \"SCHEDULED\",
    \"notes\": \"Primeira consulta\"
  }"
```

### 4. Listar Consultas do Dia
```bash
TODAY=$(date +%Y-%m-%d)
START="${TODAY}T00:00:00"
END="${TODAY}T23:59:59"

curl "http://localhost:8082/api/appointments?start=$START&end=$END" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Postman Collection

### Variáveis de Ambiente
```json
{
  "baseUrl": "http://localhost:8082/api",
  "token": "",
  "patientId": "",
  "appointmentId": ""
}
```

### Configuração de Token Automático

No script de teste do endpoint de login:
```javascript
var jsonData = pm.response.json();
pm.environment.set("token", jsonData.data.token);
```

Nos demais endpoints, adicionar no header:
```
Authorization: Bearer {{token}}
```

---

## Insomnia Collection

### Criar Workspace
1. Criar novo workspace "TB Care Platform"
2. Criar pasta "Auth"
3. Criar pasta "Patients"
4. Criar pasta "Appointments"

### Configurar Environment
```json
{
  "base_url": "http://localhost:8082/api",
  "token": ""
}
```

### Chain Requests
No endpoint de login, adicionar:
```javascript
const response = await insomnia.send();
const body = await response.json();
await insomnia.environment.set('token', body.data.token);
```

---

## Dicas

### Salvar Token em Variável
```bash
# Fazer login e salvar token
export TOKEN=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@tbcare.com", "password": "admin123"}' \
  | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# Usar em outras requisições
curl http://localhost:8082/api/patients -H "Authorization: Bearer $TOKEN"
```

### Pretty Print JSON
```bash
curl http://localhost:8082/api/patients \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.'
```

### Verificar Token JWT
Acesse [jwt.io](https://jwt.io) e cole o token para ver os claims.
