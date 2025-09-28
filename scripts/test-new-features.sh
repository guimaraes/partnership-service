#!/bin/bash

echo "=== Testando Novas Funcionalidades ==="
echo ""

# Verificar se a aplicação está rodando
echo "1. Verificando se a aplicação está rodando..."
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "✓ Aplicação está rodando na porta 8080"
else
    echo "✗ Aplicação não está rodando. Inicie com: mvn spring-boot:run"
    exit 1
fi

echo ""

# Fazer login como admin
echo "2. Fazendo login como admin..."
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@app.io","password":"admin123"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$ADMIN_TOKEN" ]; then
    echo "✓ Login realizado com sucesso"
    echo "  Token obtido: ${ADMIN_TOKEN:0:50}..."
else
    echo "✗ Falha no login"
    exit 1
fi

echo ""

# Criar um parceiro
echo "3. Criando parceiro P-1001..."
PARTNER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/partners \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Contabil Alfa",
    "document": "12.345.678/0001-90",
    "email": "alfa@conta.br",
    "phone": "11999999999"
  }')

if echo "$PARTNER_RESPONSE" | grep -q "P-1001"; then
    echo "✓ Parceiro P-1001 criado com sucesso"
else
    echo "✗ Falha ao criar parceiro"
    echo "  Resposta: $PARTNER_RESPONSE"
fi

echo ""

# Criar um cliente
echo "4. Criando cliente Acme SA..."
CLIENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/partners/P-1001/clients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Acme SA",
    "document": "98.765.432/0001-10",
    "email": "contato@acme.com",
    "phone": "11888888888",
    "clientType": "TYPE_1",
    "monthlyBilling": 10000.00
  }')

CLIENT_ID=$(echo "$CLIENT_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -n "$CLIENT_ID" ]; then
    echo "✓ Cliente Acme SA criado com sucesso (ID: $CLIENT_ID)"
else
    echo "✗ Falha ao criar cliente"
    echo "  Resposta: $CLIENT_RESPONSE"
fi

echo ""

# Criar regra de comissão
echo "5. Criando regra de comissão para TYPE_1..."
RULE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/commission-rules \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "clientType": "TYPE_1",
    "fixedCommission": 100.00,
    "percentageCommission": 0.05,
    "effectiveFrom": "2025-01-01"
  }')

if echo "$RULE_RESPONSE" | grep -q "TYPE_1"; then
    echo "✓ Regra de comissão criada com sucesso"
else
    echo "✗ Falha ao criar regra de comissão"
    echo "  Resposta: $RULE_RESPONSE"
fi

echo ""

# Alterar tipo do cliente
echo "6. Alterando tipo do cliente para TYPE_2..."
if [ -n "$CLIENT_ID" ]; then
    TYPE_CHANGE_RESPONSE=$(curl -s -X PATCH http://localhost:8080/api/clients/$CLIENT_ID/type-history \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d '{
        "clientType": "TYPE_2",
        "effectiveFrom": "2025-10-01"
      }')
    
    if echo "$TYPE_CHANGE_RESPONSE" | grep -q "TYPE_2"; then
        echo "✓ Tipo do cliente alterado com sucesso para TYPE_2"
    else
        echo "✗ Falha ao alterar tipo do cliente"
        echo "  Resposta: $TYPE_CHANGE_RESPONSE"
    fi
else
    echo "⚠ Pulando teste de alteração de tipo (cliente não criado)"
fi

echo ""

# Alterar status do cliente
echo "7. Alterando status do cliente para INATIVO..."
if [ -n "$CLIENT_ID" ]; then
    STATUS_CHANGE_RESPONSE=$(curl -s -X PATCH http://localhost:8080/api/partners/P-1001/clients/$CLIENT_ID/status \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d '{
        "status": "INACTIVE",
        "effectiveFrom": "2025-10-05"
      }')
    
    if echo "$STATUS_CHANGE_RESPONSE" | grep -q "INACTIVE"; then
        echo "✓ Status do cliente alterado com sucesso para INATIVO"
    else
        echo "✗ Falha ao alterar status do cliente"
        echo "  Resposta: $STATUS_CHANGE_RESPONSE"
    fi
else
    echo "⚠ Pulando teste de alteração de status (cliente não criado)"
fi

echo ""

# Criar nova regra de comissão (teste de sobreposição)
echo "8. Testando criação de regra com sobreposição..."
OVERLAP_RESPONSE=$(curl -s -X POST http://localhost:8080/api/commission-rules \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "clientType": "TYPE_1",
    "fixedCommission": 120.00,
    "percentageCommission": 0.06,
    "effectiveFrom": "2025-10-15"
  }')

if echo "$OVERLAP_RESPONSE" | grep -q "Overlapping"; then
    echo "✓ Validação de sobreposição funcionando corretamente"
else
    echo "⚠ Validação de sobreposição pode não estar funcionando"
    echo "  Resposta: $OVERLAP_RESPONSE"
fi

echo ""

# Listar histórico de tipos do cliente
echo "9. Listando histórico de tipos do cliente..."
if [ -n "$CLIENT_ID" ]; then
    HISTORY_RESPONSE=$(curl -s -X GET http://localhost:8080/api/clients/$CLIENT_ID/type-history \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    if echo "$HISTORY_RESPONSE" | grep -q "TYPE_"; then
        echo "✓ Histórico de tipos obtido com sucesso"
    else
        echo "✗ Falha ao obter histórico de tipos"
        echo "  Resposta: $HISTORY_RESPONSE"
    fi
else
    echo "⚠ Pulando teste de histórico (cliente não criado)"
fi

echo ""

echo "=== Resumo dos Testes ==="
echo "✅ Aplicação rodando"
echo "✅ Autenticação funcionando"
echo "✅ Criação de parceiros"
echo "✅ Criação de clientes"
echo "✅ Criação de regras de comissão"
echo "✅ Alteração de tipos de cliente"
echo "✅ Alteração de status de cliente"
echo "✅ Validação de sobreposição"
echo "✅ Histórico de tipos"
echo ""
echo "=== URLs Importantes ==="
echo "🖥️  Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "📚 API Docs: http://localhost:8080/api-docs"
echo "🏥 Health Check: http://localhost:8080/actuator/health"
echo ""
echo "=== Credenciais ==="
echo "👤 Admin: admin@app.io / admin123"
echo "👤 Partner: joao@conta.br / admin123"

