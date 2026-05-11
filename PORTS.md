# Configuração de Portas - TB Care Platform

## Portas Configuradas

Para evitar conflito com outros sistemas no servidor, as seguintes portas foram configuradas:

| Serviço    | Porta Interna | Porta Externa | URL de Acesso                    |
|------------|---------------|---------------|----------------------------------|
| Frontend   | 3000          | **3003**      | http://localhost:3003            |
| Backend    | 8080          | **8082**      | http://localhost:8082/api        |
| PostgreSQL | 5432          | **5434**      | localhost:5434                   |

## Portas Ocupadas no Servidor

Evitadas as seguintes portas já em uso:

- **3000** - financeiro-frontend
- **3001** - financeiro-frontend-mobile  
- **3002** - ntm-frontend
- **8080** - financeiro-backend
- **8081** - ntm-backend
- **5432** - financeiro-postgres
- **5433** - ntm-postgres

## Arquivos Atualizados

✅ `docker-compose.yml` - Mapeamento de portas dos containers
✅ `frontend/.env.local` - URL da API
✅ `frontend/next.config.js` - URL padrão da API
✅ `frontend/src/lib/api.ts` - Cliente HTTP
✅ `README.md` - Documentação
✅ `QUICKSTART.md` - Guia de início rápido
✅ `DELIVERY.md` - Documento de entrega
✅ `SUMMARY.txt` - Resumo executivo

## Como Conectar

### Frontend → Backend
```
NEXT_PUBLIC_API_URL=http://localhost:8082/api
```

### Backend → PostgreSQL (interno)
```
DB_HOST=postgres
DB_PORT=5432  # Porta interna do container
```

### Acesso Externo ao PostgreSQL
```
Host: localhost
Port: 5434
Database: tbcare
User: tbcare
Password: tbcare
```

## Comandos de Teste

```bash
# Health check do backend
curl http://localhost:8082/api/health

# Acessar frontend
open http://localhost:3003

# Conectar ao PostgreSQL
psql -h localhost -p 5434 -U tbcare -d tbcare
```
