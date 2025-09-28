-- Adicionar campos de vigência às regras de comissão
ALTER TABLE commission_rules 
ADD COLUMN effective_from DATE NOT NULL DEFAULT '2025-01-01';

ALTER TABLE commission_rules 
ADD COLUMN effective_to DATE NULL;

-- Remover constraint de unicidade do client_type se existir
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE table_schema = DATABASE() 
     AND table_name = 'commission_rules' 
     AND index_name = 'UK_commission_rules_client_type') > 0,
    'ALTER TABLE commission_rules DROP INDEX UK_commission_rules_client_type',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Adicionar índices para otimizar consultas por vigência
CREATE INDEX idx_commission_rules_client_type_effective 
ON commission_rules (client_type, effective_from, effective_to);

-- Adicionar campo de data efetiva do status para clientes
ALTER TABLE clients 
ADD COLUMN status_effective_from DATE NULL;
