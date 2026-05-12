# TB Care Platform

Plataforma de gestão de saúde — monolito modular com arquitetura limpa, preparada para multi-tenancy.

---

## Stack

| Camada     | Tecnologia                          |
|------------|-------------------------------------|
| Backend    | Java 21 · Spring Boot 3 · Maven     |
| Banco      | PostgreSQL 16 · Flyway              |
| Frontend   | Next.js 14 · TypeScript · Tailwind  |
| Infra      | Docker · Docker Compose             |

---

## Estrutura do Projeto

```
tbcare-platform/
├── backend/
│   └── src/main/java/com/tbcare/
│       ├── TbCareApplication.java
│       ├── auth/                    # Módulo de autenticação
│       ├── users/                   # Módulo de usuários
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── domain/
│       │   └── dto/
│       ├── patients/                # Módulo de pacientes
│       ├── appointments/            # Módulo de consultas
│       ├── professionals/           # Módulo de profissionais
│       ├── tenants/                 # Módulo de tenants
│       ├── common/                  # Utilitários compartilhados
│       │   ├── BaseEntity.java
│       │   ├── TenantAwareEntity.java
│       │   ├── exception/
│       │   └── response/
│       ├── security/                # Configurações de segurança
│       └── config/                  # Configurações Spring
├── frontend/
│   └── src/
│       ├── app/
│       │   ├── login/               # Página de login
│       │   └── (app)/               # Grupo de rotas autenticadas
│       │       ├── dashboard/
│       │       ├── agenda/
│       │       └── pacientes/
│       ├── components/
│       │   ├── layout/              # Sidebar, Header
│       │   └── ui/                  # Button, Card, Badge
│       ├── lib/                     # utils, api client
│       ├── styles/                  # globals.css
│       └── types/                   # Tipos TypeScript do domínio
├── docker-compose.yml
└── README.md
```

---

## Arquitetura

### Monolito Modular

Cada módulo (`patients`, `appointments`, `users`, `tenants`) é autossuficiente com suas próprias camadas:

```
controller → service → repository → domain
```

Módulos se comunicam via **service**, nunca diretamente entre repositories. Isso permite extrair módulos como microsserviços no futuro sem refatoração estrutural.

### Preparação Multi-Tenant

Toda entidade de negócio herda de `TenantAwareEntity`, que carrega `tenant_id`. O isolamento de dados por tenant é feito via filtro na query (Row-Level Isolation). O `tenant_id` é extraído automaticamente do JWT via `TenantContext`.

```
BaseEntity (id, createdAt, updatedAt)
    ├── Tenant
    └── TenantAwareEntity (+ tenantId)
            ├── User
            ├── Professional
            ├── Patient
            ├── Appointment
            └── AppointmentRecurrence
```

### Resposta Padrão da API

Todas as respostas seguem o envelope `ApiResponse<T>`:

```json
{
  "success": true,
  "data": { ... },
  "message": null,
  "timestamp": "2024-01-01T00:00:00"
}
```

---

## Setup Local (sem Docker)

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16+

### Backend

```bash
# Criar banco
createdb tbcare

# Rodar
cd backend
mvn spring-boot:run
```

API disponível em: `http://localhost:8082/api`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App disponível em: `http://localhost:3003`

---

## Setup com Docker

```bash
# Subir tudo
docker compose up --build

# Apenas o banco (para dev local)
docker compose up postgres
```

| Serviço   | URL                          |
|-----------|------------------------------|
| Frontend  | http://localhost:3003        |
| Backend   | http://localhost:8082/api    |
| Postgres  | localhost:5434               |

---

## Endpoints da API

### Autenticação

| Método | Rota           | Descrição                  | Auth |
|--------|----------------|----------------------------|------|
| POST   | /api/auth/login| Login (retorna JWT)        | ❌   |
| GET    | /api/auth/me   | Dados do usuário autenticado| ✅   |

### Recursos

| Método | Rota                    | Descrição                  | Auth |
|--------|-------------------------|----------------------------|------|
| GET    | /api/health             | Status da API              | ❌   |
| GET    | /api/patients           | Listar pacientes           | ✅   |
| POST   | /api/patients           | Criar paciente             | ✅   |
| GET    | /api/patients/{id}      | Buscar paciente            | ✅   |
| PUT    | /api/patients/{id}      | Atualizar paciente         | ✅   |
| DELETE | /api/patients/{id}      | Desativar paciente         | ✅   |
| GET    | /api/appointments       | Listar consultas (período) | ✅   |
| POST   | /api/appointments       | Criar consulta             | ✅   |
| GET    | /api/appointments/{id}  | Buscar consulta            | ✅   |
| PUT    | /api/appointments/{id}  | Atualizar consulta         | ✅   |
| PUT    | /api/appointments/{id}/cancel | Cancelar consulta    | ✅   |

---

## Banco de Dados

Migrations gerenciadas pelo **Flyway** em `src/main/resources/db/migration/`.

| Migration                  | Descrição                        |
|----------------------------|----------------------------------|
| V1__initial_schema.sql     | Schema base: tenants, users, patients, appointments |
| V2__add_authentication.sql | Adiciona password_hash e usuários seed |
| V3__domain_initial.sql     | Professionals, recorrências e expansão de appointments |

---

## Variáveis de Ambiente

### Backend

| Variável       | Padrão      | Descrição          |
|----------------|-------------|--------------------|
| DB_HOST        | localhost   | Host do PostgreSQL |
| DB_PORT        | 5432        | Porta do PostgreSQL|
| DB_NAME        | tbcare      | Nome do banco      |
| DB_USER        | tbcare      | Usuário do banco   |
| DB_PASSWORD    | tbcare      | Senha do banco     |
| SERVER_PORT    | 8080        | Porta da API       |
| JWT_SECRET     | (ver abaixo)| Secret para JWT (min 256 bits) |
| JWT_EXPIRATION | 86400000    | Expiração JWT (24h em ms) |

### Frontend

| Variável              | Padrão                      | Descrição       |
|-----------------------|-----------------------------|-----------------|
| NEXT_PUBLIC_API_URL   | http://localhost:8082/api   | URL da API      |

---

## Autenticação

### Usuários de Desenvolvimento

**Admin:**
- Email: `admin@tbcare.com`
- Senha: `admin123`
- Role: `ADMIN`

**Profissional:**
- Email: `joao@tbcare.com`
- Senha: `prof123`
- Role: `PROFESSIONAL`

### Exemplo de Login

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

# Listar pacientes (isolado por tenant)
curl http://localhost:8082/api/patients \
  -H "Authorization: Bearer $TOKEN"
```

### Documentação Completa

Veja [SECURITY-ARCHITECTURE.md](./SECURITY-ARCHITECTURE.md) para detalhes completos sobre:
- Fluxo de autenticação JWT
- Isolamento multi-tenant
- Roles e permissões
- Segurança de senhas
- Boas práticas

Veja [DOMAIN-MODELING.md](./DOMAIN-MODELING.md) para detalhes sobre:
- Modelagem do domínio
- Decisões de design
- Estrutura de recorrência
- Fluxos principais
- Evolução futura

---

## Funcionalidades

### Gestão de Pacientes
- ✅ Cadastro completo (nome, email, telefone, data nascimento, observações)
- ✅ Listagem de pacientes ativos
- ✅ Edição de dados
- ✅ Desativação (soft delete)
- ✅ Interface web responsiva

### Agenda e Sessões
- ✅ Visualização de agenda por dia
- ✅ Navegação entre dias (anterior/hoje/próximo)
- ✅ Criação de sessões
- ✅ Status de sessões (Agendada, Confirmada, Realizada, Cancelada, Faltou)
- ✅ Link da sala Google Meet por profissional
- ✅ Cancelamento de sessões
- ✅ Duração configurável
- 🔄 Recorrência (estrutura pronta, geração manual)

### Profissionais
- ✅ Cadastro de profissionais
- ✅ Link fixo da sala Google Meet
- ✅ Especialidade
- ✅ Vinculação com usuário do sistema

---

## Próximas Etapas

- [x] Autenticação JWT (módulo `auth` + `security`)
- [x] Multi-tenant completo (filtro automático por tenant)
- [x] DTOs com validação (Bean Validation)
- [x] CRUD de pacientes (backend + frontend)
- [x] Agenda funcional (backend + frontend)
- [x] Sessões funcionais (backend + frontend)
- [x] Estrutura de recorrência preparada
- [ ] Geração automática de sessões recorrentes
- [ ] Validação de conflitos de horário
- [ ] Testes unitários e de integração
- [ ] Paginação nas listagens
- [ ] Refresh tokens
- [ ] Rate limiting por tenant
- [ ] Notificações de lembrete
- [ ] Portal do paciente
