-- Migration V0004: Inserir roles iniciais
-- Descrição: Insere as roles iniciais no banco de dados
-- Autor: DvlprMatheus

-- INSERT
INSERT INTO roles (id, name, description, created_at, updated_at) 
VALUES 
    (gen_random_uuid(), 'ADMIN', 'Administrador do sistema com acesso total', NOW(), NULL),
    (gen_random_uuid(), 'USER', 'Usuário padrão do sistema', NOW(), NULL)
ON CONFLICT (name) DO NOTHING;