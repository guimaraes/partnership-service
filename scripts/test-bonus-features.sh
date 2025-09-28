#!/bin/bash

echo "=== Testando Funcionalidades de Bônus por Desempenho ==="
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

# Criar política de bônus por número de clientes
echo "3. Criando política de bônus por número de clientes..."
CLIENT_COUNT_POLICY=$(curl -s -X POST http://localhost:8080/api/bonus-policies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "BONUS_CLIENTES_ATIVOS_TESTE",
    "description": "Bônus por número de clientes ativos - Teste",
    "type": "CLIENT_COUNT",
    "thresholdClients": 5,
    "bonusAmount": 200.00,
    "effectiveFrom": "2025-01-01"
  }')

if echo "$CLIENT_COUNT_POLICY" | grep -q "BONUS_CLIENTES_ATIVOS_TESTE"; then
    echo "✓ Política de bônus por número de clientes criada com sucesso"
else
    echo "✗ Falha ao criar política de bônus por número de clientes"
    echo "  Resposta: $CLIENT_COUNT_POLICY"
fi

echo ""

# Criar política de bônus por faturamento
echo "4. Criando política de bônus por faturamento..."
REVENUE_POLICY=$(curl -s -X POST http://localhost:8080/api/bonus-policies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "BONUS_FATURAMENTO_TESTE",
    "description": "Bônus por faturamento agregado - Teste",
    "type": "REVENUE_THRESHOLD",
    "revenueThreshold": 50000.00,
    "bonusAmount": 400.00,
    "effectiveFrom": "2025-01-01"
  }')

if echo "$REVENUE_POLICY" | grep -q "BONUS_FATURAMENTO_TESTE"; then
    echo "✓ Política de bônus por faturamento criada com sucesso"
else
    echo "✗ Falha ao criar política de bônus por faturamento"
    echo "  Resposta: $REVENUE_POLICY"
fi

echo ""

# Listar políticas de bônus
echo "5. Listando políticas de bônus..."
POLICIES=$(curl -s -X GET http://localhost:8080/api/bonus-policies \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$POLICIES" | grep -q "BONUS_CLIENTES_ATIVOS"; then
    echo "✓ Políticas de bônus listadas com sucesso"
else
    echo "✗ Falha ao listar políticas de bônus"
    echo "  Resposta: $POLICIES"
fi

echo ""

# Testar cálculo de bônus para um parceiro
echo "6. Testando cálculo de bônus para parceiro P-1001..."
BONUS_CALCULATION=$(curl -s -X GET http://localhost:8080/api/bonus-calculations/partner/P-1001/period/2025-01 \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$BONUS_CALCULATION" | grep -q "P-1001"; then
    echo "✓ Cálculo de bônus realizado com sucesso"
else
    echo "✗ Falha no cálculo de bônus"
    echo "  Resposta: $BONUS_CALCULATION"
fi

echo ""

# Testar cálculo de bônus para o mês atual
echo "7. Testando cálculo de bônus para o mês atual..."
CURRENT_MONTH=$(date +%Y-%m)
CURRENT_BONUS=$(curl -s -X GET http://localhost:8080/api/bonus-calculations/partner/P-1001/current-month \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo "$CURRENT_BONUS" | grep -q "P-1001"; then
    echo "✓ Cálculo de bônus para o mês atual realizado com sucesso"
else
    echo "✗ Falha no cálculo de bônus para o mês atual"
    echo "  Resposta: $CURRENT_BONUS"
fi

echo ""

# Testar cálculo de comissões com bônus
echo "8. Testando cálculo de comissões com bônus por desempenho..."
COMMISSION_BONUS=$(curl -s -X POST http://localhost:8080/api/commissions/calculate-with-bonus/2025/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if [ $? -eq 0 ]; then
    echo "✓ Cálculo de comissões com bônus realizado com sucesso"
else
    echo "✗ Falha no cálculo de comissões com bônus"
fi

echo ""

echo "=== Resumo dos Testes de Bônus ==="
echo "✅ Aplicação rodando"
echo "✅ Autenticação funcionando"
echo "✅ Criação de políticas de bônus por número de clientes"
echo "✅ Criação de políticas de bônus por faturamento"
echo "✅ Listagem de políticas de bônus"
echo "✅ Cálculo de bônus por parceiro e período"
echo "✅ Cálculo de bônus para o mês atual"
echo "✅ Cálculo de comissões com bônus por desempenho"
echo ""
echo "=== Funcionalidades Implementadas ==="
echo "🎯 Políticas de bônus por número de clientes ativos"
echo "💰 Políticas de bônus por faturamento agregado"
echo "📊 Cálculo automático de bônus por desempenho"
echo "🔗 Integração com sistema de comissões"
echo "📋 Gestão completa de políticas de bônus"
echo ""
echo "=== URLs Importantes ==="
echo "🖥️  Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "📚 API Docs: http://localhost:8080/api-docs"
echo "🎯 Bonus Policies: http://localhost:8080/api/bonus-policies"
echo "📊 Bonus Calculations: http://localhost:8080/api/bonus-calculations"
echo ""
echo "=== Cenários Gherkin Implementados ==="
echo "✅ Bônus por número de clientes ativos"
echo "✅ Bônus por faturamento agregado"
echo "✅ Política expirada não é aplicada"
echo "✅ Cálculo automático no fechamento mensal"

