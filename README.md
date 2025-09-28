# Partnership Service

Sistema para controle e acompanhamento de parceiros, clientes indicados e cálculo de comissões.

## 📁 Organização do Projeto

Este projeto está organizado em pastas específicas para melhor manutenção:

- **📚 [docs/](docs/)** - Toda a documentação do projeto
- **🔧 [scripts/](scripts/)** - Scripts de automação e teste
- **📄 [examples/](examples/)** - Arquivos de exemplo (CSV, etc.)

## 🚀 Início Rápido

### 1. Configuração do Ambiente

```bash
# Pré-requisitos
- Java 17+
- Maven 3.6+
- MySQL 8.0+

# Criar banco de dados
mysql -u root -p
CREATE DATABASE partnership_db;
```

### 2. Executar a Aplicação

```bash
# Executar o servidor
mvn spring-boot:run

# A aplicação estará disponível em:
# http://localhost:8080
```

### 3. Acessar a Documentação

- **📖 Documentação Completa**: [docs/README.md](docs/README.md)
- **🔍 Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **📊 Health Check**: http://localhost:8080/actuator/health

## 🧪 Scripts de Teste

```bash
# Testar API completa
./scripts/test-api.sh

# Testar Swagger UI
./scripts/test-swagger.sh

# Ver todos os scripts disponíveis
ls scripts/
```

## 📋 Funcionalidades Principais

- ✅ **Gestão de Parceiros** - CRUD completo
- ✅ **Gestão de Clientes** - Por parceiro
- ✅ **Cálculo de Comissões** - Automático por tipo
- ✅ **Relatórios** - CSV e PDF
- ✅ **Importação em Massa** - CSV assíncrono
- ✅ **Auditoria** - Trilhas de mudanças
- ✅ **Validações** - Domínio e negócio
- ✅ **Monitoramento** - Health e métricas
- ✅ **Segurança** - JWT + Roles
- ✅ **Documentação** - Swagger/OpenAPI

## 🔐 Usuários Padrão

| Usuário | Senha | Role | Descrição |
|---------|-------|------|-----------|
| `admin@app.io` | `admin123` | ADMIN | Acesso total |
| `joao@conta.br` | `admin123` | PARTNER | Parceiro P-1001 |

## 📚 Documentação Detalhada

Para informações completas sobre:
- Configuração detalhada
- Estrutura da API
- Exemplos de uso
- Regras de negócio
- Troubleshooting

👉 **Acesse**: [docs/README.md](docs/README.md)

## 🛠️ Desenvolvimento

```bash
# Estrutura do projeto
partnership-service/
├── docs/           # Documentação
├── scripts/        # Scripts de teste
├── examples/       # Arquivos de exemplo
├── src/           # Código fonte
├── pom.xml        # Dependências Maven
└── Dockerfile     # Container Docker
```

## 📞 Suporte

- **Documentação**: [docs/](docs/)
- **Issues**: Use o sistema de issues do repositório
- **Logs**: Verifique os logs da aplicação para debugging

---

**Desenvolvido com ❤️ usando Spring Boot 3.5.6 + Java 17**
