#!/bin/bash

# Script para testar a API do Partnership Service
# Certifique-se de que a aplicação esteja rodando em http://localhost:8080

BASE_URL="http://localhost:8080/api"
ADMIN_USER="admin@app.io"
ADMIN_PASS="admin123"
PARTNER_USER="joao@conta.br"
PARTNER_PASS="admin123"

echo "=== Testando Partnership Service API ==="
echo

# Função para fazer login e obter token
login() {
    local username=$1
    local password=$2
    local response=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    
    echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4
}

# Função para fazer requisição autenticada
authenticated_request() {
    local method=$1
    local url=$2
    local token=$3
    local data=$4
    
    if [ -n "$data" ]; then
        curl -s -X "$method" "$url" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$data"
    else
        curl -s -X "$method" "$url" \
            -H "Authorization: Bearer $token"
    fi
}

echo "1. Testando login do administrador..."
ADMIN_TOKEN=$(login "$ADMIN_USER" "$ADMIN_PASS")
if [ -n "$ADMIN_TOKEN" ]; then
    echo "✓ Login do administrador bem-sucedido"
    echo "Token: ${ADMIN_TOKEN:0:50}..."
else
    echo "✗ Falha no login do administrador"
    exit 1
fi
echo

echo "2. Testando login do parceiro..."
PARTNER_TOKEN=$(login "$PARTNER_USER" "$PARTNER_PASS")
if [ -n "$PARTNER_TOKEN" ]; then
    echo "✓ Login do parceiro bem-sucedido"
    echo "Token: ${PARTNER_TOKEN:0:50}..."
else
    echo "✗ Falha no login do parceiro"
    exit 1
fi
echo

echo "3. Testando listagem de parceiros (ADMIN)..."
PARTNERS_RESPONSE=$(authenticated_request "GET" "$BASE_URL/partners" "$ADMIN_TOKEN")
echo "Resposta: $PARTNERS_RESPONSE"
echo

echo "4. Testando criação de novo parceiro (ADMIN)..."
NEW_PARTNER_DATA='{
    "name": "Contabil Beta",
    "document": "98.765.432/0001-10",
    "email": "beta@conta.br",
    "phone": "11888888888"
}'
NEW_PARTNER_RESPONSE=$(authenticated_request "POST" "$BASE_URL/partners" "$ADMIN_TOKEN" "$NEW_PARTNER_DATA")
echo "Resposta: $NEW_PARTNER_RESPONSE"
echo

echo "5. Testando criação de cliente para o parceiro P-1001..."
NEW_CLIENT_DATA='{
    "name": "Empresa XYZ",
    "document": "11.222.333/0001-44",
    "email": "contato@empresaxyz.com",
    "phone": "11777777777",
    "clientType": "TYPE_2",
    "monthlyBilling": 5000.00
}'
NEW_CLIENT_RESPONSE=$(authenticated_request "POST" "$BASE_URL/partners/P-1001/clients" "$ADMIN_TOKEN" "$NEW_CLIENT_DATA")
echo "Resposta: $NEW_CLIENT_RESPONSE"
echo

echo "6. Testando listagem de clientes do parceiro P-1001..."
CLIENTS_RESPONSE=$(authenticated_request "GET" "$BASE_URL/partners/P-1001/clients" "$ADMIN_TOKEN")
echo "Resposta: $CLIENTS_RESPONSE"
echo

echo "7. Testando listagem de regras de comissão..."
COMMISSION_RULES_RESPONSE=$(authenticated_request "GET" "$BASE_URL/commission-rules" "$ADMIN_TOKEN")
echo "Resposta: $COMMISSION_RULES_RESPONSE"
echo

echo "8. Testando cálculo de comissões para o mês atual..."
CURRENT_YEAR=$(date +%Y)
CURRENT_MONTH=$(date +%m)
COMMISSION_CALC_RESPONSE=$(authenticated_request "POST" "$BASE_URL/commissions/calculate/$CURRENT_YEAR/$CURRENT_MONTH" "$ADMIN_TOKEN")
echo "Resposta: $COMMISSION_CALC_RESPONSE"
echo

echo "9. Testando listagem de comissões do parceiro P-1001..."
COMMISSIONS_RESPONSE=$(authenticated_request "GET" "$BASE_URL/commissions/partner/P-1001" "$ADMIN_TOKEN")
echo "Resposta: $COMMISSIONS_RESPONSE"
echo

echo "10. Testando relatório de parceiros para o mês atual..."
REPORT_RESPONSE=$(authenticated_request "GET" "$BASE_URL/reports/partners/$CURRENT_YEAR/$CURRENT_MONTH" "$ADMIN_TOKEN")
echo "Resposta: $REPORT_RESPONSE"
echo

echo "11. Testando acesso do parceiro aos seus próprios dados..."
PARTNER_OWN_DATA=$(authenticated_request "GET" "$BASE_URL/partners/P-1001" "$PARTNER_TOKEN")
echo "Resposta: $PARTNER_OWN_DATA"
echo

echo "12. Testando tentativa de acesso do parceiro a dados de outro parceiro (deve falhar)..."
PARTNER_OTHER_DATA=$(authenticated_request "GET" "$BASE_URL/partners/P-8888" "$PARTNER_TOKEN")
echo "Resposta: $PARTNER_OTHER_DATA"
echo

echo "=== Testes concluídos ==="
echo
echo "Para testar manualmente, use os seguintes tokens:"
echo "Admin Token: $ADMIN_TOKEN"
echo "Partner Token: $PARTNER_TOKEN"
echo
echo "Exemplo de uso:"
echo "curl -H \"Authorization: Bearer $ADMIN_TOKEN\" $BASE_URL/partners"

