-- Criar tabela de jobs de importação
CREATE TABLE import_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(50) NOT NULL UNIQUE,
    import_type ENUM('PARTNERS', 'CLIENTS') NOT NULL,
    status ENUM('ACCEPTED', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    total_lines INT,
    success_lines INT,
    error_lines INT,
    message VARCHAR(1000),
    error_details TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_import_jobs_job_id ON import_jobs(job_id);
CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_jobs_import_type ON import_jobs(import_type);
CREATE INDEX idx_import_jobs_created_at ON import_jobs(created_at);
