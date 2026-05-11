# 🚀 Deploy no Servidor - TB Care Platform

## Pré-requisitos no Servidor

- Docker 20+
- Docker Compose 2+
- Git

## Passo 1: Clonar o Repositório

```bash
cd ~/apps
git clone https://github.com/brusiar/tbcare-platform.git
cd tbcare-platform
```

## Passo 2: Verificar Portas Disponíveis

O projeto está configurado para usar:
- **Frontend:** 3003
- **Backend:** 8082
- **PostgreSQL:** 5434

Verificar se as portas estão livres:

```bash
# Verificar portas em uso
netstat -tuln | grep -E '3003|8082|5434'

# Ou com lsof
lsof -i :3003
lsof -i :8082
lsof -i :5434
```

Se alguma porta estiver ocupada, edite o `docker-compose.yml` antes de subir.

## Passo 3: Subir os Containers

```bash
# Subir em background
docker compose up -d --build

# Ver logs em tempo real
docker compose logs -f

# Ver logs de um serviço específico
docker compose logs -f backend
docker compose logs -f frontend
```

## Passo 4: Verificar Status

```bash
# Ver containers rodando
docker compose ps

# Verificar health dos serviços
docker compose ps --format "table {{.Name}}\t{{.Status}}"
```

Aguarde até ver:
```
tbcare-postgres   Up (healthy)
tbcare-backend    Up (healthy)
tbcare-frontend   Up
```

## Passo 5: Testar os Serviços

### Backend (Health Check)

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

### Frontend

```bash
curl -I http://localhost:3003
```

Deve retornar `HTTP/1.1 200 OK`

### PostgreSQL

```bash
docker compose exec postgres psql -U tbcare -d tbcare -c "SELECT version();"
```

## Passo 6: Verificar Migrations

```bash
# Ver logs do backend para confirmar que Flyway rodou
docker compose logs backend | grep -i flyway

# Conectar ao banco e verificar tabelas
docker compose exec postgres psql -U tbcare -d tbcare -c "\dt"
```

Deve mostrar as tabelas:
- tenants
- users
- patients
- appointments
- flyway_schema_history

## Passo 7: Criar Dados de Teste (Opcional)

```bash
# Criar um paciente
curl -X POST http://localhost:8082/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "dateOfBirth": "1990-05-15",
    "phone": "11999999999",
    "email": "joao@example.com"
  }'

# Listar pacientes
curl http://localhost:8082/api/patients
```

## Comandos Úteis

### Ver logs

```bash
# Todos os serviços
docker compose logs -f

# Apenas backend
docker compose logs -f backend

# Últimas 100 linhas
docker compose logs --tail=100
```

### Restart de serviços

```bash
# Restart de um serviço específico
docker compose restart backend
docker compose restart frontend

# Restart de tudo
docker compose restart
```

### Parar e remover

```bash
# Parar containers
docker compose stop

# Parar e remover containers
docker compose down

# Parar, remover containers e volumes (CUIDADO: apaga dados do banco)
docker compose down -v
```

### Rebuild

```bash
# Rebuild apenas backend
docker compose up -d --build backend

# Rebuild apenas frontend
docker compose up -d --build frontend

# Rebuild tudo
docker compose up -d --build
```

### Ver uso de recursos

```bash
docker stats
```

## Troubleshooting

### Backend não inicia

```bash
# Ver logs detalhados
docker compose logs backend

# Verificar se o banco está acessível
docker compose exec backend ping postgres

# Verificar variáveis de ambiente
docker compose exec backend env | grep DB_
```

### Frontend não inicia

```bash
# Ver logs
docker compose logs frontend

# Verificar se consegue acessar o backend
docker compose exec frontend curl http://backend:8080/api/health
```

### PostgreSQL não inicia

```bash
# Ver logs
docker compose logs postgres

# Verificar volume
docker volume ls | grep tbcare

# Remover volume e recriar (CUIDADO: apaga dados)
docker compose down -v
docker compose up -d
```

### Porta já em uso

Se alguma porta estiver ocupada, edite `docker-compose.yml`:

```yaml
services:
  frontend:
    ports:
      - "3004:3000"  # Mudar 3003 para 3004
  
  backend:
    ports:
      - "8083:8080"  # Mudar 8082 para 8083
  
  postgres:
    ports:
      - "5435:5432"  # Mudar 5434 para 5435
```

Depois:
```bash
docker compose down
docker compose up -d --build
```

## Atualizar o Projeto

```bash
# Parar containers
docker compose down

# Atualizar código
git pull origin main

# Rebuild e subir
docker compose up -d --build

# Ver logs
docker compose logs -f
```

## Backup do Banco

```bash
# Criar backup
docker compose exec postgres pg_dump -U tbcare tbcare > backup_$(date +%Y%m%d_%H%M%S).sql

# Restaurar backup
docker compose exec -T postgres psql -U tbcare tbcare < backup_20240101_120000.sql
```

## Monitoramento

### Ver uso de recursos

```bash
# CPU e memória em tempo real
docker stats

# Espaço em disco
docker system df
```

### Logs de produção

```bash
# Salvar logs em arquivo
docker compose logs > logs_$(date +%Y%m%d_%H%M%S).log

# Monitorar erros
docker compose logs -f | grep -i error
```

## Configuração de Firewall (se necessário)

```bash
# Permitir portas (exemplo com ufw)
sudo ufw allow 3003/tcp
sudo ufw allow 8082/tcp

# Verificar regras
sudo ufw status
```

## Acesso Externo

Se quiser acessar de fora do servidor, configure o nginx como proxy reverso:

```nginx
# /etc/nginx/sites-available/tbcare

server {
    listen 80;
    server_name tbcare.seudominio.com;

    location / {
        proxy_pass http://localhost:3003;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    location /api {
        proxy_pass http://localhost:8082/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

Ativar:
```bash
sudo ln -s /etc/nginx/sites-available/tbcare /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## Checklist de Deploy

- [ ] Clonar repositório
- [ ] Verificar portas disponíveis
- [ ] Subir containers (`docker compose up -d --build`)
- [ ] Verificar status (`docker compose ps`)
- [ ] Testar health check (`curl http://localhost:8082/api/health`)
- [ ] Testar frontend (`curl http://localhost:3003`)
- [ ] Verificar migrations (tabelas criadas)
- [ ] Criar dados de teste
- [ ] Configurar backup automático (opcional)
- [ ] Configurar nginx (se acesso externo)

## Contatos

- Repositório: https://github.com/brusiar/tbcare-platform
- Documentação: Ver README.md, ARCHITECTURE.md, QUICKSTART.md
