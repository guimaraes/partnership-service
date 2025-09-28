#!/bin/bash

echo "=== Testando Swagger UI ==="
echo ""

# Verificar se a aplicação está rodando
echo "1. Verificando se a aplicação está rodando..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✓ Aplicação está rodando na porta 8080"
else
    echo "✗ Aplicação não está rodando. Inicie com: mvn spring-boot:run"
    exit 1
fi

echo ""

# Testar acesso ao OpenAPI JSON
echo "2. Testando acesso à documentação OpenAPI..."
if curl -s http://localhost:8080/api-docs | grep -q "openapi"; then
    echo "✓ Documentação OpenAPI acessível em http://localhost:8080/api-docs"
else
    echo "✗ Erro ao acessar documentação OpenAPI"
fi

echo ""

# Testar acesso ao Swagger UI
echo "3. Testando acesso ao Swagger UI..."
if curl -s http://localhost:8080/swagger-ui/index.html | grep -q "swagger"; then
    echo "✓ Swagger UI acessível em http://localhost:8080/swagger-ui/index.html"
else
    echo "✗ Erro ao acessar Swagger UI"
fi

echo ""

# Testar endpoint de login via Swagger
echo "4. Testando endpoint de login..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@app.io","password":"admin123"}')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo "✓ Endpoint de login funcionando"
    echo "  Token obtido: $(echo $LOGIN_RESPONSE | cut -c1-50)..."
else
    echo "✗ Erro no endpoint de login"
    echo "  Resposta: $LOGIN_RESPONSE"
fi

echo ""
echo "=== URLs do Swagger ==="
echo "📚 Documentação OpenAPI JSON: http://localhost:8080/api-docs"
echo "🖥️  Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo ""
echo "=== Credenciais para teste ==="
echo "👤 Admin: admin@app.io / admin123"
echo "👤 Partner: joao@conta.br / admin123"
echo ""
echo "=== Como usar ==="
echo "1. Acesse http://localhost:8080/swagger-ui/index.html"
echo "2. Clique em 'Authorize' (cadeado)"
echo "3. Faça login via POST /api/auth/login"
echo "4. Copie o token retornado"
echo "5. Cole no campo 'Value' do Authorization"
echo "6. Clique em 'Authorize'"
echo "7. Teste os endpoints protegidos"
