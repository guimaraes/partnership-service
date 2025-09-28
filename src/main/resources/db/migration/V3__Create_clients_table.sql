CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    client_type ENUM('TYPE_1', 'TYPE_2', 'TYPE_3', 'TYPE_4', 'TYPE_5') NOT NULL,
    monthly_billing DECIMAL(15,2),
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    partner_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_document (document),
    INDEX idx_email (email),
    INDEX idx_partner_id (partner_id),
    INDEX idx_client_type (client_type),
    INDEX idx_status (status),
    FOREIGN KEY (partner_id) REFERENCES partners(partner_id) ON DELETE CASCADE
);

