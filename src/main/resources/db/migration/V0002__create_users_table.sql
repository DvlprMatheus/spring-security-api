-- Migration V0002: Criar tabela de usuários
-- Descrição: Cria a tabela de usuários com os campos username, email, password, created_at e updated_at
-- Autor: Matheus Cruz

-- CREATE
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- INDEX
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_email_lookup ON users(email);

-- COMMENT
COMMENT ON TABLE users IS 'Tabela de usuários';
COMMENT ON COLUMN users.username IS 'Nome de usuário';
COMMENT ON COLUMN users.email IS 'Email do usuário';
COMMENT ON COLUMN users.password IS 'Senha do usuário';
COMMENT ON COLUMN users.created_at IS 'Data de criação do usuário';
COMMENT ON COLUMN users.updated_at IS 'Data de atualização do usuário';