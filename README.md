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
│       ├── auth/                    # Módulo de autenticação (futuro)
│       ├── users/                   # Módulo de usuários
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── domain/
│       │   └── dto/
│       ├── patients/                # Módulo de pacientes
│       ├── appointments/            # Módulo de consultas
│       ├── tenants/                 # Módulo de tenants
│       ├── common/                  # Utilitários compartilhados
│       │   ├── BaseEntity.java
│       │   ├── TenantAwareEntity.java
│       │   ├── exception/
│       │   └── response/
│       ├── security/                # Configurações de segurança (futuro)
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

Toda entidade de negócio herda de `TenantAwareEntity`, que carrega `tenant_id`. O isolamento de dados por tenant é feito via filtro na query (Row-Level Isolation). Quando autenticação for implementada, o `tenant_id` virá do contexto de segurança automaticamente.

```
BaseEntity (id, createdAt, updatedAt)
    └── TenantAwareEntity (+ tenantId)
            ├── User
            ├── Patient
            └── Appointment
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

| Método | Rota                    | Descrição                  |
|--------|-------------------------|----------------------------|
| GET    | /api/health             | Status da API              |
| GET    | /api/patients           | Listar pacientes           |
| POST   | /api/patients           | Criar paciente             |
| GET    | /api/patients/{id}      | Buscar paciente            |
| PUT    | /api/patients/{id}      | Atualizar paciente         |
| DELETE | /api/patients/{id}      | Desativar paciente         |
| GET    | /api/appointments       | Listar consultas (período) |
| POST   | /api/appointments       | Criar consulta             |
| GET    | /api/appointments/{id}  | Buscar consulta            |
| PUT    | /api/appointments/{id}  | Atualizar consulta         |

---

## Banco de Dados

Migrations gerenciadas pelo **Flyway** em `src/main/resources/db/migration/`.

| Migration              | Descrição                        |
|------------------------|----------------------------------|
| V1__initial_schema.sql | Schema base: tenants, users, patients, appointments |

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

### Frontend

| Variável              | Padrão                      | Descrição       |
|-----------------------|-----------------------------|-----------------|
| NEXT_PUBLIC_API_URL   | http://localhost:8082/api   | URL da API      |

---

## Próximas Etapas

- [ ] Autenticação JWT (módulo `auth` + `security`)
- [ ] Multi-tenant completo (filtro automático por tenant)
- [ ] DTOs com validação (Bean Validation)
- [ ] Testes unitários e de integração
- [ ] Paginação nas listagens
- [ ] Implementação das telas com dados reais
