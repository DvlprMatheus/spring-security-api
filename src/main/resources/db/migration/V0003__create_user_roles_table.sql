-- Migration V0003: Criar tabela de relacionamento usuário-role
-- Descrição: Cria a tabela de relacionamento usuário-role com os campos user_id e role_id
-- Autor: Matheus Cruz

-- CREATE
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) 
        REFERENCES roles(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- INDEX
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- COMMENT
COMMENT ON TABLE user_roles IS 'Tabela de relacionamento usuário-role';
COMMENT ON COLUMN user_roles.user_id IS 'ID do usuário';
COMMENT ON COLUMN user_roles.role_id IS 'ID da role';