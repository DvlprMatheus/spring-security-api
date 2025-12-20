-- Criar tabela roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Criar índice único para name (já coberto pelo UNIQUE, mas útil para performance)
CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

-- Inserir roles iniciais
INSERT INTO roles (name, description, created_at, updated_at) 
VALUES 
    ('ADMIN', 'Administrador do sistema com acesso total', NOW(), NOW()),
    ('USER', 'Usuário padrão do sistema', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

