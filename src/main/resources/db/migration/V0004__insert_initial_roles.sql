-- Migration V0004: Inserir roles iniciais
-- Descrição: Insere as roles iniciais no banco de dados
-- Autor: Matheus Cruz

-- INSERT
INSERT INTO roles (name, description, created_at, updated_at) 
VALUES 
    ('ADMIN', 'Administrador do sistema com acesso total', NOW(), NULL),
    ('USER', 'Usuário padrão do sistema', NOW(), NULL)
ON CONFLICT (name) DO NOTHING;