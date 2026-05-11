# 🔧 Troubleshooting - Problemas de DNS/Rede no Docker

## Erro: "server misbehaving" ou "failed to fetch anonymous token"

Este erro ocorre quando o Docker não consegue resolver DNS para baixar imagens do Docker Hub.

---

## ✅ Solução 1: Configurar DNS do Docker (Recomendado)

### Passo 1: Editar daemon.json

```bash
sudo nano /etc/docker/daemon.json
```

### Passo 2: Adicionar configuração de DNS

Se o arquivo estiver vazio, adicione:

```json
{
  "dns": ["8.8.8.8", "8.8.4.4", "1.1.1.1"]
}
```

Se já tiver conteúdo, adicione apenas a linha `"dns"`:

```json
{
  "log-driver": "json-file",
  "dns": ["8.8.8.8", "8.8.4.4", "1.1.1.1"]
}
```

### Passo 3: Salvar e reiniciar Docker

```bash
# Salvar: Ctrl+O, Enter, Ctrl+X

# Reiniciar Docker
sudo systemctl restart docker

# Verificar status
sudo systemctl status docker
```

### Passo 4: Tentar novamente

```bash
cd ~/apps/tbcare-platform
docker compose up -d --build
```

---

## ✅ Solução 2: Verificar e Corrigir Conectividade

### Testar DNS

```bash
# Testar resolução de DNS
nslookup auth.docker.io
nslookup registry-1.docker.io

# Se falhar, testar DNS público
nslookup google.com 8.8.8.8
```

### Testar Conectividade

```bash
# Testar ping
ping -c 3 8.8.8.8
ping -c 3 1.1.1.1

# Testar acesso ao Docker Hub
curl -I https://hub.docker.com
curl -I https://registry-1.docker.io
```

### Verificar Proxy

```bash
# Ver se há proxy configurado
echo $HTTP_PROXY
echo $HTTPS_PROXY
echo $NO_PROXY

# Se houver proxy, configurar no Docker
sudo nano /etc/systemd/system/docker.service.d/http-proxy.conf
```

Adicionar:
```ini
[Service]
Environment="HTTP_PROXY=http://proxy.example.com:8080"
Environment="HTTPS_PROXY=http://proxy.example.com:8080"
Environment="NO_PROXY=localhost,127.0.0.1"
```

Depois:
```bash
sudo systemctl daemon-reload
sudo systemctl restart docker
```

---

## ✅ Solução 3: Usar Imagens Já Baixadas

Se você já tem outros projetos rodando no servidor, pode usar as mesmas imagens base.

### Verificar imagens disponíveis

```bash
docker images | grep -E "eclipse-temurin|node|postgres"
```

### Ajustar Dockerfiles para usar imagens locais

Se você tem `openjdk:21` ou similar:

**backend/Dockerfile:**
```dockerfile
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Se você tem `node:20`:

**frontend/Dockerfile:**
```dockerfile
FROM node:20-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci

FROM node:20-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
EXPOSE 3000
ENV PORT=3000
CMD ["node", "server.js"]
```

---

## ✅ Solução 4: Build Local (Sem Docker)

Se o servidor já tem Java e Node.js instalados, pode rodar sem Docker.

### Verificar versões instaladas

```bash
java -version    # Precisa ser 21+
node -v          # Precisa ser 20+
mvn -v           # Maven 3.9+
```

### Backend

```bash
cd ~/apps/tbcare-platform/backend

# Compilar
mvn clean package -DskipTests

# Rodar
java -jar target/*.jar
```

Ou com Maven:
```bash
mvn spring-boot:run
```

### Frontend

```bash
cd ~/apps/tbcare-platform/frontend

# Instalar dependências
npm install

# Build
npm run build

# Rodar
npm start
```

### PostgreSQL (via Docker)

```bash
docker run -d \
  --name tbcare-postgres \
  -e POSTGRES_DB=tbcare \
  -e POSTGRES_USER=tbcare \
  -e POSTGRES_PASSWORD=tbcare \
  -p 5434:5432 \
  postgres:16-alpine
```

Se o PostgreSQL também falhar, use o já instalado no servidor:

```bash
# Criar banco
sudo -u postgres psql -c "CREATE DATABASE tbcare;"
sudo -u postgres psql -c "CREATE USER tbcare WITH PASSWORD 'tbcare';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE tbcare TO tbcare;"
```

Ajustar `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tbcare
```

---

## ✅ Solução 5: Baixar Imagens Manualmente

Se o problema for temporário, tente baixar as imagens uma por vez:

```bash
# Backend
docker pull eclipse-temurin:21-jdk-alpine
docker pull eclipse-temurin:21-jre-alpine

# Frontend
docker pull node:20-alpine

# PostgreSQL
docker pull postgres:16-alpine
```

Se alguma falhar, espere alguns minutos e tente novamente.

---

## 🔍 Diagnóstico Completo

Execute este script para diagnóstico:

```bash
#!/bin/bash

echo "=== Diagnóstico Docker DNS ==="
echo ""

echo "1. Testando DNS do sistema:"
nslookup google.com
echo ""

echo "2. Testando DNS público:"
nslookup google.com 8.8.8.8
echo ""

echo "3. Testando Docker Hub:"
nslookup registry-1.docker.io
echo ""

echo "4. Testando conectividade:"
ping -c 3 8.8.8.8
echo ""

echo "5. Testando HTTPS:"
curl -I https://hub.docker.com
echo ""

echo "6. Verificando proxy:"
echo "HTTP_PROXY: $HTTP_PROXY"
echo "HTTPS_PROXY: $HTTPS_PROXY"
echo ""

echo "7. Configuração Docker DNS:"
cat /etc/docker/daemon.json 2>/dev/null || echo "Arquivo não existe"
echo ""

echo "8. Imagens disponíveis:"
docker images | head -10
echo ""

echo "=== Fim do diagnóstico ==="
```

Salve como `diagnose.sh`, dê permissão e execute:

```bash
chmod +x diagnose.sh
./diagnose.sh
```

---

## 📞 Próximos Passos

Depois de aplicar uma das soluções acima:

1. Tentar build novamente:
   ```bash
   cd ~/apps/tbcare-platform
   docker compose up -d --build
   ```

2. Ver logs em tempo real:
   ```bash
   docker compose logs -f
   ```

3. Verificar status:
   ```bash
   docker compose ps
   ```

4. Testar health check:
   ```bash
   curl http://localhost:8082/api/health
   ```

---

## 🆘 Se Nada Funcionar

Entre em contato informando:

1. Resultado do script de diagnóstico
2. Versão do Docker: `docker --version`
3. Sistema operacional: `cat /etc/os-release`
4. Logs completos: `docker compose logs > logs.txt`
