CREATE TABLE commission_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_type ENUM('TYPE_1', 'TYPE_2', 'TYPE_3', 'TYPE_4', 'TYPE_5') NOT NULL UNIQUE,
    fixed_commission DECIMAL(15,2) NOT NULL,
    percentage_commission DECIMAL(5,4),
    min_billing_threshold DECIMAL(15,2),
    max_billing_threshold DECIMAL(15,2),
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_client_type (client_type),
    INDEX idx_status (status)
);

