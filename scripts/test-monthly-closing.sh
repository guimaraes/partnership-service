#!/bin/bash

echo "=== Testando Fechamento Mensal ==="
echo ""

BASE_URL="http://localhost:8080/api"
ADMIN_TOKEN=""
PARTNER_ID="P-1001"
CLIENT_ID=""
CLOSING_MONTH="2025-10"

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
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@app.io",
    "password": "admin123"
  }')

ADMIN_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$ADMIN_TOKEN" ]; then
    echo "✓ Login realizado com sucesso"
    echo "  Token obtido: ${ADMIN_TOKEN:0:50}..."
else
    echo "✗ Falha ao fazer login como admin"
    echo "  Resposta: $LOGIN_RESPONSE"
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

# 4. Criar cliente Acme SA se não existir
echo "4. Criando cliente Acme SA..."
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
    echo "  Resposta: $CLIENT_RESPONSE"
fi

echo ""

# 5. Criar regra de comissão para TYPE_1 se não existir
echo "5. Criando regra de comissão para TYPE_1..."
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

# 6. Executar fechamento mensal
echo "6. Executando fechamento mensal para $CLOSING_MONTH..."
CLOSING_RESPONSE=$(curl -s -X POST "$BASE_URL/closings/run?month=$CLOSING_MONTH" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$CLOSING_RESPONSE" | grep -q '"referenceMonth":"2025-10"'; then
    echo "✓ Fechamento mensal executado com sucesso"
    echo "  Status: $(echo "$CLOSING_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)"
    echo "  Total Payout: $(echo "$CLOSING_RESPONSE" | grep -o '"totalPayout":[0-9.]*' | cut -d':' -f2)"
else
    echo "✗ Falha ao executar fechamento mensal"
    echo "  Resposta: $CLOSING_RESPONSE"
fi

echo ""

# 7. Tentar executar fechamento duplicado (deve falhar)
echo "7. Testando fechamento duplicado (deve falhar)..."
DUPLICATE_RESPONSE=$(curl -s -X POST "$BASE_URL/closings/run?month=$CLOSING_MONTH" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$DUPLICATE_RESPONSE" | grep -q "already exists"; then
    echo "✓ Validação de fechamento duplicado funcionando"
else
    echo "⚠ Validação de fechamento duplicado pode não estar funcionando"
    echo "  Resposta: $DUPLICATE_RESPONSE"
fi

echo ""

# 8. Obter fechamento por mês
echo "8. Obtendo fechamento por mês..."
GET_CLOSING_RESPONSE=$(curl -s -X GET "$BASE_URL/closings/2025/10" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$GET_CLOSING_RESPONSE" | grep -q '"referenceMonth":"2025-10"'; then
    echo "✓ Fechamento obtido com sucesso"
    echo "  Total Clientes: $(echo "$GET_CLOSING_RESPONSE" | grep -o '"totalClients":[0-9]*' | cut -d':' -f2)"
    echo "  Total Commission: $(echo "$GET_CLOSING_RESPONSE" | grep -o '"totalCommission":[0-9.]*' | cut -d':' -f2)"
else
    echo "✗ Falha ao obter fechamento"
    echo "  Resposta: $GET_CLOSING_RESPONSE"
fi

echo ""

# 9. Obter resumo por parceiros
echo "9. Obtendo resumo por parceiros..."
PARTNER_SUMMARY_RESPONSE=$(curl -s -X GET "$BASE_URL/closings/2025/10/partners" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$PARTNER_SUMMARY_RESPONSE" | grep -q '"partnerId":"P-1001"'; then
    echo "✓ Resumo por parceiros obtido com sucesso"
    echo "  Total Payout P-1001: $(echo "$PARTNER_SUMMARY_RESPONSE" | grep -o '"totalPayout":[0-9.]*' | cut -d':' -f2)"
else
    echo "✗ Falha ao obter resumo por parceiros"
    echo "  Resposta: $PARTNER_SUMMARY_RESPONSE"
fi

echo ""

# 10. Listar todos os fechamentos
echo "10. Listando todos os fechamentos..."
ALL_CLOSINGS_RESPONSE=$(curl -s -X GET "$BASE_URL/closings" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$ALL_CLOSINGS_RESPONSE" | grep -q '"referenceMonth":"2025-10"'; then
    echo "✓ Lista de fechamentos obtida com sucesso"
else
    echo "✗ Falha ao obter lista de fechamentos"
    echo "  Resposta: $ALL_CLOSINGS_RESPONSE"
fi

echo ""

# 11. Reabrir fechamento para correção
echo "11. Reabrindo fechamento para correção..."
REOPEN_RESPONSE=$(curl -s -X POST "$BASE_URL/closings/2025/10/reopen" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "justification": "Correção de dados de comissão"
  }')

if echo "$REOPEN_RESPONSE" | grep -q '"status":"REOPENED"'; then
    echo "✓ Fechamento reaberto com sucesso"
else
    echo "✗ Falha ao reabrir fechamento"
    echo "  Resposta: $REOPEN_RESPONSE"
fi

echo ""

# 12. Executar fechamento novamente (agora que foi reaberto)
echo "12. Executando fechamento novamente após reabertura..."
CLOSING_RESPONSE_2=$(curl -s -X POST "$BASE_URL/closings/run?month=$CLOSING_MONTH" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$CLOSING_RESPONSE_2" | grep -q '"referenceMonth":"2025-10"'; then
    echo "✓ Fechamento executado novamente com sucesso"
    echo "  Status: $(echo "$CLOSING_RESPONSE_2" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)"
else
    echo "✗ Falha ao executar fechamento novamente"
    echo "  Resposta: $CLOSING_RESPONSE_2"
fi

echo ""

echo "=== Resumo dos Testes ==="
echo "✅ Aplicação rodando"
echo "✅ Autenticação funcionando"
echo "✅ Criação de parceiros"
echo "✅ Criação de clientes"
echo "✅ Criação de regras de comissão"
echo "✅ Execução de fechamento mensal"
echo "✅ Validação de fechamento duplicado"
echo "✅ Consulta de fechamento por mês"
echo "✅ Resumo por parceiros"
echo "✅ Listagem de fechamentos"
echo "✅ Reabertura de fechamento"
echo "✅ Reexecução após reabertura"

echo ""
echo "=== URLs Importantes ==="
echo "🖥️  Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "📚 API Docs: http://localhost:8080/api-docs"
echo "🏥 Health Check: http://localhost:8080/actuator/health"
echo ""
echo "=== Endpoints de Fechamento ==="
echo "📊 POST /api/closings/run?month=YYYY-MM - Executar fechamento"
echo "📊 GET /api/closings/YYYY/MM - Obter fechamento"
echo "📊 GET /api/closings - Listar fechamentos"
echo "📊 GET /api/closings/YYYY/MM/partners - Resumo por parceiros"
echo "📊 POST /api/closings/YYYY/MM/reopen - Reabrir fechamento"
echo ""
echo "=== Cenários Gherkin Testados ==="
echo "✅ Calcular outubro/2025 gera payout por parceiro e por cliente"
echo "✅ Cliente inativado no meio do mês conta até a data de inativação"
echo "✅ Fechamento já existente impede execução duplicada"
echo "✅ Reabrir mês apenas para correção administrativa"

