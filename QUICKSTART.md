# Guia de Início Rápido

## Opção 1: Docker (Recomendado)

### Pré-requisitos
- Docker 20+
- Docker Compose 2+

### Subir tudo

```bash
docker compose up --build
```

Aguarde alguns minutos. Quando ver:
```
tbcare-backend    | Started TbCareApplication in X seconds
tbcare-frontend   | ▲ Next.js 14.2.3
```

Acesse:
- **Frontend:** http://localhost:3003
- **Backend API:** http://localhost:8082/api/health
- **PostgreSQL:** localhost:5434

---

## Opção 2: Desenvolvimento Local

### Pré-requisitos
- Java 21
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16+

### 1. Banco de Dados

```bash
# Criar banco
createdb tbcare

# Ou via Docker
docker compose up postgres
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

API disponível em: http://localhost:8082/api

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

App disponível em: http://localhost:3003

---

## Testando a API

### Health Check

```bash
curl http://localhost:8082/api/health
```

Resposta esperada:
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

### Criar Paciente

```bash
curl -X POST http://localhost:8082/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "dateOfBirth": "1990-05-15",
    "phone": "11999999999",
    "email": "joao@example.com"
  }'
```

### Listar Pacientes

```bash
curl http://localhost:8082/api/patients
```

### Criar Consulta

```bash
curl -X POST http://localhost:8082/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "UUID_DO_PACIENTE",
    "userId": "00000000-0000-0000-0000-000000000001",
    "scheduledAt": "2024-12-25T10:00:00",
    "durationMin": 60,
    "status": "SCHEDULED"
  }'
```

### Listar Consultas (período)

```bash
curl "http://localhost:8082/api/appointments?start=2024-12-01T00:00:00&end=2024-12-31T23:59:59"
```

---

## Estrutura de Pastas

```
tbcare-platform/
├── backend/                 # Spring Boot API
│   ├── src/main/java/
│   │   └── com/tbcare/
│   │       ├── appointments/
│   │       ├── patients/
│   │       ├── users/
│   │       ├── tenants/
│   │       ├── common/
│   │       └── config/
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/
│
├── frontend/                # Next.js App
│   └── src/
│       ├── app/
│       │   ├── login/
│       │   └── (app)/
│       ├── components/
│       ├── lib/
│       └── types/
│
└── docker-compose.yml
```

---

## Comandos Úteis

### Backend

```bash
# Compilar
mvn clean compile

# Rodar testes
mvn test

# Gerar JAR
mvn clean package

# Limpar target
mvn clean
```

### Frontend

```bash
# Instalar dependências
npm install

# Dev server
npm run dev

# Build produção
npm run build

# Rodar build
npm start

# Lint
npm run lint
```

### Docker

```bash
# Subir tudo
docker compose up

# Subir em background
docker compose up -d

# Ver logs
docker compose logs -f

# Parar tudo
docker compose down

# Rebuild
docker compose up --build

# Limpar volumes
docker compose down -v
```

---

## Variáveis de Ambiente

### Backend (.env ou docker-compose.yml)

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tbcare
DB_USER=tbcare
DB_PASSWORD=tbcare
SERVER_PORT=8080
```

### Frontend (.env.local)

```env
NEXT_PUBLIC_API_URL=http://localhost:8082/api
```

---

## Troubleshooting

### Backend não inicia

**Erro:** `Connection refused`
- Verifique se o PostgreSQL está rodando
- Confirme as credenciais em `application.yml`

**Erro:** `Port 8082 already in use`
```bash
# Matar processo na porta 8082
lsof -ti:8082 | xargs kill -9
```

### Frontend não inicia

**Erro:** `EADDRINUSE: address already in use :::3003`
```bash
# Matar processo na porta 3003
lsof -ti:3003 | xargs kill -9
```

**Erro:** `Module not found`
```bash
# Reinstalar dependências
rm -rf node_modules package-lock.json
npm install
```

### Migrations não rodam

```bash
# Limpar banco e recriar
dropdb tbcare
createdb tbcare

# Restart backend (Flyway roda automaticamente)
```

---

## Próximos Passos

1. Explorar as páginas do frontend
2. Testar os endpoints da API
3. Criar alguns pacientes e consultas
4. Revisar a arquitetura em `ARCHITECTURE.md`
5. Começar a implementar autenticação (próxima etapa)

---

## Suporte

- Documentação completa: `README.md`
- Arquitetura: `ARCHITECTURE.md`
- Issues: [GitHub Issues]
