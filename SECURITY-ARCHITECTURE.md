# Arquitetura de Segurança e Multi-Tenant

## Visão Geral

O TB Care Platform implementa autenticação JWT com isolamento multi-tenant completo. O `tenant_id` é extraído do token JWT e nunca vem do frontend, garantindo segurança e isolamento de dados.

---

## Fluxo de Autenticação

```
┌─────────┐         ┌─────────┐         ┌──────────┐         ┌──────────┐
│ Cliente │         │  Auth   │         │   JWT    │         │  Tenant  │
│         │         │ Service │         │   Util   │         │ Context  │
└────┬────┘         └────┬────┘         └────┬─────┘         └────┬─────┘
     │                   │                   │                    │
     │ POST /auth/login  │                   │                    │
     ├──────────────────>│                   │                    │
     │                   │                   │                    │
     │                   │ authenticate()    │                    │
     │                   │ (email/password)  │                    │
     │                   │                   │                    │
     │                   │ generateToken()   │                    │
     │                   ├──────────────────>│                    │
     │                   │                   │                    │
     │                   │ JWT (com tenant)  │                    │
     │                   │<──────────────────┤                    │
     │                   │                   │                    │
     │ JWT + user data   │                   │                    │
     │<──────────────────┤                   │                    │
     │                   │                   │                    │
     │ GET /api/patients │                   │                    │
     │ Authorization:    │                   │                    │
     │ Bearer <token>    │                   │                    │
     ├──────────────────────────────────────>│                    │
     │                   │                   │                    │
     │                   │                   │ extractTenantId()  │
     │                   │                   ├───────────────────>│
     │                   │                   │                    │
     │                   │                   │ setTenantId()      │
     │                   │                   │ (ThreadLocal)      │
     │                   │                   │                    │
     │                   │                   │ Tenant isolado     │
     │<──────────────────────────────────────┴────────────────────┤
```

---

## Componentes de Segurança

### 1. SecurityConfig

Configuração central do Spring Security:

- **Endpoints públicos**: `/auth/**`, `/health`
- **Endpoints protegidos**: Todos os demais requerem autenticação
- **Sessão**: Stateless (JWT)
- **Password Encoder**: BCrypt
- **Filtro JWT**: Executado antes de `UsernamePasswordAuthenticationFilter`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // Configuração de segurança stateless com JWT
}
```

### 2. JwtUtil

Responsável por gerar e validar tokens JWT:

**Claims incluídos no token:**
- `userId`: UUID do usuário
- `tenantId`: UUID do tenant (CRÍTICO para isolamento)
- `name`: Nome do usuário
- `role`: Role do usuário (ADMIN, PROFESSIONAL, PATIENT)
- `subject`: Email do usuário

**Configuração:**
- Algoritmo: HS256
- Secret: Mínimo 256 bits (configurável via `app.jwt.secret`)
- Expiração: 24h (configurável via `app.jwt.expiration`)

### 3. JwtAuthenticationFilter

Filtro que intercepta todas as requisições:

1. Extrai o token do header `Authorization: Bearer <token>`
2. Valida o token
3. Extrai o `tenantId` do token
4. Define o `tenantId` no `TenantContext` (ThreadLocal)
5. Autentica o usuário no `SecurityContext`
6. **Limpa o `TenantContext` após a requisição** (finally block)

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        try {
            // Valida JWT e define tenant
            UUID tenantId = jwtUtil.extractTenantId(token);
            TenantContext.setTenantId(tenantId);
            // ...
        } finally {
            TenantContext.clear(); // CRÍTICO: limpa após request
        }
    }
}
```

### 4. TenantContext

ThreadLocal que armazena o `tenantId` da requisição atual:

```java
public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    
    public static void setTenantId(UUID tenantId) { ... }
    public static UUID getTenantId() { ... }
    public static void clear() { ... }
}
```

**Uso nos Services:**
```java
UUID tenantId = TenantContext.getTenantId();
List<Patient> patients = patientRepository.findByTenantId(tenantId);
```

### 5. UserPrincipal

Implementação de `UserDetails` do Spring Security:

- Encapsula dados do usuário autenticado
- Contém `id`, `tenantId`, `email`, `name`, `role`
- Usado em `@AuthenticationPrincipal` nos controllers

```java
@GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
    @AuthenticationPrincipal UserPrincipal userPrincipal
) {
    // Acesso direto aos dados do usuário autenticado
}
```

### 6. CustomUserDetailsService

Carrega usuários do banco para autenticação:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmailAndActiveTrue(email)
            .orElseThrow(() -> new UsernameNotFoundException(...));
        return new UserPrincipal(user);
    }
}
```

---

## Modelo de Dados

### Hierarquia de Entidades

```
BaseEntity (id, createdAt, updatedAt)
    │
    ├── Tenant (name, slug, active)
    │
    └── TenantAwareEntity (+ tenantId)
            │
            ├── User (name, email, passwordHash, role, active)
            ├── Patient (name, dateOfBirth, phone, email, notes, active)
            └── Appointment (patientId, userId, scheduledAt, status, notes)
```

### Tenant

```sql
CREATE TABLE tenants (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);
```

### User

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255),
    role            VARCHAR(50) NOT NULL DEFAULT 'PROFESSIONAL',
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, email)
);
```

**Índices:**
- `idx_users_tenant` em `tenant_id`
- `idx_users_email` em `email`

---

## Roles e Permissões

### Roles Disponíveis

```java
public enum UserRole {
    ADMIN,          // Administrador do tenant
    PROFESSIONAL,   // Profissional de saúde
    PATIENT         // Paciente
}
```

### Mapeamento Spring Security

As roles são automaticamente prefixadas com `ROLE_`:
- `ADMIN` → `ROLE_ADMIN`
- `PROFESSIONAL` → `ROLE_PROFESSIONAL`
- `PATIENT` → `ROLE_PATIENT`

### Uso de Roles (Futuro)

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(UUID userId) { ... }

@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
public void createAppointment(...) { ... }
```

---

## Endpoints de Autenticação

### POST /auth/login

Autentica usuário e retorna JWT.

**Request:**
```json
{
  "email": "admin@tbcare.com",
  "password": "admin123"
}
```

**Response:**
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

### GET /auth/me

Retorna dados do usuário autenticado.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
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

## Isolamento Multi-Tenant

### Princípios

1. **Tenant nunca vem do frontend**: Sempre extraído do JWT
2. **Row-Level Isolation**: Cada query filtra por `tenant_id`
3. **ThreadLocal**: `TenantContext` isolado por thread/requisição
4. **Limpeza automática**: `finally` block no filtro JWT

### Implementação nos Services

```java
@Service
public class PatientService {
    
    public List<Patient> findAll() {
        UUID tenantId = TenantContext.getTenantId();
        return patientRepository.findByTenantIdAndActiveTrue(tenantId);
    }
    
    public Patient create(Patient patient) {
        UUID tenantId = TenantContext.getTenantId();
        patient.setTenantId(tenantId);
        return patientRepository.save(patient);
    }
}
```

### Queries Automáticas

Todos os repositories de entidades `TenantAwareEntity` devem filtrar por `tenant_id`:

```java
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findByTenantIdAndActiveTrue(UUID tenantId);
    Optional<Patient> findByIdAndTenantId(UUID id, UUID tenantId);
}
```

---

## Segurança de Senhas

### BCrypt

- Algoritmo: BCrypt
- Rounds: 10 (padrão)
- Salt: Gerado automaticamente

### Geração de Hash

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Senhas de Desenvolvimento

**Admin:**
- Email: `admin@tbcare.com`
- Senha: `admin123`

**Professional:**
- Email: `joao@tbcare.com`
- Senha: `prof123`

**⚠️ IMPORTANTE**: Alterar senhas em produção!

---

## Variáveis de Ambiente

### JWT

```bash
JWT_SECRET=tbcare-secret-key-change-in-production-min-256-bits-required-for-hs256-algorithm
JWT_EXPIRATION=86400000  # 24h em milissegundos
```

### Banco de Dados

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tbcare
DB_USER=tbcare
DB_PASSWORD=tbcare
```

---

## Fluxo de Requisição Completo

```
1. Cliente envia requisição com JWT
   ↓
2. JwtAuthenticationFilter intercepta
   ↓
3. Valida token JWT
   ↓
4. Extrai tenantId do token
   ↓
5. Define tenantId no TenantContext (ThreadLocal)
   ↓
6. Autentica usuário no SecurityContext
   ↓
7. Controller recebe requisição
   ↓
8. Service usa TenantContext.getTenantId()
   ↓
9. Repository filtra por tenant_id
   ↓
10. Response retornado ao cliente
   ↓
11. Finally: TenantContext.clear()
```

---

## Boas Práticas

### ✅ Fazer

- Sempre usar `TenantContext.getTenantId()` nos services
- Validar que o recurso pertence ao tenant antes de operações
- Limpar `TenantContext` após cada requisição
- Usar `@AuthenticationPrincipal` para acessar usuário autenticado
- Filtrar todas as queries por `tenant_id`

### ❌ Não Fazer

- Nunca aceitar `tenant_id` do frontend
- Nunca confiar em dados do cliente para isolamento
- Nunca expor `tenant_id` em URLs públicas
- Nunca reutilizar `TenantContext` entre requisições
- Nunca fazer queries sem filtro de tenant

---

## Testes de Autenticação

### Teste Manual com cURL

```bash
# Login
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@tbcare.com",
    "password": "admin123"
  }'

# Usar token retornado
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Buscar usuário autenticado
curl http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer $TOKEN"

# Listar pacientes (com tenant isolado)
curl http://localhost:8082/api/patients \
  -H "Authorization: Bearer $TOKEN"
```

---

## Próximas Melhorias

### Curto Prazo
- [ ] Refresh tokens
- [ ] Logout (blacklist de tokens)
- [ ] Rate limiting por tenant
- [ ] Auditoria de acessos

### Médio Prazo
- [ ] RBAC granular (permissões por recurso)
- [ ] MFA (Multi-Factor Authentication)
- [ ] OAuth2 / OIDC
- [ ] Filtro automático de tenant via Hibernate Filter

### Longo Prazo
- [ ] Tenant por schema (isolamento completo)
- [ ] Tenant por database
- [ ] SSO (Single Sign-On)
- [ ] SAML integration

---

## Troubleshooting

### Token inválido ou expirado

**Erro:** `401 Unauthorized`

**Solução:** Fazer login novamente para obter novo token.

### Tenant não encontrado

**Erro:** `TenantContext.getTenantId()` retorna `null`

**Causa:** Token JWT não contém `tenantId` ou filtro não executou.

**Solução:** Verificar que o token foi gerado corretamente e que o filtro está ativo.

### Usuário vê dados de outro tenant

**Causa CRÍTICA:** Query sem filtro de `tenant_id`.

**Solução:** Sempre usar `TenantContext.getTenantId()` e filtrar queries.

### Senha não funciona

**Causa:** Hash BCrypt incorreto ou senha não foi salva.

**Solução:** Verificar que `passwordEncoder.encode()` foi usado ao criar usuário.

---

## Referências

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/)
- [JJWT Library](https://github.com/jwtk/jjwt)
- [Multi-Tenancy Patterns](https://docs.microsoft.com/en-us/azure/architecture/patterns/multi-tenancy)
