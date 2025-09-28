-- Criar tabela de fechamentos mensais
CREATE TABLE monthly_closings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_month VARCHAR(7) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    total_partners INT DEFAULT 0,
    total_clients INT DEFAULT 0,
    total_commission DECIMAL(15,2) DEFAULT 0.00,
    total_bonus DECIMAL(15,2) DEFAULT 0.00,
    total_payout DECIMAL(15,2) DEFAULT 0.00,
    justification VARCHAR(1000),
    closed_by VARCHAR(255),
    reopened_by VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Criar tabela de detalhes do fechamento
CREATE TABLE closing_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monthly_closing_id BIGINT NOT NULL,
    partner_id VARCHAR(20) NOT NULL,
    client_id BIGINT NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    client_type VARCHAR(50) NOT NULL,
    client_status VARCHAR(50) NOT NULL,
    client_active_from DATE,
    client_inactive_from DATE,
    monthly_revenue DECIMAL(15,2),
    rule_type VARCHAR(50) NOT NULL,
    commission_rate DECIMAL(10,4),
    commission_value DECIMAL(15,2) NOT NULL,
    bonus_value DECIMAL(15,2) DEFAULT 0.00,
    total_value DECIMAL(15,2) NOT NULL,
    observations VARCHAR(500),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (monthly_closing_id) REFERENCES monthly_closings(id) ON DELETE CASCADE,
    FOREIGN KEY (partner_id) REFERENCES partners(partner_id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Adicionar índices para otimizar consultas
CREATE INDEX idx_monthly_closings_reference_month ON monthly_closings (reference_month);
CREATE INDEX idx_monthly_closings_status ON monthly_closings (status);
CREATE INDEX idx_closing_details_monthly_closing_id ON closing_details (monthly_closing_id);
CREATE INDEX idx_closing_details_partner_id ON closing_details (partner_id);
CREATE INDEX idx_closing_details_client_id ON closing_details (client_id);

-- Adicionar constraint para validar status
ALTER TABLE monthly_closings 
ADD CONSTRAINT chk_monthly_closings_status 
CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'REOPENED', 'CANCELLED'));

-- Adicionar constraint para validar formato do mês de referência
ALTER TABLE monthly_closings 
ADD CONSTRAINT chk_reference_month_format 
CHECK (reference_month REGEXP '^[0-9]{4}-[0-9]{2}$');

