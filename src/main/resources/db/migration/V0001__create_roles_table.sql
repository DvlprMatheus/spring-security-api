-- Migration V0001: Criar tabela de roles
-- Descrição: Cria a tabela de roles com os campos name, description, created_at e updated_at
-- Autor: Matheus Cruz

-- CREATE
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- INDEX
CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

-- COMMENT
COMMENT ON TABLE roles IS 'Tabela de roles';
COMMENT ON COLUMN roles.name IS 'Nome da role';
COMMENT ON COLUMN roles.description IS 'Descrição da role';
COMMENT ON COLUMN roles.created_at IS 'Data de criação da role';
COMMENT ON COLUMN roles.updated_at IS 'Data de atualização da role';