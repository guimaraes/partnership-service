# 📚 Índice da Documentação

Bem-vindo à documentação completa do **Partnership Service**! Aqui você encontrará todos os guias e informações necessárias para entender, configurar e usar o sistema.

## 🚀 Início Rápido

- **[README.md](README.md)** - Documentação principal e guia de início
- **[HELP.md](HELP.md)** - Guia de ajuda e troubleshooting

## 📋 Implementações e Funcionalidades

### Funcionalidades Principais
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Resumo geral das implementações
- **[REPORTS_IMPLEMENTATION.md](REPORTS_IMPLEMENTATION.md)** - Sistema de relatórios e exportações
- **[MONTHLY_CLOSING_IMPLEMENTATION.md](MONTHLY_CLOSING_IMPLEMENTATION.md)** - Fechamentos mensais
- **[PARTNER_DASHBOARD_IMPLEMENTATION.md](PARTNER_DASHBOARD_IMPLEMENTATION.md)** - Dashboard de parceiros

### Funcionalidades Avançadas
- **[BONUS_IMPLEMENTATION_SUMMARY.md](BONUS_IMPLEMENTATION_SUMMARY.md)** - Sistema de bônus
- **[SWAGGER_IMPLEMENTATION.md](SWAGGER_IMPLEMENTATION.md)** - Documentação da API
- **[ADMIN_ACCESS_FIX.md](ADMIN_ACCESS_FIX.md)** - Correções de acesso administrativo

## 🎯 Por Tipo de Usuário

### 👨‍💼 Administradores
- Configuração do sistema
- Gestão de usuários e parceiros
- Aprovação de comissões
- Acesso a todos os relatórios
- Configuração de regras de comissão

### 🤝 Parceiros
- Gestão de clientes
- Visualização de comissões
- Relatórios pessoais
- Dashboard de performance

### 👨‍💻 Desenvolvedores
- Estrutura da API
- Configuração do ambiente
- Scripts de teste
- Documentação técnica

## 🔧 Configuração e Deploy

### Ambiente de Desenvolvimento
1. Configurar banco de dados MySQL
2. Executar `mvn spring-boot:run`
3. Acessar http://localhost:8080

### Ambiente de Produção
1. Configurar variáveis de ambiente
2. Build com `mvn clean package`
3. Deploy com Docker ou JAR

## 🧪 Testes e Validação

### Scripts Disponíveis
- `test-api.sh` - Testes completos da API
- `test-swagger.sh` - Validação do Swagger UI
- `test-monthly-closing.sh` - Testes de fechamento
- `test-partner-dashboard.sh` - Testes do dashboard
- `test-bonus-features.sh` - Testes de bônus
- `test-new-features.sh` - Testes de novas funcionalidades

### Como Executar
```bash
# Na raiz do projeto
./scripts/test-api.sh
```

## 📊 Monitoramento

### Health Checks
- **Aplicação**: http://localhost:8080/actuator/health
- **Métricas**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

### Logs
- Verificar logs da aplicação para debugging
- Usar níveis de log apropriados (DEBUG, INFO, WARN, ERROR)

## 🔐 Segurança

### Autenticação
- JWT tokens
- Expiração configurável
- Refresh automático

### Autorização
- Roles: ADMIN, PARTNER
- Controle de acesso por endpoint
- Validação de propriedade de dados

## 📈 Métricas e Observabilidade

### Métricas Disponíveis
- Execuções de fechamento mensal
- Importações de dados
- Criação de entidades
- Logs de auditoria
- Performance da aplicação

### Dashboards
- Health status
- Métricas customizadas
- Relatórios de uso

## 🆘 Suporte

### Problemas Comuns
1. **Erro de conexão com banco**: Verificar credenciais e status do MySQL
2. **Token expirado**: Fazer novo login
3. **Erro 403**: Verificar permissões do usuário
4. **Erro 500**: Verificar logs da aplicação

### Contato
- Documentação: Esta pasta `docs/`
- Issues: Sistema de issues do repositório
- Logs: Verificar logs da aplicação

---

**Última atualização**: $(date)
**Versão**: 1.0.0
**Status**: ✅ Produção
