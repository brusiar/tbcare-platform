#!/bin/sh

# Substituir a URL da API nos arquivos buildados
if [ -n "$NEXT_PUBLIC_API_URL" ]; then
  echo "Configurando API_URL para: $NEXT_PUBLIC_API_URL"
  find /app/.next -type f -name "*.js" -exec sed -i "s|http://localhost:8081/api|$NEXT_PUBLIC_API_URL|g" {} +
fi

# Iniciar o servidor
exec node server.js
