# Sistema Inteligente de Gestão de Vagas Rotativas | Estacionamento Inteligente

Sistema desktop de gerenciamento de estacionamento desenvolvido para as disciplinas de **Banco de Dados** e **Garantia da Qualidade de Software** (Projeto A3). O projeto contempla controle de fluxo de cancelas, vagas em tempo real, cálculo dinâmico de tarifas, autenticação segura de operadores e tratamento rigoroso de integridade transacional (ACID) e concorrência no banco de dados.

---

## Sumário
- [1. Sobre o Projeto](#1-sobre-o-projeto)
- [2. Funcionalidades](#2-funcionalidades)
- [3. Tecnologias Utilizadas](#3-tecnologias-utilizadas)
- [4. Pré-requisitos](#4-pré-requisitos)
- [5. Instalação](#5-instalação)
- [6. Execução](#7-execução)
- [7. Testes Automatizados](#8-testes-automatizados)
- [8. Estrutura do Projeto](#9-estrutura-do-projeto)
- [9. Observações e Regras Importantes](#10-observações-e-regras-importantes)
- [10. Autores](#11-autores)

---

## 1. Sobre o Projeto
O **Estacionamento Inteligente** tem como finalidade automatizar o controle de pátio em estacionamentos com múltiplas cancelas. O sistema foi projetado para operar com alta confiabilidade mesmo em situações de alta concorrência (ex: duas cancelas liberando a mesma vaga simultaneamente), garantindo que nenhuma inconsistência ou sobrealocação (*overbooking*) ocorra, além de assegurar que falhas operacionais não gerem "veículos fantasmas" no banco de dados.

---

## 2. Funcionalidades
* **Autenticação e Cadastro de Operadores:** Cadastro seguro com criptografia de senha via hash BCrypt e controle de acesso via login.
* **Monitoramento de Vagas em Tempo Real:** Visualização dinâmica do quantitativo de vagas livres e ocupadas, segmentadas por tipo de veículo (Carro e Moto).
* **Entrada de Veículos:**
  * Validação de placas (padrão tradicional `ABC1234` e padrão Mercosul `ABC1D23`).
  * Alocação automática da primeira vaga livre do respectivo tipo com trava pessimista.
  * Emissão de ticket atrelado à cancela de entrada.
  * Bloqueio imediato caso o pátio atinja lotação esgotada.
* **Consulta e Pagamento:**
  * Consulta do ticket pela placa e cálculo automatizado da estadia proporcional com fração de hora.
  * Transição de estado de `ATIVO` para `PAGO`.
* **Saída de Veículos:**
  * Bloqueio rígido de cancela para veículos que ainda não efetuaram o pagamento.
  * Encerramento do ticket (`PAGO` $\rightarrow$ `ENCERRADO`) e devolução imediata da vaga para o status `LIVRE`.

---

## 3. Tecnologias Utilizadas
* **Linguagem:** Java 21 (LTS)
* **Interface Gráfica:** Java Swing
* **Persistência / Conexão:** JDBC (Java Database Connectivity) e MySQL Connector/J
* **Banco de Dados:** MySQL 8.x / 9.x (Motor InnoDB para suporte a transações ACID e Locking)
* **Segurança:** jBCrypt (Hashing seguro de senhas com Salt)
* **Gerenciador de Dependências e Build:** Apache Maven
* **Testes Automatizados:** JUnit 5 (Jupiter) e Maven Surefire Plugin
* **Controle de Versão:** Git / GitHub

---

## 4. Pré-requisitos
Antes de iniciar, certifique-se de possuir instalado em sua máquina:
* [JDK 21](https://www.oracle.com/java/technologies/downloads/#java21 ) ou superior;
* [MySQL Server](https://dev.mysql.com/downloads/mysql/ ) (versão 8.0 ou superior);
* [Apache Maven](https://maven.apache.org/ ) (opcional se executado via NetBeans/IDE);
* [Git](https://git-scm.com/ ).

---

## 5. Instalação

1. Clone o repositório em seu ambiente local:
```bash
git clone https://github.com/MiguelReisB/estacionamento-inteligente.git
cd estacionamento-inteligente
```

2. Abra o projeto em sua IDE Java de preferência (NetBeans, IntelliJ IDEA, VS Code ou Eclipse).

---

---

## 6. Execução

### Opção 1: Via NetBeans / IDE
1. Abra o módulo `desktop` na IDE.
2. Aguarde o download das dependências do Maven.
3. Clique com o botão direito no projeto `desktop` $\rightarrow$ **Run** (ou execute a classe principal `com.mycompany.mavenproject1.Mavenproject1`).

### Opção 2: Via Linha de Comando (Maven)
No diretório raiz do repositório:
```bash
# Compilar os módulos
mvn clean install -DskipTests

# Executar a aplicação Desktop
cd desktop
mvn exec:java
```

---

## 7. Testes Automatizados

O projeto conta com uma suíte de testes unitários e de integração utilizando **JUnit 5**, cobrindo o fluxo operacional e os requisitos obrigatórios de Banco de Dados.

Para executar todos os testes automatizados via terminal:
```bash
cd desktop
mvn test
```

### Principais Cenários Cobertos:
* **Concorrência e Lock Pessimista (`ConcorrenciaTransacoesTest`):** Simula duas cancelas (threads concorrentes via `CountDownLatch`) disputando a última vaga livre no mesmo milissegundo. Valida que o `SELECT ... FOR UPDATE` garante a exclusão mútua sem causar sobrealocação.
* **Atomicidade e ROLLBACK (`ConcorrenciaTransacoesTest`):** Simula uma falha forçada no meio da transação de entrada, garantindo que o `ROLLBACK` desfaça inserções e impeça "veículos fantasmas" no banco.
* **COMMIT com Sucesso (`ConcorrenciaTransacoesTest`):** Valida a amarração transacional completa de entrada e ocupação da vaga.
* **Ciclo de Saída e Pagamento (`SaidaVeiculoServiceTest`):** Testa bloqueio de saída de inadimplentes, pagamento e liberação da vaga.
* **Cálculo Tarifário (`CalculadoraTarifaTest`):** Testa regras de cobrança mínima e frações de hora.
* **Segurança e Acesso (`UsuarioDAOTest`):** Valida hashing BCrypt, bloqueio de e-mails duplicados e autenticação.

---

## 8. Estrutura do Projeto

```text
estacionamento-inteligente/
├── database/                   # Scripts DDL e DML de inicialização do banco
│   ├── usuarios.sql            # Criação da base e tabela de usuários
│   └── operacoes.sql           # Tabelas de vagas, tickets e restrições
├── docs/                       # Documentação técnica, requisitos e diagramas DER
│   ├── modeloConceitualBD.png
│   ├── modeloLogicoFisicoBD.png
│   ├── RequisitosFuncionais.md
│   └── RequisitosNaoFuncionais.md
├── desktop/                    # Módulo da Aplicação Desktop (Java Swing)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── com/mycompany/mavenproject1/  # Telas e UI Swing
│   │   │   │   ├── dao/                          # Data Access Objects (JDBC)
│   │   │   │   ├── model/                        # Entidades e Enums de domínio
│   │   │   │   ├── service/                      # Regras de negócio e transações
│   │   │   │   └── util/                         # Conexão, BCrypt e validações
│   │   │   └── resources/                        # Imagens e db.properties
│   │   └── test/java/                            # Testes automatizados (JUnit 5)
│   │       ├── dao/                              # Testes de persistência e segurança
│   │       └── service/                          # Testes de concorrência e negócio
│   └── pom.xml                                   # Dependências do módulo Desktop
└── pom.xml                                       # POM raiz agregador
```

---

## 9. Observações e Regras Importantes
* **Mecanismo Anti-Colisão no Banco:** A tabela `tickets` conta com a coluna gerada `placa_aberta` com restrição `UNIQUE`. Isso garante a nível de integridade física no MySQL que nenhum veículo possa ter mais de um ticket com status `ATIVO` ou `PAGO` ao mesmo tempo.
* **Lock Pessimista:** Utiliza `SELECT ... FOR UPDATE` sobre as linhas da tabela `vagas`, o que bloqueia o registro durante a transação e previne inconsistências por concorrência (*Race Conditions*).
* **Restituição do Ambiente de Teste:** As classes de teste contam com rotinas `@BeforeEach` e `@AfterAll` para garantir que a base de dados não permaneça corrompida ou bloqueada após a execução das baterias de testes.

---

## 10. Autores
Trabalho desenvolvido por:

* **Bruno Carvalho Vieira** - RA: 192510091
* **Henrique Antunes Nogueira** - RA: 192512318í
* **Laryssa Antonieta Souza Vaz** - RA: 192513729
* **Luís Felipe Silva Dutra** - RA: 192512431
* **Miguel Ribeiro Borges Reis** - RA: 192613005
