-- tabela de usuarios (login) para instalacao nova
-- (num banco que ja tem a tabela, use o migracao_perfil_usuarios.sql)

CREATE DATABASE IF NOT EXISTS projeto_a3
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE projeto_a3;

CREATE TABLE IF NOT EXISTS usuarios (
    id     INT          NOT NULL AUTO_INCREMENT,
    nome   VARCHAR(100) NOT NULL,
    email  VARCHAR(150) NOT NULL,
    senha  VARCHAR(60)  NOT NULL, -- hash do bcrypt
    perfil VARCHAR(10)  NOT NULL DEFAULT 'OPERADOR', -- ADMIN ou OPERADOR
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email),
    CONSTRAINT ck_usuarios_perfil CHECK (perfil IN ('ADMIN', 'OPERADOR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- primeiro administrador: admin@estacionamento.com / admin123 (troque depois)
INSERT IGNORE INTO usuarios (nome, email, senha, perfil)
VALUES ('Administrador', 'admin@estacionamento.com',
        '$2a$12$m3IZ5yPv2Sszy9v3VB572.zbXizIBZ2SXoDJEmRmOY1N7BW4FJzdS', 'ADMIN');
