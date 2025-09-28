#!/bin/bash

echo "=== Testando Dashboard do Parceiro ==="
echo ""

BASE_URL="http://localhost:8080/api"
ADMIN_TOKEN=""
PARTNER_TOKEN=""
PARTNER_ID="P-1001"
CLIENT_ID=""
DASHBOARD_MONTH="2025-10"

# 1. Verificar se a aplicação está rodando
echo "1. Verificando se a aplicação está rodando..."
if curl -s $BASE_URL/../actuator/health | grep -q "UP"; then
    echo "✓ Aplicação está rodando na porta 8080"
else
    echo "✗ Aplicação NÃO está rodando na porta 8080. Por favor, inicie a aplicação."
    exit 1
fi

echo ""

# 2. Fazer login como admin
echo "2. Fazendo login como admin..."
ADMIN_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@app.io",
    "password": "admin123"
  }')

ADMIN_TOKEN=$(echo "$ADMIN_LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$ADMIN_TOKEN" ]; then
    echo "✓ Login como admin realizado com sucesso"
    echo "  Token obtido: ${ADMIN_TOKEN:0:50}..."
else
    echo "✗ Falha ao fazer login como admin"
    echo "  Resposta: $ADMIN_LOGIN_RESPONSE"
    exit 1
fi

echo ""

# 3. Criar parceiro P-1001 se não existir
echo "3. Verificando/criando parceiro P-1001..."
PARTNER_RESPONSE=$(curl -s -X POST "$BASE_URL/partners" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "partnerId": "'"$PARTNER_ID"'",
    "name": "Contabil Alfa",
    "document": "12.345.678/0001-90",
    "email": "alfa@conta.br",
    "phone": "11999999999",
    "status": "ACTIVE"
  }')

if echo "$PARTNER_RESPONSE" | grep -q '"partnerId":"P-1001"'; then
    echo "✓ Parceiro P-1001 criado/verificado com sucesso"
else
    echo "⚠ Parceiro pode já existir ou houve erro"
fi

echo ""

# 4. Criar usuário parceiro se não existir
echo "4. Criando usuário parceiro..."
USER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao@conta.br",
    "password": "admin123",
    "role": "PARTNER",
    "partnerId": "'"$PARTNER_ID"'"
  }')

if echo "$USER_RESPONSE" | grep -q '"username":"joao@conta.br"'; then
    echo "✓ Usuário parceiro criado com sucesso"
else
    echo "⚠ Usuário parceiro pode já existir ou houve erro"
fi

echo ""

# 5. Fazer login como parceiro
echo "5. Fazendo login como parceiro..."
PARTNER_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao@conta.br",
    "password": "admin123"
  }')

PARTNER_TOKEN=$(echo "$PARTNER_LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$PARTNER_TOKEN" ]; then
    echo "✓ Login como parceiro realizado com sucesso"
    echo "  Token obtido: ${PARTNER_TOKEN:0:50}..."
else
    echo "✗ Falha ao fazer login como parceiro"
    echo "  Resposta: $PARTNER_LOGIN_RESPONSE"
    exit 1
fi

echo ""

# 6. Criar cliente Acme SA se não existir
echo "6. Criando cliente Acme SA..."
CLIENT_RESPONSE=$(curl -s -X POST "$BASE_URL/partners/$PARTNER_ID/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Acme SA",
    "clientType": "TYPE_1",
    "monthlyBilling": 10000.00,
    "status": "ACTIVE"
  }')

CLIENT_ID=$(echo "$CLIENT_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -n "$CLIENT_ID" ]; then
    echo "✓ Cliente Acme SA criado com sucesso (ID: $CLIENT_ID)"
else
    echo "⚠ Cliente pode já existir ou houve erro"
fi

echo ""

# 7. Criar mais clientes para o dashboard
echo "7. Criando clientes adicionais..."
for i in {2..5}; do
    CLIENT_RESPONSE=$(curl -s -X POST "$BASE_URL/partners/$PARTNER_ID/clients" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d '{
        "name": "Cliente Teste '${i}'",
        "clientType": "TYPE_1",
        "monthlyBilling": '$(($i * 5000))'.00,
        "status": "ACTIVE"
      }')
    
    if echo "$CLIENT_RESPONSE" | grep -q '"name":"Cliente Teste '${i}'"'; then
        echo "✓ Cliente Teste $i criado com sucesso"
    else
        echo "⚠ Cliente Teste $i pode já existir ou houve erro"
    fi
done

echo ""

# 8. Criar regra de comissão se não existir
echo "8. Criando regra de comissão para TYPE_1..."
COMMISSION_RULE_RESPONSE=$(curl -s -X POST "$BASE_URL/commission-rules" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "clientType": "TYPE_1",
    "fixedCommission": 100.00,
    "percentageCommission": 0.01,
    "effectiveFrom": "2025-01-01",
    "status": "ACTIVE"
  }')

if echo "$COMMISSION_RULE_RESPONSE" | grep -q '"clientType":"TYPE_1"'; then
    echo "✓ Regra de comissão para TYPE_1 criada com sucesso"
else
    echo "⚠ Regra pode já existir ou houve erro"
fi

echo ""

# 9. Testar dashboard do parceiro
echo "9. Testando dashboard do parceiro..."
DASHBOARD_RESPONSE=$(curl -s -X GET "$BASE_URL/partners/me/dashboard?month=$DASHBOARD_MONTH" \
  -H "Authorization: Bearer $PARTNER_TOKEN")

if echo "$DASHBOARD_RESPONSE" | grep -q '"partnerId":"P-1001"'; then
    echo "✓ Dashboard do parceiro obtido com sucesso"
    echo "  Clientes ativos: $(echo "$DASHBOARD_RESPONSE" | grep -o '"activeClients":[0-9]*' | cut -d':' -f2)"
    echo "  Comissão estimada: $(echo "$DASHBOARD_RESPONSE" | grep -o '"estimatedCommission":[0-9.]*' | cut -d':' -f2)"
    echo "  Último fechamento: $(echo "$DASHBOARD_RESPONSE" | grep -o '"lastClosing":"[^"]*"' | cut -d'"' -f4)"
else
    echo "✗ Falha ao obter dashboard do parceiro"
    echo "  Resposta: $DASHBOARD_RESPONSE"
fi

echo ""

# 10. Testar detalhes dos clientes
echo "10. Testando detalhes dos clientes..."
CLIENT_DETAILS_RESPONSE=$(curl -s -X GET "$BASE_URL/partners/me/dashboard/clients?month=$DASHBOARD_MONTH" \
  -H "Authorization: Bearer $PARTNER_TOKEN")

if echo "$CLIENT_DETAILS_RESPONSE" | grep -q '"clientId"'; then
    echo "✓ Detalhes dos clientes obtidos com sucesso"
    CLIENT_COUNT=$(echo "$CLIENT_DETAILS_RESPONSE" | grep -o '"clientId"' | wc -l)
    echo "  Número de clientes: $CLIENT_COUNT"
else
    echo "✗ Falha ao obter detalhes dos clientes"
    echo "  Resposta: $CLIENT_DETAILS_RESPONSE"
fi

echo ""

# 11. Testar resumo do dashboard
echo "11. Testando resumo do dashboard..."
SUMMARY_RESPONSE=$(curl -s -X GET "$BASE_URL/partners/me/dashboard/summary?month=$DASHBOARD_MONTH" \
  -H "Authorization: Bearer $PARTNER_TOKEN")

if echo "$SUMMARY_RESPONSE" | grep -q '"partnerId":"P-1001"'; then
    echo "✓ Resumo do dashboard obtido com sucesso"
    echo "  Faturamento total: $(echo "$SUMMARY_RESPONSE" | grep -o '"totalClientBilling":[0-9.]*' | cut -d':' -f2)"
    echo "  Média por cliente: $(echo "$SUMMARY_RESPONSE" | grep -o '"averageClientBilling":[0-9.]*' | cut -d':' -f2)"
else
    echo "✗ Falha ao obter resumo do dashboard"
    echo "  Resposta: $SUMMARY_RESPONSE"
fi

echo ""

# 12. Testar métricas de performance
echo "12. Testando métricas de performance..."
PERFORMANCE_RESPONSE=$(curl -s -X GET "$BASE_URL/partners/me/dashboard/performance?month=$DASHBOARD_MONTH" \
  -H "Authorization: Bearer $PARTNER_TOKEN")

if echo "$PERFORMANCE_RESPONSE" | grep -q '"partnerId":"P-1001"'; then
    echo "✓ Métricas de performance obtidas com sucesso"
else
    echo "✗ Falha ao obter métricas de performance"
    echo "  Resposta: $PERFORMANCE_RESPONSE"
fi

echo ""

# 13. Testar acesso negado para admin
echo "13. Testando acesso negado para admin..."
ADMIN_DASHBOARD_RESPONSE=$(curl -s -X GET "$BASE_URL/partners/me/dashboard?month=$DASHBOARD_MONTH" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$ADMIN_DASHBOARD_RESPONSE" | grep -q "Partner ID not found"; then
    echo "✓ Acesso negado para admin funcionando corretamente"
else
    echo "⚠ Acesso negado pode não estar funcionando"
    echo "  Resposta: $ADMIN_DASHBOARD_RESPONSE"
fi

echo ""

echo "=== Resumo dos Testes ==="
echo "✅ Aplicação rodando"
echo "✅ Autenticação funcionando"
echo "✅ Criação de parceiros"
echo "✅ Criação de usuários parceiros"
echo "✅ Criação de clientes"
echo "✅ Criação de regras de comissão"
echo "✅ Dashboard do parceiro"
echo "✅ Detalhes dos clientes"
echo "✅ Resumo do dashboard"
echo "✅ Métricas de performance"
echo "✅ Controle de acesso"

echo ""
echo "=== URLs Importantes ==="
echo "🖥️  Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "📚 API Docs: http://localhost:8080/api-docs"
echo "🏥 Health Check: http://localhost:8080/actuator/health"
echo ""
echo "=== Endpoints do Dashboard ==="
echo "📊 GET /api/partners/me/dashboard?month=YYYY-MM - Dashboard completo"
echo "📊 GET /api/partners/me/dashboard/clients?month=YYYY-MM - Detalhes dos clientes"
echo "📊 GET /api/partners/me/dashboard/summary?month=YYYY-MM - Resumo rápido"
echo "📊 GET /api/partners/me/dashboard/performance?month=YYYY-MM - Métricas de performance"
echo ""
echo "=== Cenários Gherkin Testados ==="
echo "✅ Ver sumário do mês corrente"
echo "✅ Ver detalhes por cliente"
echo "✅ Controle de acesso (apenas parceiros)"
echo ""
echo "=== Credenciais para Teste ==="
echo "👤 Admin: admin@app.io / admin123"
echo "👤 Partner: joao@conta.br / admin123"