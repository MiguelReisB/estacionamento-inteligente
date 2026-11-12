# STATUS DO PROJETO — Estacionamento Inteligente

> Sistema Inteligente de Gestão de Vagas Rotativas
> Disciplinas: Banco de Dados | Garantia da Qualidade de Software
> **Última atualização:** 04/11/2026
> Legenda: ✅ feito · 🟡 parcial / com problemas · ❌ não iniciado · ⚠️ bug ou risco

---

## 1. Resumo rápido (leia isto primeiro)

| Área | Situação | Comentário |
|---|---|---|
| Banco de dados (modelagem) | ❌ / 🟡 | Só existe a tabela `usuarios` no dump. **Não existem as tabelas `vagas` e `tickets`**, que são o coração do projeto. |
| Autenticação | 🟡 | Login e cadastro funcionam contra o MySQL, mas a senha é salva em **texto puro** e o cadastro tem **SQL Injection**. |
| Conexão JDBC | 🟡 | `FabricaConexao` (MySQL) funciona, mas convive com uma segunda conexão `BancoDados` (H2) que não deveria existir. |
| Interface gráfica | 🟡 | Telas de Login, Cadastro, Principal e Entrada desenhadas. **Nenhum botão da Tela Principal e da Tela de Entrada faz nada ainda.** Falta tela de Saída/Pagamento. |
| Operações (entrada/saída/pagamento) | ❌ | Não há cadastro de vagas, consulta, saída, pagamento nem liberação de vaga. |
| Transação + Lock (requisito central) | 🟡 | `EntradaVeiculoService` já tem `SELECT ... FOR UPDATE`, `COMMIT` e `ROLLBACK`, mas **não está ligado a nenhuma tela** e depende de tabelas que não existem. |
| Testes de concorrência | ❌ | Nenhum teste de duas cancelas simultâneas. |
| Documentação / relatório | ❌ | Pastas `docs/` e `database/` vazias. README só tem a lista de integrantes. |

**Fluxo atual que funciona de ponta a ponta:** abrir o sistema → cadastrar usuário → fazer login → ⚠️ *o sistema provavelmente quebra ao abrir a Tela Principal* (ver bug B1).

---

## 2. Estrutura atual do código

```
estacionamento-inteligente/
├── pom.xml                         Maven, Java 26, mysql-connector-j 9.5.0, H2 2.4.240, AbsoluteLayout
├── projeto_a3_usuarios.sql         Dump MySQL — só a tabela `usuarios`
├── README.md                       Só lista de integrantes (5 nomes)
├── database/  (vazia)              ← deveria ter o script SQL oficial
├── docs/      (vazia)              ← deveria ter MER, relatório, casos de teste
├── mavenproject1/                  ⚠️ PASTA SOBRANDO de um projeto antigo
│   ├── dados/estacionamento.mv.db     banco H2 antigo
│   └── src/main/resources/img/        carro.png, moto.png, vagas.png  ← imagens no lugar errado
└── src/main/java/
    ├── model/       Usuario, Vaga, Ticket            (POJOs, sem uso real ainda)
    ├── service/     EntradaVeiculoService           (transação + lock, não usado)
    ├── util/        FabricaConexao                  (conexão MySQL)
    └── com/mycompany/mavenproject1/
        ├── Mavenproject1.java     main → inicializa H2 e abre Tela_Login
        ├── BancoDados.java        conexão H2 (legado, conflita com FabricaConexao)
        ├── Tela_Login             ✅ funcional
        ├── Tela_Cadastro          🟡 funcional, mas inseguro
        ├── Tela_Principal         🟡 só visual
        └── Tela_Entrada           🟡 só visual (não é aberta por nenhuma tela)
```

---

## 3. Bugs e riscos encontrados no código (prioridade alta)

| # | Problema | Onde | Impacto | Responsável sugerido |
|---|---|---|---|---|
| **B1** | ⚠️ As imagens estão em `mavenproject1/src/main/resources/img/`, mas o Maven procura em `src/main/resources/img/` (que não existe). Além disso o código pede `"/img/carro png.png"` e o arquivo se chama `carro.png`. `getResource` retorna `null` → `NullPointerException`. | `Tela_Principal`, `Tela_Entrada` | **O sistema quebra logo depois do login.** | Integrante 3 |
| **B2** | ⚠️ SQL Injection: o cadastro monta o SQL concatenando texto (`"... VALUES('" + nome + "', ..."`). | `Tela_Cadastro.btnAvancarMouseClicked` | Falha de segurança; professor vai notar. | Integrante 2 |
| **B3** | ⚠️ Senhas em texto puro (requisito exige hash). A coluna `senha VARCHAR(8)` nem comporta um hash (BCrypt = 60 chars, SHA-256 hex = 64). | `Tela_Cadastro`, `Tela_Login`, SQL | Descumpre requisito explícito. | Integrantes 1 e 2 |
| **B4** | ⚠️ Dois bancos diferentes: `main` chama `BancoDados.inicializar()` (cria tabela no **H2** com coluna `email`), mas login/cadastro usam **MySQL** via `FabricaConexao` (sem `email`). `BancoDados.validarLogin` nunca é usado. | `Mavenproject1`, `BancoDados` | Confusão e código morto; o H2 não atende ao requisito de InnoDB/MySQL. | Integrante 2 |
| **B5** | ⚠️ Usuário, senha e porta do MySQL fixos no código e versionados (`remoto` / porta `5014` / banco `teste`). O dump usa o banco `projeto_a3`. | `FabricaConexao` | Cada integrante precisa editar o código para rodar; credencial exposta no Git; nomes de banco inconsistentes. | Integrante 2 |
| **B6** | `EntradaVeiculoService`: no `catch (SQLException)` não há `rollback()` explícito (o fechamento da conexão desfaz, mas para a apresentação o ROLLBACK precisa estar visível). Mensagens de log sem espaços (`"autorizada" + placa`). | `EntradaVeiculoService` | Evidência fraca do requisito de rollback. | Integrante 5 |
| **B7** | Nomes de campos inconsistentes entre código e modelos: service usa `placa`, `data_entrada`, `cancela_entrada`, `status`; `Ticket.java` usa `placaVeiculo`, `horarioEntrada` e não tem `cancela`/`status`/`saída`/`valor`. | `model/*`, `service/*` | Vai dar retrabalho quando criarem os DAOs. | Integrantes 1 e 4 |
| **B8** | Botões usam `mouseClicked` em vez de `actionPerformed` (Enter/teclado não funcionam). Senha lida com `getText()` (deprecated) em vez de `getPassword()`. Cadastro com sucesso não mostra mensagem. | Telas | Usabilidade / boas práticas. | Integrante 3 |
| **B9** | `pom.xml` exige **Java 26** (`release 26`) — todo mundo precisa do mesmo JDK, inclusive no PC da apresentação. Dependência `AbsoluteLayout` parece não ser usada. | `pom.xml` | Pode não compilar na máquina de alguém. | Integrante 6 |
| **B10** | README lista 5 integrantes, a divisão de tarefas tem 6. | `README.md` | Conferir. | Integrante 6 |

---

## 4. Progresso por integrante

### Integrante 1 — Banco de dados e modelagem
- [ ] Modelo conceitual
- [ ] Modelo lógico
- [ ] Modelo físico
- [ ] Tabela `usuarios` revisada (coluna de senha com tamanho para hash, `nome`/login `UNIQUE`)
- [ ] Tabela `vagas` (ex.: `id`, `identificacao` UNIQUE, `tipo` CARRO/MOTO, `status` LIVRE/OCUPADA)
- [ ] Tabela `tickets` (ex.: `id`, `placa`, `vaga_id` FK, `data_entrada`, `data_saida`, `cancela_entrada`, `valor`, `status` ATIVO/PAGO/ENCERRADO)
- [ ] (Opcional) Tabela `pagamentos` ou `cancelas`
- [ ] Todas as tabelas com `ENGINE=InnoDB` (obrigatório para lock de linha)
- [ ] Restrições: PK, FK, `CHECK`/`ENUM` de status, `NOT NULL`
- [ ] Índices: `vagas(status)` (essencial para o `FOR UPDATE` não travar a tabela inteira), `tickets(placa)`, `tickets(status)`
- [ ] Script único `database/schema.sql` + `database/dados_teste.sql`
- [ ] Padronizar o nome do banco (hoje: `teste` no código, `projeto_a3` no dump)
- [ ] MER documentado em `docs/`

> 🔗 **Bloqueia:** Integrantes 4 e 5 (o service já referencia `vagas` e `tickets`).

### Integrante 2 — Autenticação e integração JDBC
- [x] Classe de conexão MySQL (`util/FabricaConexao`)
- [x] Conexão transacional com `autoCommit=false`
- [x] Login funcional (com `PreparedStatement`) ✅
- [x] Cadastro funcional (verifica usuário duplicado e senhas iguais)
- [ ] Corrigir SQL Injection no cadastro (B2)
- [ ] Hash de senha (BCrypt ou SHA-256 + salt) no cadastro e no login (B3)
- [ ] Criar `UsuarioDAO` (tirar SQL de dentro das telas)
- [ ] Remover `BancoDados.java` (H2) e a dependência H2, ou justificar (B4)
- [ ] Mover credenciais para `db.properties` fora do Git, com um `db.properties.example` (B5)
- [ ] Mensagens de erro amigáveis para falha de conexão (hoje mostra a mensagem técnica)

### Integrante 3 — Interface gráfica
- [x] Tela de Login (visual + navegação para Cadastro) ✅
- [x] Tela de Cadastro (visual + voltar/sair) ✅
- [x] Tela Principal (layout com contadores “Veículos estacionados” / “Vagas disponíveis” e botões ENTRADA/SAÍDA)
- [x] Tela de Entrada (layout: tipo de veículo carro/moto, placa, “Registrar Veículo”)
- [ ] **Corrigir imagens (B1)** — mover `img/` para `src/main/resources/img/` e trocar `"carro png.png"` por `"carro.png"`
- [ ] Botão ENTRADA abrir `Tela_Entrada`
- [ ] Botão SAÍDA abrir tela de saída/pagamento
- [ ] Criar **Tela de Saída / Pagamento** (buscar ticket por placa, mostrar tempo e valor, confirmar pagamento)
- [ ] Criar **Tela de Vagas** (tabela `JTable` com vagas e status; cadastrar vaga)
- [ ] Contadores da Tela Principal com dados reais do banco (hoje mostram “N.º”)
- [ ] Botões carro/moto marcarem a seleção visualmente
- [ ] Botão voltar nas telas internas
- [ ] Padronizar cores (já existe: azul `36,105,224` / vermelho `212,45,73`), fontes e mensagens
- [ ] Ajustes B8 (`actionPerformed`, `getPassword()`, mensagem de sucesso no cadastro)

### Integrante 4 — Operações do estacionamento
- [x] Modelos `Vaga` e `Ticket` criados (precisam ser alinhados ao banco — B7)
- [ ] `VagaDAO`: cadastrar, listar, contar livres/ocupadas
- [ ] `TicketDAO`: buscar ticket ativo por placa, listar ativos
- [ ] Ligar “Registrar Veículo” ao `EntradaVeiculoService` (validar placa antes)
- [ ] Validação de placa (formato antigo `ABC1234` e Mercosul `ABC1D23`)
- [ ] Impedir entrada de placa que já tem ticket ATIVO
- [ ] Cálculo do valor (regra de tarifa: definir fração/hora, tolerância)
- [ ] Pagamento → muda ticket para PAGO
- [ ] Saída → ticket ENCERRADO + vaga LIVRE (também em transação)
- [ ] Máquina de estados respeitada (não pagar ticket encerrado, não liberar sem pagamento etc.)

### Integrante 5 — Transações e concorrência
- [x] Transação de entrada com `COMMIT` / `ROLLBACK` (`EntradaVeiculoService`) ✅
- [x] Lock pessimista `SELECT ... FOR UPDATE` ✅
- [ ] `rollback()` explícito no `catch` + logs claros com horário e nome da cancela (B6)
- [ ] Transação também na saída/pagamento
- [ ] Classe de teste `SimulacaoConcorrencia` com **2 threads** (Cancela Norte e Cancela Sul) disparando ao mesmo tempo (usar `CountDownLatch`) com **1 vaga livre**
- [ ] Cenário de falha: forçar erro entre o INSERT do ticket e o UPDATE da vaga e provar que o ticket some (sem “carro fantasma”)
- [ ] Cenário sem lock (para comparação) mostrando o problema
- [ ] Prints/logs como evidência (inclusive `SHOW ENGINE INNODB STATUS` ou `performance_schema.data_locks`)
- [ ] Decidir: vaga específica (linha por vaga) **ou** contador de vagas — hoje o código usa vaga específica

### Integrante 6 — Qualidade, testes e documentação
- [ ] Lista de requisitos funcionais (RF) e não funcionais (RNF)
- [ ] Casos de teste (login, cadastro, entrada, lotação esgotada, pagamento, saída, concorrência, rollback)
- [ ] Registro dos resultados dos testes
- [ ] Relatório técnico
- [ ] Guia de instalação: JDK, MySQL, criar banco, rodar script, configurar credenciais, rodar o projeto
- [ ] README completo
- [ ] Roteiro da demonstração
- [ ] Remover a pasta `mavenproject1/` depois que as imagens forem movidas

---

## 5. Próximos passos — ordem sugerida

**Fase 1 — Destravar (fazer primeiro, tudo depende disso)**
1. Integrante 1: entregar `database/schema.sql` com `usuarios`, `vagas`, `tickets` (InnoDB, FKs, índices) + dados de teste.
2. Integrante 3: corrigir as imagens (B1) para o sistema parar de quebrar após o login.
3. Integrante 2: remover H2 / `BancoDados`, mover credenciais para arquivo de configuração e padronizar o nome do banco.

**Fase 2 — Fluxo principal funcionando**
4. Integrante 2: hash de senha + correção do SQL Injection.
5. Integrante 4 + 3: Tela Principal → Tela Entrada → `EntradaVeiculoService` → mensagem “Entrada liberada” ou “Lotação Esgotada”.
6. Integrante 4 + 3: contadores reais na Tela Principal.

**Fase 3 — Completar o ciclo**
7. Integrante 4 + 3: tela de saída, pagamento e liberação da vaga.
8. Integrante 5: simulação das duas cancelas + teste de rollback + evidências.

**Fase 4 — Entrega**
9. Integrante 6: casos de teste executados, relatório, guia de instalação, roteiro da apresentação.
10. Todos: ensaio da demonstração no computador que será usado.

---

## 6. Decisões em aberto (o grupo precisa definir)

- [ ] **Vaga específica ou contador?** O código atual ocupa uma vaga específica (linha em `vagas`). Manter assim?
- [ ] **Login por nome de usuário ou por e-mail?** (MySQL usa `nome`; o H2 antigo usava `email`.)
- [ ] **Tipos de vaga:** a tela tem carro/moto — vagas separadas por tipo ou vaga única?
- [ ] **Regra de cobrança:** valor por hora, fração, tolerância inicial?
- [ ] **Algoritmo de hash:** BCrypt (biblioteca `jbcrypt`) ou SHA-256 com salt (sem dependência extra)?
- [ ] **Versão do Java:** manter 26 ou baixar para 21 (LTS) para evitar problema na máquina da apresentação?
- [ ] **Nome oficial do banco:** `projeto_a3` ou `estacionamento`?

---

## 7. Como atualizar este arquivo

- Ao terminar uma tarefa, marque `[x]` e, se quiser, coloque o nome e a data: `[x] Tabela vagas — Bruno, 06/11`.
- Bug novo? Adicione na tabela da seção 3 com o próximo número (B11, B12…).
- Atualize a data no topo.
