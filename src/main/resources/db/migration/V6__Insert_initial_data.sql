-- Insert initial admin user
INSERT INTO users (username, password, role, partner_id, is_active) 
VALUES ('admin@app.io', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ADMIN', NULL, TRUE);

-- Insert initial partner user
INSERT INTO users (username, password, role, partner_id, is_active) 
VALUES ('joao@conta.br', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'PARTNER', 'P-1001', TRUE);

-- Insert initial partner
INSERT INTO partners (partner_id, name, document, email, phone, status) 
VALUES ('P-1001', 'Contabil Alfa', '12.345.678/0001-90', 'alfa@conta.br', '11999999999', 'ACTIVE');

-- Insert commission rules
INSERT INTO commission_rules (client_type, fixed_commission, percentage_commission, min_billing_threshold, max_billing_threshold, status) VALUES
('TYPE_1', 100.00, 0.05, 1000.00, 5000.00, 'ACTIVE'),
('TYPE_2', 150.00, 0.07, 2000.00, 10000.00, 'ACTIVE'),
('TYPE_3', 200.00, 0.10, 5000.00, 20000.00, 'ACTIVE'),
('TYPE_4', 300.00, 0.12, 10000.00, 50000.00, 'ACTIVE'),
('TYPE_5', 500.00, 0.15, 20000.00, NULL, 'ACTIVE');

