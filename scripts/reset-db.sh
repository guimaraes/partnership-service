#!/bin/bash

echo "=== Resetando banco de dados ==="
echo ""

# Verificar se o Docker está rodando
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker primeiro."
    exit 1
fi

# Parar e remover container se existir
echo "1. Parando container MySQL..."
docker stop partnership-mysql 2>/dev/null || true
docker rm partnership-mysql 2>/dev/null || true

# Remover volume se existir
echo "2. Removendo volume MySQL..."
docker volume rm partnership-service_mysql_data 2>/dev/null || true

# Iniciar novo container
echo "3. Iniciando novo container MySQL..."
docker run -d \
  --name partnership-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=partnership_db \
  -p 3306:3306 \
  mysql:8.0

# Aguardar MySQL inicializar
echo "4. Aguardando MySQL inicializar..."
sleep 30

# Verificar se MySQL está rodando
echo "5. Verificando conexão com MySQL..."
max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if docker exec partnership-mysql mysqladmin ping -h localhost --silent; then
        echo "✅ MySQL está rodando!"
        break
    fi
    
    echo "⏳ Tentativa $attempt/$max_attempts - Aguardando MySQL..."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo "❌ Falha ao conectar com MySQL após $max_attempts tentativas"
    exit 1
fi

echo ""
echo "=== Banco de dados resetado com sucesso! ==="
echo "✅ Container: partnership-mysql"
echo "✅ Database: partnership_db"
echo "✅ Usuário: root"
echo "✅ Senha: root"
echo "✅ Porta: 3306"
echo ""
echo "Agora você pode executar: mvn spring-boot:run"

