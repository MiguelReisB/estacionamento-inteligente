-- tabela de usuarios (login)

CREATE DATABASE IF NOT EXISTS projeto_a3
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE projeto_a3;

CREATE TABLE IF NOT EXISTS usuarios (
    id    INT          NOT NULL AUTO_INCREMENT,
    nome  VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    senha CHAR(60)     NOT NULL, -- hash do bcrypt
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
