-- Criar tabela de histórico de tipos de cliente
CREATE TABLE client_type_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    client_type VARCHAR(20) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_client_type_history_client_id (client_id),
    INDEX idx_client_type_history_effective_period (effective_from, effective_to),
    
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

