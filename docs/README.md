# Partnership Service

Sistema para controle e acompanhamento de parceiros, clientes indicados e cálculo de comissões.

## Tecnologias Utilizadas

- **Backend**: Spring Boot 3.5.6, Java 17, Maven
- **Banco de Dados**: MySQL 8.0
- **Segurança**: Spring Security + JWT
- **ORM**: JPA/Hibernate
- **Migrações**: Flyway
- **Validação**: Bean Validation

## Configuração do Ambiente

### Pré-requisitos

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Configuração do Banco de Dados

1. Crie um banco de dados MySQL:
```sql
CREATE DATABASE partnership_db;
```

2. Configure as credenciais no arquivo `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/partnership_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

### Executando a Aplicação

1. Clone o repositório
2. Execute o comando:
```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

## 📚 Documentação da API (Swagger)

A aplicação inclui documentação completa da API via Swagger/OpenAPI:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Como usar o Swagger:
1. Acesse http://localhost:8080/swagger-ui/index.html
2. Clique em "Authorize" (ícone de cadeado)
3. Faça login via `POST /api/auth/login`:
   ```json
   {
     "username": "admin@app.io",
     "password": "admin123"
   }
   ```
4. Copie o token retornado
5. Cole no campo "Value" do Authorization (formato: `Bearer <token>`)
6. Clique em "Authorize"
7. Teste os endpoints protegidos diretamente na interface

## Estrutura da API

### Autenticação

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin@app.io",
  "password": "admin123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin@app.io",
  "role": "ADMIN",
  "partnerId": null
}
```

### Gestão de Parceiros

#### Criar Parceiro (ADMIN)
```http
POST /api/partners
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Contabil Alfa",
  "document": "12.345.678/0001-90",
  "email": "alfa@conta.br",
  "phone": "11999999999"
}
```

#### Listar Parceiros
```http
GET /api/partners
Authorization: Bearer {token}
```

#### Obter Parceiro por ID
```http
GET /api/partners/{partnerId}
Authorization: Bearer {token}
```

#### Atualizar Parceiro
```http
PUT /api/partners/{partnerId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Contabil Alfa Atualizado",
  "document": "12.345.678/0001-90",
  "email": "novo@conta.br",
  "phone": "11999999999"
}
```

#### Inativar Parceiro (ADMIN)
```http
DELETE /api/partners/{partnerId}
Authorization: Bearer {token}
```

### Gestão de Clientes

#### Criar Cliente
```http
POST /api/partners/{partnerId}/clients
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Empresa XYZ",
  "document": "98.765.432/0001-10",
  "email": "contato@empresaxyz.com",
  "phone": "11888888888",
  "clientType": "TYPE_2",
  "monthlyBilling": 5000.00
}
```

#### Listar Clientes do Parceiro
```http
GET /api/partners/{partnerId}/clients
Authorization: Bearer {token}
```

#### Obter Cliente por ID
```http
GET /api/partners/{partnerId}/clients/{clientId}
Authorization: Bearer {token}
```

#### Atualizar Cliente
```http
PUT /api/partners/{partnerId}/clients/{clientId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Empresa XYZ Atualizada",
  "document": "98.765.432/0001-10",
  "email": "novo@empresaxyz.com",
  "phone": "11888888888",
  "clientType": "TYPE_3",
  "monthlyBilling": 8000.00
}
```

#### Inativar Cliente
```http
DELETE /api/partners/{partnerId}/clients/{clientId}
Authorization: Bearer {token}
```

### Regras de Comissionamento (ADMIN)

#### Criar Regra de Comissão
```http
POST /api/commission-rules
Authorization: Bearer {token}
Content-Type: application/json

{
  "clientType": "TYPE_1",
  "fixedCommission": 100.00,
  "percentageCommission": 0.05,
  "minBillingThreshold": 1000.00,
  "maxBillingThreshold": 5000.00
}
```

#### Listar Regras de Comissão
```http
GET /api/commission-rules
Authorization: Bearer {token}
```

#### Obter Regra por Tipo de Cliente
```http
GET /api/commission-rules/client-type/{clientType}
Authorization: Bearer {token}
```

### Cálculo de Comissões

#### Calcular Comissões do Mês
```http
POST /api/commissions/calculate/{year}/{month}
Authorization: Bearer {token}
```

#### Listar Comissões do Parceiro
```http
GET /api/commissions/partner/{partnerId}
Authorization: Bearer {token}
```

#### Listar Comissões do Parceiro por Mês
```http
GET /api/commissions/partner/{partnerId}/{year}/{month}
Authorization: Bearer {token}
```

#### Aprovar Comissão (ADMIN)
```http
PUT /api/commissions/{commissionId}/approve
Authorization: Bearer {token}
```

#### Marcar Comissão como Paga (ADMIN)
```http
PUT /api/commissions/{commissionId}/pay
Authorization: Bearer {token}
```

### Relatórios

#### Relatório de Parceiros por Mês
```http
GET /api/reports/partners/{year}/{month}
Authorization: Bearer {token}
```

#### Relatório de Parceiro Específico
```http
GET /api/reports/partners/{partnerId}/{year}/{month}
Authorization: Bearer {token}
```

## Tipos de Cliente

- **TYPE_1**: Cliente básico
- **TYPE_2**: Cliente intermediário
- **TYPE_3**: Cliente avançado
- **TYPE_4**: Cliente premium
- **TYPE_5**: Cliente enterprise

## Perfis de Usuário

### ADMIN
- Acesso total ao sistema
- Pode gerenciar parceiros, clientes e regras de comissão
- Pode aprovar e marcar comissões como pagas
- Acesso a todos os relatórios

### PARTNER
- Acesso apenas aos seus próprios dados
- Pode gerenciar seus clientes
- Pode visualizar suas comissões e relatórios
- Não pode acessar dados de outros parceiros

## Usuários Padrão

### Administrador
- **Username**: `admin@app.io`
- **Password**: `admin123`
- **Role**: `ADMIN`

### Parceiro
- **Username**: `joao@conta.br`
- **Password**: `admin123`
- **Role**: `PARTNER`
- **Partner ID**: `P-1001`

## Regras de Comissão Padrão

| Tipo | Comissão Fixa | Comissão % | Faturamento Mín. | Faturamento Máx. |
|------|---------------|------------|------------------|------------------|
| TYPE_1 | R$ 100,00 | 5% | R$ 1.000,00 | R$ 5.000,00 |
| TYPE_2 | R$ 150,00 | 7% | R$ 2.000,00 | R$ 10.000,00 |
| TYPE_3 | R$ 200,00 | 10% | R$ 5.000,00 | R$ 20.000,00 |
| TYPE_4 | R$ 300,00 | 12% | R$ 10.000,00 | R$ 50.000,00 |
| TYPE_5 | R$ 500,00 | 15% | R$ 20.000,00 | - |

## Cálculo de Comissões

O sistema calcula comissões baseado em:

1. **Comissão Fixa**: Valor definido por tipo de cliente
2. **Comissão Percentual**: Percentual sobre o faturamento mensal
3. **Bônus**: 2% adicional para clientes com faturamento > R$ 10.000,00
4. **Limites**: Respeita os limites mínimo e máximo de faturamento

**Fórmula**: `Comissão Total = Comissão Fixa + (Faturamento × Percentual) + Bônus`

## Status das Comissões

- **PENDING**: Aguardando aprovação
- **APPROVED**: Aprovada para pagamento
- **PAID**: Paga
- **CANCELLED**: Cancelada

## Validações

- CPF/CNPJ devem ser únicos no sistema
- Emails devem ter formato válido
- Telefones devem ter 10-11 dígitos
- Faturamento deve ser positivo
- Percentuais de comissão devem estar entre 0 e 1

## Segurança

- Autenticação via JWT
- Autorização baseada em roles
- Validação de acesso por parceiro
- Senhas criptografadas com BCrypt

## Monitoramento

A aplicação inclui endpoints do Spring Boot Actuator para monitoramento:

- `/actuator/health` - Status da aplicação
- `/actuator/info` - Informações da aplicação
- `/actuator/metrics` - Métricas da aplicação

## Desenvolvimento

### Estrutura do Projeto

```
partnership-service/
├── docs/                    # Documentação do projeto
│   ├── README.md           # Este arquivo
│   ├── IMPLEMENTATION_SUMMARY.md
│   ├── REPORTS_IMPLEMENTATION.md
│   ├── MONTHLY_CLOSING_IMPLEMENTATION.md
│   ├── PARTNER_DASHBOARD_IMPLEMENTATION.md
│   ├── BONUS_IMPLEMENTATION_SUMMARY.md
│   ├── SWAGGER_IMPLEMENTATION.md
│   ├── ADMIN_ACCESS_FIX.md
│   └── HELP.md
├── scripts/                 # Scripts de automação e teste
│   ├── test-api.sh
│   ├── test-swagger.sh
│   ├── test-monthly-closing.sh
│   ├── test-partner-dashboard.sh
│   ├── test-bonus-features.sh
│   ├── test-new-features.sh
│   └── reset-db.sh
├── examples/                # Arquivos de exemplo
│   ├── partners_example.csv
│   ├── clients_example.csv
│   └── partners_report.csv
├── src/main/java/br/com/ecomercial/partnership/
│   ├── config/          # Configurações (Security, JWT)
│   ├── controller/      # Controllers REST
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # Entidades JPA
│   ├── repository/     # Repositórios JPA
│   └── service/        # Serviços de negócio
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/    # Scripts de migração do Flyway
├── pom.xml
├── Dockerfile
└── docker-compose.yml
```

### Scripts de Teste

O projeto inclui scripts organizados na pasta `scripts/` para facilitar os testes:

```bash
# Testar API completa
./scripts/test-api.sh

# Testar Swagger UI
./scripts/test-swagger.sh

# Testar fechamento mensal
./scripts/test-monthly-closing.sh

# Testar dashboard de parceiros
./scripts/test-partner-dashboard.sh

# Testar funcionalidades de bônus
./scripts/test-bonus-features.sh

# Testar novas funcionalidades
./scripts/test-new-features.sh

# Resetar banco de dados
./scripts/reset-db.sh
```

### Executando Testes

```bash
mvn test
```

### Build da Aplicação

```bash
mvn clean package
```

### Executando com Docker (Futuro)

```bash
docker build -t partnership-service .
docker run -p 8080:8080 partnership-service
```

## Próximos Passos

1. Implementar frontend em React.js
2. Adicionar testes unitários e de integração
3. Implementar cache com Redis
4. Adicionar logs estruturados
5. Implementar notificações por email
6. Adicionar dashboard em tempo real
7. Implementar backup automático
8. Adicionar métricas de performance
