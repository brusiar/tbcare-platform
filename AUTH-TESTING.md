# Guia de Teste - Autenticação JWT

## Teste Rápido

### 1. Iniciar o Backend

```bash
cd backend
mvn spring-boot:run
```

Aguarde até ver: `Started TbCareApplication`

### 2. Testar Login

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@tbcare.com",
    "password": "admin123"
  }'
```

**Resposta esperada:**
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

### 3. Copiar o Token

Copie o valor do campo `token` da resposta.

### 4. Testar Endpoint Autenticado

```bash
# Substitua <TOKEN> pelo token copiado
curl http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```

**Resposta esperada:**
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

### 5. Testar Isolamento Multi-Tenant

```bash
# Listar pacientes (isolado por tenant do JWT)
curl http://localhost:8082/api/patients \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Usuários Disponíveis

### Admin
- **Email:** admin@tbcare.com
- **Senha:** admin123
- **Role:** ADMIN
- **Tenant:** tbcare-demo

### Profissional
- **Email:** joao@tbcare.com
- **Senha:** prof123
- **Role:** PROFESSIONAL
- **Tenant:** tbcare-demo

---

## Testes de Segurança

### 1. Endpoint Público (sem token)

```bash
curl http://localhost:8082/api/health
```

✅ Deve funcionar sem autenticação.

### 2. Endpoint Protegido (sem token)

```bash
curl http://localhost:8082/api/patients
```

❌ Deve retornar `401 Unauthorized`.

### 3. Token Inválido

```bash
curl http://localhost:8082/api/patients \
  -H "Authorization: Bearer token-invalido"
```

❌ Deve retornar `401 Unauthorized`.

### 4. Token Expirado

Aguarde 24h ou altere `JWT_EXPIRATION` para testar.

❌ Deve retornar `401 Unauthorized`.

---

## Teste com Postman/Insomnia

### 1. Criar Requisição de Login

- **Método:** POST
- **URL:** `http://localhost:8082/api/auth/login`
- **Headers:** `Content-Type: application/json`
- **Body (JSON):**
```json
{
  "email": "admin@tbcare.com",
  "password": "admin123"
}
```

### 2. Salvar Token

Copie o `token` da resposta.

### 3. Criar Requisição Autenticada

- **Método:** GET
- **URL:** `http://localhost:8082/api/auth/me`
- **Headers:** `Authorization: Bearer <TOKEN>`

---

## Verificar Banco de Dados

```bash
# Conectar ao PostgreSQL
psql -h localhost -p 5432 -U tbcare -d tbcare

# Listar tenants
SELECT * FROM tenants;

# Listar usuários
SELECT id, name, email, role, tenant_id FROM users;

# Verificar hash de senha
SELECT email, password_hash FROM users WHERE email = 'admin@tbcare.com';
```

---

## Decodificar JWT

Acesse [jwt.io](https://jwt.io) e cole o token para ver os claims:

```json
{
  "userId": "00000000-0000-0000-0000-000000000001",
  "tenantId": "00000000-0000-0000-0000-000000000001",
  "name": "Admin User",
  "role": "ADMIN",
  "sub": "admin@tbcare.com",
  "iat": 1234567890,
  "exp": 1234654290
}
```

---

## Troubleshooting

### Erro: "User not found"

**Causa:** Email ou senha incorretos.

**Solução:** Verificar credenciais ou executar migration V2.

### Erro: "401 Unauthorized"

**Causa:** Token inválido, expirado ou ausente.

**Solução:** Fazer login novamente para obter novo token.

### Erro: "Connection refused"

**Causa:** Backend não está rodando.

**Solução:** Iniciar o backend com `mvn spring-boot:run`.

### Erro: "Flyway migration failed"

**Causa:** Banco de dados não está limpo.

**Solução:**
```bash
# Dropar e recriar banco
dropdb tbcare
createdb tbcare

# Reiniciar backend
mvn spring-boot:run
```

---

## Próximos Testes

- [ ] Criar novo paciente com token
- [ ] Criar consulta com token
- [ ] Testar com usuário PROFESSIONAL
- [ ] Verificar isolamento entre tenants (criar segundo tenant)
- [ ] Testar refresh token (quando implementado)
