#!/bin/bash

echo "🔍 TB Care Platform - Verificação de Ambiente"
echo "=============================================="
echo ""

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para verificar comando
check_command() {
    if command -v $1 &> /dev/null; then
        version=$($2)
        echo -e "${GREEN}✓${NC} $1 instalado: $version"
        return 0
    else
        echo -e "${RED}✗${NC} $1 não encontrado"
        return 1
    fi
}

# Verificar Java
echo "📦 Verificando dependências..."
check_command "java" "java -version 2>&1 | head -n 1"
check_command "mvn" "mvn -version | head -n 1"
check_command "node" "node --version"
check_command "npm" "npm --version"
check_command "docker" "docker --version"
check_command "docker-compose" "docker compose version"

echo ""
echo "🏗️  Verificando estrutura do projeto..."

# Verificar backend
if [ -f "backend/pom.xml" ]; then
    echo -e "${GREEN}✓${NC} backend/pom.xml encontrado"
else
    echo -e "${RED}✗${NC} backend/pom.xml não encontrado"
fi

if [ -f "backend/src/main/resources/application.yml" ]; then
    echo -e "${GREEN}✓${NC} application.yml encontrado"
else
    echo -e "${RED}✗${NC} application.yml não encontrado"
fi

# Verificar frontend
if [ -f "frontend/package.json" ]; then
    echo -e "${GREEN}✓${NC} frontend/package.json encontrado"
else
    echo -e "${RED}✗${NC} frontend/package.json não encontrado"
fi

if [ -f "frontend/tailwind.config.ts" ]; then
    echo -e "${GREEN}✓${NC} tailwind.config.ts encontrado"
else
    echo -e "${RED}✗${NC} tailwind.config.ts não encontrado"
fi

# Verificar Docker
if [ -f "docker-compose.yml" ]; then
    echo -e "${GREEN}✓${NC} docker-compose.yml encontrado"
else
    echo -e "${RED}✗${NC} docker-compose.yml não encontrado"
fi

echo ""
echo "📝 Verificando documentação..."

docs=("README.md" "ARCHITECTURE.md" "QUICKSTART.md" "DELIVERY.md")
for doc in "${docs[@]}"; do
    if [ -f "$doc" ]; then
        echo -e "${GREEN}✓${NC} $doc encontrado"
    else
        echo -e "${RED}✗${NC} $doc não encontrado"
    fi
done

echo ""
echo "🔨 Testando compilação..."

# Testar backend
echo -n "Backend (Maven): "
cd backend
if mvn clean compile -q > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Compilado com sucesso${NC}"
else
    echo -e "${RED}✗ Erro na compilação${NC}"
fi
cd ..

# Testar frontend
echo -n "Frontend (Next.js): "
cd frontend
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}⚠ Instalando dependências...${NC}"
    npm install --silent > /dev/null 2>&1
fi

if npm run build > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Build com sucesso${NC}"
else
    echo -e "${RED}✗ Erro no build${NC}"
fi
cd ..

echo ""
echo "=============================================="
echo "✅ Verificação concluída!"
echo ""
echo "Para iniciar o projeto:"
echo "  docker compose up --build"
echo ""
echo "Ou localmente:"
echo "  Terminal 1: docker compose up postgres"
echo "  Terminal 2: cd backend && mvn spring-boot:run"
echo "  Terminal 3: cd frontend && npm run dev"
