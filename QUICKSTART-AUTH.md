# 🚀 Quick Start - Autenticação JWT

## ✅ O que foi implementado

A ETAPA 2 está completa! O TB Care Platform agora possui:

- ✅ Autenticação JWT funcional
- ✅ Login com email/senha
- ✅ Isolamento multi-tenant automático
- ✅ Spring Security configurado
- ✅ Roles: ADMIN, PROFESSIONAL, PATIENT
- ✅ Usuários seed para desenvolvimento

---

## 🏃 Iniciar Rapidamente

### 1. Subir o Backend

```bash
cd backend
mvn spring-boot:run
```

Aguarde até ver: `Started TbCareApplication`

### 2. Testar Login

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@tbcare.com", "password": "admin123"}'
```

### 3. Copiar Token

Copie o valor do campo `token` da resposta.

### 4. Usar Token

```bash
# Substitua <TOKEN> pelo token copiado
curl http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 👥 Usuários Disponíveis

### Admin
```
Email: admin@tbcare.com
Senha: admin123
Role: ADMIN
```

### Profissional
```
Email: joao@tbcare.com
Senha: prof123
Role: PROFESSIONAL
```

---

## 🧪 Teste Automatizado

```bash
./test-auth.sh
```

Este script testa:
- ✅ Endpoint público (health)
- ✅ Endpoint protegido sem token (deve falhar)
- ✅ Login com admin
- ✅ Endpoint /auth/me
- ✅ Endpoint protegido com token
- ✅ Login com professional

---

## 📚 Documentação

### Arquitetura de Segurança
[SECURITY-ARCHITECTURE.md](./SECURITY-ARCHITECTURE.md)
- Fluxo de autenticação completo
- Componentes de segurança
- Isolamento multi-tenant
- Boas práticas

### Guia de Testes
[AUTH-TESTING.md](./AUTH-TESTING.md)
- Testes manuais com cURL
- Testes com Postman/Insomnia
- Troubleshooting

### Resumo da Implementação
[ETAPA-2-COMPLETA.md](./ETAPA-2-COMPLETA.md)
- Status dos entregáveis
- Componentes implementados
- Próximos passos

---

## 🔑 Endpoints

### Públicos (sem autenticação)
- `GET /api/health` - Status da API
- `POST /api/auth/login` - Login

### Protegidos (requer JWT)
- `GET /api/auth/me` - Dados do usuário autenticado
- `GET /api/patients` - Listar pacientes
- `POST /api/patients` - Criar paciente
- `GET /api/appointments` - Listar consultas
- `POST /api/appointments` - Criar consulta

---

## 🔐 Como Funciona

1. **Login**: Cliente envia email/senha
2. **JWT**: Backend retorna token com userId, tenantId, role
3. **Requisições**: Cliente envia token no header `Authorization: Bearer <token>`
4. **Validação**: Filtro JWT valida token e extrai tenantId
5. **Isolamento**: TenantContext garante que cada query filtra por tenant
6. **Resposta**: Dados isolados por tenant retornados ao cliente

---

## 🎯 Próximos Passos

### Desenvolvimento
1. Implementar testes unitários
2. Implementar testes de integração
3. Adicionar paginação nas listagens
4. Integrar frontend com autenticação

### Produção
1. Implementar refresh tokens
2. Adicionar rate limiting
3. Configurar monitoramento
4. Alterar senhas padrão

---

## ⚠️ Importante

- **Senhas**: As senhas de desenvolvimento devem ser alteradas em produção
- **JWT Secret**: Alterar `JWT_SECRET` para um valor seguro em produção
- **HTTPS**: Usar HTTPS em produção para proteger tokens
- **Tenant**: Nunca aceitar tenantId do frontend, sempre do JWT

---

## 🆘 Problemas?

### Backend não inicia
```bash
# Verificar se o PostgreSQL está rodando
docker compose up postgres

# Limpar e recompilar
cd backend
mvn clean install
mvn spring-boot:run
```

### Login falha
```bash
# Verificar se as migrations rodaram
psql -h localhost -p 5432 -U tbcare -d tbcare
SELECT * FROM users;
```

### Token inválido
- Fazer login novamente para obter novo token
- Verificar se o token não expirou (24h)
- Verificar se está usando `Bearer <token>` no header

---

## 📞 Suporte

Consulte a documentação completa:
- [README.md](./README.md) - Visão geral do projeto
- [SECURITY-ARCHITECTURE.md](./SECURITY-ARCHITECTURE.md) - Arquitetura de segurança
- [AUTH-TESTING.md](./AUTH-TESTING.md) - Guia de testes
- [ETAPA-2-COMPLETA.md](./ETAPA-2-COMPLETA.md) - Status da implementação
