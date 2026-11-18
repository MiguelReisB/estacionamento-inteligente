-- tabelas de vagas e tickets
-- rodar depois do usuarios.sql (que cria o banco)

USE projeto_a3;

CREATE TABLE IF NOT EXISTS vagas (
    id            INT                      NOT NULL AUTO_INCREMENT,
    identificacao VARCHAR(10)              NOT NULL,
    tipo          ENUM('CARRO','MOTO')     NOT NULL,
    status        ENUM('LIVRE','OCUPADA')  NOT NULL DEFAULT 'LIVRE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_vagas_identificacao (identificacao),
    KEY idx_vagas_tipo_status (tipo, status) -- usado no select for update da entrada
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- status do ticket: ATIVO -> PAGO -> ENCERRADO
CREATE TABLE IF NOT EXISTS tickets (
    id              INT                                NOT NULL AUTO_INCREMENT,
    placa           VARCHAR(7)                         NOT NULL,
    tipo_veiculo    ENUM('CARRO','MOTO')               NOT NULL,
    vaga_id         INT                                NOT NULL,
    data_entrada    DATETIME                           NOT NULL,
    data_pagamento  DATETIME                           NULL,
    data_saida      DATETIME                           NULL,
    cancela_entrada VARCHAR(30)                        NOT NULL,
    valor           DECIMAL(8,2)                       NULL,
    status          ENUM('ATIVO','PAGO','ENCERRADO')   NOT NULL DEFAULT 'ATIVO',
    -- fica com a placa enquanto o ticket nao foi encerrado, com o unique
    -- o banco nao deixa a mesma placa ter 2 tickets abertos
    placa_aberta    VARCHAR(7) AS (IF(status = 'ENCERRADO', NULL, placa)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tickets_placa_aberta (placa_aberta),
    KEY idx_tickets_placa (placa),
    KEY idx_tickets_status (status),
    CONSTRAINT fk_tickets_vaga FOREIGN KEY (vaga_id) REFERENCES vagas (id),
    CONSTRAINT ck_tickets_valor CHECK (valor IS NULL OR valor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- vagas pra teste: 10 de carro e 5 de moto
INSERT IGNORE INTO vagas (identificacao, tipo) VALUES
    ('C01','CARRO'),('C02','CARRO'),('C03','CARRO'),('C04','CARRO'),('C05','CARRO'),
    ('C06','CARRO'),('C07','CARRO'),('C08','CARRO'),('C09','CARRO'),('C10','CARRO'),
    ('M01','MOTO'),('M02','MOTO'),('M03','MOTO'),('M04','MOTO'),('M05','MOTO');
