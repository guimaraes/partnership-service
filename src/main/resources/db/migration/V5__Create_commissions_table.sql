CREATE TABLE commissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_id VARCHAR(20) NOT NULL,
    client_id BIGINT NOT NULL,
    reference_month DATE NOT NULL,
    client_billing DECIMAL(15,2) NOT NULL,
    commission_value DECIMAL(15,2) NOT NULL,
    bonus_value DECIMAL(15,2),
    total_value DECIMAL(15,2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_partner_id (partner_id),
    INDEX idx_client_id (client_id),
    INDEX idx_reference_month (reference_month),
    INDEX idx_status (status),
    UNIQUE KEY uk_partner_client_month (partner_id, client_id, reference_month),
    FOREIGN KEY (partner_id) REFERENCES partners(partner_id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

