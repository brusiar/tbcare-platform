# 🐌 Deploy em Ambiente com Rede Lenta

Se você está enfrentando timeouts no build do Docker, use esta abordagem alternativa.

## Problema

O `npm ci` está demorando muito ou dando timeout devido à rede lenta do servidor.

## Solução 1: Usar Dockerfile Simplificado (Recomendado)

### Passo 1: Atualizar código no servidor

```bash
cd ~/apps/tbcare-platform
git pull origin main
```

### Passo 2: Usar docker-compose simplificado

```bash
# Parar containers antigos (se houver)
docker compose down

# Usar versão simplificada
docker compose -f docker-compose.simple.yml up -d --build
```

Este Dockerfile tem:
- ✅ Timeout maior (10 minutos)
- ✅ Retry automático
- ✅ Flags otimizadas para rede lenta
- ✅ Menos camadas (mais rápido)

### Passo 3: Monitorar o build

```bash
# Ver logs em tempo real
docker compose -f docker-compose.simple.yml logs -f
```

---

## Solução 2: Build Local do Frontend

Se ainda assim der timeout, faça o build localmente no servidor:

### Passo 1: Instalar Node.js (se não tiver)

```bash
# Verificar se já tem
node -v

# Se não tiver, instalar
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs
```

### Passo 2: Build manual do frontend

```bash
cd ~/apps/tbcare-platform/frontend

# Instalar dependências (pode demorar)
npm install --prefer-offline --no-audit --legacy-peer-deps

# Build
npm run build
```

### Passo 3: Criar Dockerfile otimizado

Crie `frontend/Dockerfile.local`:

```dockerfile
FROM node:20-alpine

WORKDIR /app

# Copiar node_modules já instalado
COPY node_modules ./node_modules
COPY package.json ./
COPY .next ./.next
COPY public ./public

EXPOSE 3000
ENV PORT=3000
ENV NODE_ENV=production

CMD ["npm", "start"]
```

### Passo 4: Ajustar docker-compose

Crie `docker-compose.local.yml`:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: tbcare-postgres
    environment:
      POSTGRES_DB: tbcare
      POSTGRES_USER: tbcare
      POSTGRES_PASSWORD: tbcare
    ports:
      - "5434:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tbcare"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: tbcare-backend
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: tbcare
      DB_USER: tbcare
      DB_PASSWORD: tbcare
      SERVER_PORT: 8080
      CORS_ALLOWED_ORIGINS: http://localhost:3003
    ports:
      - "8082:8080"
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.local
    container_name: tbcare-frontend
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8082/api
    ports:
      - "3003:3000"
    depends_on:
      - backend

volumes:
  postgres_data:
```

### Passo 5: Subir containers

```bash
docker compose -f docker-compose.local.yml up -d --build
```

---

## Solução 3: Rodar Frontend Sem Docker

Se nada funcionar, rode o frontend diretamente no servidor:

```bash
cd ~/apps/tbcare-platform/frontend

# Instalar dependências (se ainda não fez)
npm install --prefer-offline --no-audit --legacy-peer-deps

# Build
npm run build

# Rodar em background com PM2
npm install -g pm2
pm2 start npm --name "tbcare-frontend" -- start
pm2 save
pm2 startup
```

Neste caso, suba apenas backend e postgres com Docker:

```bash
# Criar docker-compose.backend-only.yml
cat > docker-compose.backend-only.yml << 'EOF'
services:
  postgres:
    image: postgres:16-alpine
    container_name: tbcare-postgres
    environment:
      POSTGRES_DB: tbcare
      POSTGRES_USER: tbcare
      POSTGRES_PASSWORD: tbcare
    ports:
      - "5434:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tbcare"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: tbcare-backend
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: tbcare
      DB_USER: tbcare
      DB_PASSWORD: tbcare
      SERVER_PORT: 8080
      CORS_ALLOWED_ORIGINS: http://localhost:3003
    ports:
      - "8082:8080"
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  postgres_data:
EOF

# Subir apenas backend e postgres
docker compose -f docker-compose.backend-only.yml up -d --build
```

---

## Verificar Status

Após qualquer solução:

```bash
# Ver containers rodando
docker ps

# Testar backend
curl http://localhost:8082/api/health

# Testar frontend
curl -I http://localhost:3003

# Ver logs
docker compose logs -f
# ou
pm2 logs tbcare-frontend
```

---

## Dicas para Rede Lenta

1. **Aumentar timeout do Docker:**
   ```bash
   # Editar daemon.json
   sudo nano /etc/docker/daemon.json
   ```
   
   Adicionar:
   ```json
   {
     "dns": ["8.8.8.8", "8.8.4.4"],
     "max-concurrent-downloads": 1,
     "max-concurrent-uploads": 1
   }
   ```
   
   ```bash
   sudo systemctl restart docker
   ```

2. **Usar cache do npm:**
   ```bash
   # No servidor, antes do build
   npm config set cache /tmp/npm-cache --global
   ```

3. **Build em horário de menor tráfego:**
   - Madrugada geralmente tem rede mais rápida

4. **Considerar mirror local:**
   - Se tiver muitos deploys, configure um registry mirror local

---

## Resumo das Opções

| Solução | Complexidade | Velocidade | Recomendado |
|---------|--------------|------------|-------------|
| docker-compose.simple.yml | Baixa | Média | ✅ Sim |
| Build local + Docker | Média | Alta | ✅ Sim |
| Frontend sem Docker | Alta | Muito Alta | ⚠️ Último recurso |

---

## Próximos Passos

Depois que subir com sucesso:

1. Testar endpoints da API
2. Acessar frontend
3. Criar alguns dados de teste
4. Configurar backup automático
5. Configurar monitoramento
