# Arquitetura TB Care Platform

## Visão Geral

O TB Care Platform é um **monolito modular** construído com arquitetura limpa, preparado para evolução futura para multi-tenancy completo e eventual extração de microsserviços.

---

## Decisões Arquiteturais

### 1. Monolito Modular

**Por quê?**
- Simplicidade inicial de deploy e operação
- Menor overhead de infraestrutura
- Transações ACID nativas
- Facilita desenvolvimento inicial

**Como está organizado?**
Cada módulo de negócio (`patients`, `appointments`, `users`, `tenants`) é completamente isolado:

```
módulo/
├── controller/     # Camada de apresentação (REST)
├── service/        # Lógica de negócio
├── repository/     # Acesso a dados
├── domain/         # Entidades JPA
└── dto/            # Objetos de transferência (futuro)
```

**Regras de comunicação:**
- Módulos se comunicam APENAS via camada `service`
- Nunca acessar `repository` de outro módulo diretamente
- Isso permite extrair módulos como microsserviços sem refatoração

---

### 2. Preparação Multi-Tenant

**Estratégia: Row-Level Isolation**

Todas as entidades de negócio herdam de `TenantAwareEntity`:

```java
BaseEntity (id, createdAt, updatedAt)
    └── TenantAwareEntity (+ tenantId)
            ├── User
            ├── Patient
            └── Appointment
```

**Estado atual:**
- `tenant_id` presente em todas as tabelas
- Filtro manual por tenant nos repositories
- Tenant padrão hardcoded nos controllers (UUID fixo)

**Próximos passos:**
- Implementar filtro automático via Hibernate Filter
- Extrair `tenant_id` do contexto de segurança (JWT)
- Adicionar validação de tenant em todas as operações

---

### 3. Camada de Persistência

**JPA + Hibernate + PostgreSQL**

- Entidades com auditoria automática (`@CreatedDate`, `@LastModifiedDate`)
- Migrations gerenciadas pelo Flyway (versionamento de schema)
- UUIDs como chave primária (distribuição futura)

**Por que não Lombok?**
- Incompatibilidade com Java 21 e Maven Compiler Plugin 3.13
- Optamos por getters/setters manuais para máxima compatibilidade
- Código mais explícito e debugável

---

### 4. API REST

**Padrão de resposta unificado:**

```json
{
  "success": true,
  "data": { ... },
  "message": "Optional message",
  "timestamp": "2024-01-01T00:00:00"
}
```

**Tratamento de erros:**
- `GlobalExceptionHandler` centralizado
- Exceções mapeadas para HTTP status apropriados
- Mensagens de erro consistentes

---

### 5. Frontend - Next.js 14

**App Router + Server Components**

Estrutura:
```
app/
├── login/              # Página pública
└── (app)/              # Grupo de rotas autenticadas
    ├── layout.tsx      # Layout com Sidebar
    ├── dashboard/
    ├── agenda/
    └── pacientes/
```

**Design System:**
- Tailwind CSS com paleta customizada extraída da referência visual
- Componentes reutilizáveis em `components/ui/`
- Identidade visual: roxo (`#5a3c78`) + tons quentes (`#dca08c`)

---

## Stack Técnica

### Backend
- **Java 21** - LTS, performance, records, pattern matching
- **Spring Boot 3.3** - Framework maduro, ecossistema rico
- **PostgreSQL 16** - ACID, JSON support, extensibilidade
- **Flyway** - Migrations versionadas
- **Maven** - Build e gerenciamento de dependências

### Frontend
- **Next.js 14** - SSR, App Router, otimizações automáticas
- **TypeScript** - Type safety, melhor DX
- **Tailwind CSS** - Utility-first, customização fácil
- **React 18** - Concurrent features, Server Components

### Infra
- **Docker** - Containerização
- **Docker Compose** - Orquestração local

---

## Padrões de Código

### Backend

**Controllers:**
- Responsáveis apenas por HTTP (request/response)
- Delegam lógica para services
- Validação básica de entrada

**Services:**
- Contêm lógica de negócio
- Gerenciam transações (`@Transactional`)
- Orquestram múltiplos repositories se necessário

**Repositories:**
- Apenas queries de dados
- Sem lógica de negócio
- Métodos descritivos (ex: `findByTenantIdAndActiveTrue`)

### Frontend

**Componentes:**
- Server Components por padrão
- Client Components (`'use client'`) apenas quando necessário
- Props tipadas com TypeScript

**Estilização:**
- Classes Tailwind inline
- Função `cn()` para merge condicional de classes
- Variantes via objetos de configuração

---

## Segurança (Futuro)

**Planejado:**
- Autenticação JWT
- Refresh tokens
- RBAC (Role-Based Access Control)
- Rate limiting
- CORS configurado por ambiente

---

## Testes (Futuro)

**Backend:**
- Testes unitários (JUnit 5 + Mockito)
- Testes de integração (TestContainers + PostgreSQL)
- Testes de API (RestAssured)

**Frontend:**
- Testes unitários (Jest + React Testing Library)
- Testes E2E (Playwright)

---

## Deploy (Futuro)

**Opções:**
- Docker Compose (desenvolvimento/staging)
- Kubernetes (produção)
- AWS ECS/Fargate
- Render/Railway (MVP rápido)

---

## Escalabilidade

**Vertical (curto prazo):**
- Aumentar recursos do container
- Connection pooling otimizado
- Cache (Redis) para queries frequentes

**Horizontal (médio prazo):**
- Load balancer + múltiplas instâncias
- Read replicas do PostgreSQL
- CDN para assets estáticos

**Microsserviços (longo prazo):**
- Extrair módulos como serviços independentes
- Event-driven architecture (Kafka/RabbitMQ)
- API Gateway

---

## Monitoramento (Futuro)

- **Logs:** Structured logging (JSON)
- **Métricas:** Prometheus + Grafana
- **Tracing:** OpenTelemetry
- **Alertas:** PagerDuty/Opsgenie
- **APM:** New Relic/Datadog

---

## Conclusão

A arquitetura foi desenhada para:
1. **Simplicidade inicial** - Monolito fácil de desenvolver e deployar
2. **Preparação futura** - Multi-tenant e microsserviços sem refatoração massiva
3. **Clean code** - Separação clara de responsabilidades
4. **Escalabilidade** - Vertical primeiro, horizontal depois
5. **Manutenibilidade** - Código explícito, bem organizado, testável
