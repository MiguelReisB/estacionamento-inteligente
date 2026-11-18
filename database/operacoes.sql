-- Tabelas de operação do estacionamento (vagas e tickets) — Integrante 4
-- Pode ser incorporado ao schema.sql oficial do Integrante 1.
-- Rodar depois de database/usuarios.sql (que cria o banco projeto_a3).

USE projeto_a3;

-- Vagas separadas por tipo: carro só ocupa vaga CARRO, moto só ocupa vaga MOTO.
CREATE TABLE IF NOT EXISTS vagas (
    id            INT                      NOT NULL AUTO_INCREMENT,
    identificacao VARCHAR(10)              NOT NULL,
    tipo          ENUM('CARRO','MOTO')     NOT NULL,
    status        ENUM('LIVRE','OCUPADA')  NOT NULL DEFAULT 'LIVRE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_vagas_identificacao (identificacao),
    -- índice usado pelo SELECT ... FOR UPDATE: trava só as linhas do tipo/status procurado
    KEY idx_vagas_tipo_status (tipo, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Ciclo de vida do ticket: ATIVO -> PAGO -> ENCERRADO
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
    -- Preenchida só enquanto o ticket não foi encerrado. O UNIQUE garante,
    -- no próprio banco, que a mesma placa não tenha dois tickets abertos.
    placa_aberta    VARCHAR(7) AS (IF(status = 'ENCERRADO', NULL, placa)) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tickets_placa_aberta (placa_aberta),
    KEY idx_tickets_placa (placa),
    KEY idx_tickets_status (status),
    CONSTRAINT fk_tickets_vaga FOREIGN KEY (vaga_id) REFERENCES vagas (id),
    CONSTRAINT ck_tickets_valor CHECK (valor IS NULL OR valor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dados de teste: 10 vagas de carro e 5 de moto
INSERT IGNORE INTO vagas (identificacao, tipo) VALUES
    ('C01','CARRO'),('C02','CARRO'),('C03','CARRO'),('C04','CARRO'),('C05','CARRO'),
    ('C06','CARRO'),('C07','CARRO'),('C08','CARRO'),('C09','CARRO'),('C10','CARRO'),
    ('M01','MOTO'),('M02','MOTO'),('M03','MOTO'),('M04','MOTO'),('M05','MOTO');
