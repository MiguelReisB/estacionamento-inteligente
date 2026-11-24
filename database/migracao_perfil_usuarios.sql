-- perfis de usuario (ADMIN / OPERADOR) e senha com hash
-- rodar UMA vez no banco que a API usa (veja spring.datasource.url em
-- api/src/main/resources/application.properties, hoje o loca_veiculos)
-- ex.: USE loca_veiculos;

-- 1) a senha passa a guardar o hash do bcrypt (60 caracteres)
--    contas antigas continuam entrando: no primeiro login a API troca a senha pelo hash
ALTER TABLE usuarios MODIFY senha VARCHAR(60) NOT NULL;

-- 2) perfil do usuario. todo cadastro novo entra como OPERADOR
ALTER TABLE usuarios
    ADD COLUMN perfil VARCHAR(10) NOT NULL DEFAULT 'OPERADOR',
    ADD CONSTRAINT ck_usuarios_perfil CHECK (perfil IN ('ADMIN', 'OPERADOR'));

-- 3) primeiro administrador do sistema
--    login: admin@estacionamento.com / senha: admin123 (troque depois da apresentacao)
INSERT INTO usuarios (nome, email, senha, perfil)
VALUES ('Administrador', 'admin@estacionamento.com',
        '$2a$12$m3IZ5yPv2Sszy9v3VB572.zbXizIBZ2SXoDJEmRmOY1N7BW4FJzdS', 'ADMIN')
ON DUPLICATE KEY UPDATE perfil = 'ADMIN';

-- se preferir promover uma conta que ja existe:
-- UPDATE usuarios SET perfil = 'ADMIN' WHERE email = 'seu@email.com';
