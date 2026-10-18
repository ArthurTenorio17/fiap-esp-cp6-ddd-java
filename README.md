
# FIAP Bank - Esteira de Crédito 🏦💳
----
## 👥 Integrantes

**Turma:** 2ESPG

| Nome | RM |
| :--- | :--- |
| Arthur Araújo Tenório | 562272 |
| Breno Gonçalves Báo | 564037 |
| Vinicius Cavalcanti dos Reis | 562063 |
-----
Monorepo contendo o ecossistema da **Esteira de Crédito do FIAP Bank**, composto por quatro aplicações integradas:

- Core Bancário
- Plataforma de Crédito
- Mesa de Operações de Crédito
- Internet Banking Mobile

O projeto demonstra a integração entre serviços bancários, análise de solicitações de crédito e interfaces utilizadas por clientes e operadores.

---

## 📁 Estrutura do Workspace e Documentação

Cada aplicação possui seu próprio `README.md`, com informações sobre tecnologias, arquitetura e funcionalidades.

| Projeto | Tecnologia | Porta | Documentação |
| :--- | :--- | :---: | :--- |
| `fiap-bank-core-bancario` | Java 21, Spring Boot, JPA, DDD e Maven multimódulo | `8080` | [README do Core Bancário](./fiap-bank-core-bancario/README.md) |
| `fiap-bank-plataforma-credito` | Java 21, Spring Boot, JPA, RestClient, DDD e Maven multimódulo | `8081` | [README da Plataforma de Crédito](./fiap-bank-plataforma-credito/README.md) |
| `fiap-bank-mesa-operacoes-credito` | Java 21, Spring Boot, Thymeleaf e OpenFeign | `8082` | [README da Mesa de Operações](./fiap-bank-mesa-operacoes-credito/README.md) |
| `fiap-bank-internet-banking-mobile` | Angular, TypeScript e CSS responsivo | `4200` | [README do Mobile Angular](./fiap-bank-internet-banking-mobile/README.md) |

### Organização das aplicações

**Core Bancário:** responsável pelos dados de clientes e contas bancárias.

**Plataforma de Crédito:** recebe solicitações, consulta informações bancárias e executa o fluxo de análise de crédito.

**Mesa de Operações:** interface web para visualização e tratamento de solicitações de crédito.

**Internet Banking Mobile:** interface Angular utilizada pelo cliente para consultar informações, solicitar crédito e acompanhar o histórico de solicitações.

---

## 🛠️ Pré-requisitos

Para executar o projeto, é necessário instalar:

- **JDK 21** (ou superior)
- **Apache Maven 3.9+**
- **Node.js e npm**
- Navegador web atualizado
- Git, para controle de versão

### Verificar o Java

Antes de executar qualquer aplicação Java, confira a versão configurada no terminal:

```bash
java -version
mvn -version
```

Os comandos devem indicar o uso do **Java 21 ou superior**.

### Configuração do Java 21 no Git Bash

Se o Maven estiver utilizando outra versão do Java, configure o `JAVA_HOME` com o caminho da sua instalação.

Exemplo de configuração no Git Bash:

```bash
export JAVA_HOME="/c/Users/SEU_USUARIO/.jdks/NOME_DO_JDK_21"
export PATH="$JAVA_HOME/bin:$PATH"
```

Em seguida, confirme novamente:

```bash
java -version
mvn -version
```

**Importante:** o caminho do `JAVA_HOME` varia conforme o computador. A configuração feita com `export` vale para a sessão atual do terminal.

---

## 🚦 Ordem Recomendada de Execução

Para testar as integrações, inicie as aplicações na seguinte ordem:

1. Core Bancário
2. Plataforma de Crédito
3. Mesa de Operações
4. Internet Banking Mobile

**Importante:** os comandos abaixo consideram que o terminal está inicialmente na pasta principal do repositório.

Cada aplicação deve permanecer em execução em seu próprio terminal.

### 1. Core Bancário — Porta 8080

Responsável pela gestão dos dados bancários utilizados na integração.

No primeiro terminal:

```bash
cd fiap-bank-core-bancario
mvn clean install
cd fiap-bank-core-bancario-presentation
mvn spring-boot:run
```

Endereços:

- API: `http://localhost:8080/api/v1`
- Contas: `http://localhost:8080/api/v1/contas`
- Console H2: `http://localhost:8080/h2-console`

Aguarde a mensagem de inicialização antes de iniciar a Plataforma de Crédito.

### 2. Plataforma de Crédito — Porta 8081

Responsável pelo processamento das solicitações e pela integração com o Core Bancário.

Em um segundo terminal, a partir da pasta principal do repositório:

```bash
cd fiap-bank-plataforma-credito
mvn clean install
cd fiap-bank-plataforma-credito-presentation
mvn spring-boot:run
```

Endereços:

- API de crédito: `http://localhost:8081/api/v1`
- Solicitações: `http://localhost:8081/api/v1/solicitacoes`
- Console H2: `http://localhost:8081/h2-console`

**Atenção:** o Core Bancário deve estar em execução para que as consultas integradas de clientes e contas funcionem.

### 3. Mesa de Operações — Porta 8082

Interface web desenvolvida com Thymeleaf para acompanhamento e tratamento das solicitações.

Em um terceiro terminal, a partir da pasta principal do repositório:

```bash
cd fiap-bank-mesa-operacoes-credito
mvn spring-boot:run
```

Abra no navegador:

`http://localhost:8082`

A Mesa consulta os dados da Plataforma de Crédito. Portanto, mantenha a aplicação da porta `8081` em execução.

### 4. Internet Banking Mobile — Porta 4200

Interface Angular responsiva para o cliente bancário.

Em um quarto terminal, a partir da pasta principal do repositório:

```bash
cd fiap-bank-internet-banking-mobile
npm install
npm start
```

Abra no navegador:

`http://localhost:4200`

O Angular utiliza a Plataforma de Crédito para enviar solicitações, consultar seus status e obter informações bancárias por meio da integração com o Core.

---

## 🔗 Integração entre as Aplicações

O fluxo principal de comunicação é:

```text
Internet Banking Mobile (Angular - 4200)
                 |
                 v
Plataforma de Crédito (Spring Boot - 8081)
                 |
                 v
Core Bancário (Spring Boot - 8080)

Mesa de Operações (Thymeleaf - 8082)
                 |
                 v
Plataforma de Crédito (Spring Boot - 8081)
```

### Fluxo de solicitação de crédito

1. O cliente acessa o Internet Banking Mobile.
2. O Angular envia a solicitação para a Plataforma de Crédito.
3. A Plataforma consulta as informações necessárias no Core Bancário.
4. A solicitação é processada conforme as regras implementadas.
5. O cliente pode consultar o histórico e o status das solicitações.
6. A Mesa de Operações apresenta as solicitações disponíveis para acompanhamento e tratamento.

### Integração com o Core Bancário

O Angular consulta as informações bancárias por meio da Plataforma de Crédito.

Entre os dados apresentados estão:

- Agência
- Número da conta
- Situação da conta
- Saldo bancário

O saldo exibido deve refletir os dados retornados pela consulta ao Core Bancário.

**A aprovação de uma solicitação de crédito não deve aumentar artificialmente o saldo exibido no Angular.** A atualização efetiva do saldo depende das operações realizadas pelo sistema bancário.

---

## 🧪 Dados Pré-Carregados para Testes

O projeto possui dados de exemplo para facilitar a validação dos fluxos.

| Cliente | CPF | Cenário |
| :--- | :--- | :--- |
| Ana Silva | `12345678900` | Cliente utilizado nos testes de integração |
| Carlos Eduardo Oliveira | `98765432100` | Cliente de exemplo |
| Mariana Souza | `55544433399` | Cenário de restrição no SCR |

### Exemplo de conta bancária

Nos testes realizados com o cliente **Ana Silva**, a consulta ao Core Bancário retornou:

- Agência: `0001`
- Conta: `12345-6`
- Saldo: `R$ 12.500,00`

Os dados são utilizados para demonstração e testes locais.

---

## 📡 Exemplos de Requisições

Os exemplos abaixo podem ser executados com `curl`, Postman ou outra ferramenta de requisições HTTP.

### Consultar contas no Core Bancário

```bash
curl -i http://localhost:8080/api/v1/contas
```

### Listar solicitações de crédito

```bash
curl -i http://localhost:8081/api/v1/solicitacoes
```

### Solicitar crédito

```bash
curl -i -X POST "http://localhost:8081/api/v1/solicitacoes" \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": "12345678900",
    "nomeCliente": "Ana Silva",
    "tipoCredito": "PESSOAL",
    "valorSolicitado": 5000.00,
    "quantidadeParcelas": 12,
    "rendaMensal": 4500.00
  }'
```

O resultado depende das regras de análise de crédito implementadas.

### Consultar cliente no Core por meio da Plataforma

```bash
curl -i -X POST "http://localhost:8081/api/credito/consultarClienteCore" \
  -H "Content-Type: application/json" \
  -d '{"cpfCliente":"12345678900"}'
```

Esse endpoint permite validar a comunicação da Plataforma de Crédito com o Core Bancário.

---

## 📱 Internet Banking Mobile

O Angular oferece uma interface responsiva para utilização pelo cliente.

Entre as funcionalidades disponíveis estão:

- Visualização das informações da conta;
- Consulta do saldo bancário;
- Solicitação de crédito;
- Simulação de parcelas;
- Consulta do histórico de solicitações;
- Acompanhamento do status das solicitações;
- Tela demonstrativa de extrato.

### Observação sobre o extrato

**O extrato apresentado no Angular é demonstrativo.**

As movimentações e os totais dessa tela são exemplos utilizados para fins visuais e de apresentação.

Eles **não representam transações bancárias reais consultadas no Core Bancário**.

Essa informação também é apresentada na interface do aplicativo para evitar confusão entre dados demonstrativos e dados efetivamente integrados.

---

## 🖥️ Mesa de Operações de Crédito

A Mesa de Operações utiliza Spring Boot e Thymeleaf.

O painel permite visualizar:

- Total de solicitações;
- Solicitações pendentes de análise;
- Solicitações aprovadas;
- Solicitações reprovadas;
- Informações dos clientes;
- Valores solicitados;
- Quantidade de parcelas;
- Informações de score e SCR;
- Status e detalhes das solicitações.

A interface consulta a Plataforma de Crédito para apresentar os dados da esteira.

---

## ✅ Testes Automatizados

Foram executados testes automatizados dos dois backends principais.

| Projeto | Testes executados | Falhas | Erros | Resultado |
| :--- | ---: | ---: | ---: | :--- |
| Core Bancário | 23 | 0 | 0 | SUCCESS |
| Plataforma de Crédito | 6 | 0 | 0 | SUCCESS |
| **Total** | **29** | **0** | **0** | **Aprovado** |

### Executar os testes do Core Bancário

A partir da pasta principal do repositório:

```bash
cd fiap-bank-core-bancario
mvn clean test
```

### Executar os testes da Plataforma de Crédito

A partir da pasta principal do repositório:

```bash
cd fiap-bank-plataforma-credito
mvn clean test
```

### Resultados obtidos

Na validação realizada em 16/10/2026:

- Core Bancário: `BUILD SUCCESS`, com 23 testes aprovados;
- Plataforma de Crédito: `BUILD SUCCESS`, com 6 testes aprovados;
- Nenhuma falha ou erro nos testes executados.

Os testes automatizados complementam, mas não substituem, a validação manual do fluxo integrado.

---

## 🔍 Validação Manual da Integração

O fluxo foi verificado localmente com as aplicações em execução.

### Cenários verificados

**1. Consulta ao Core Bancário**

A API do Core Bancário respondeu com os dados da conta do cliente de teste.

**2. Integração entre Plataforma de Crédito e Core**

A consulta de cliente realizada pela Plataforma retornou os dados bancários esperados.

**3. Criação de solicitação de crédito**

Foi realizada uma requisição HTTP de criação de solicitação, com resposta `201 Created`.

**4. Consulta da solicitação na Mesa**

A solicitação criada foi exibida no painel da Mesa de Operações.

**5. Exibição dos dados no Angular**

A interface apresentou o saldo e os dados da conta, com formatação brasileira de moeda.

**6. Correção da apresentação visual**

Foram realizados ajustes de contraste e legibilidade na Mesa de Operações.

**7. Tratamento dos dados demonstrativos**

A interface Angular passou a identificar o extrato como demonstrativo, sem apresentar suas movimentações como transações reais.

---

## ⚠️ Observações Importantes

- Os projetos Java exigem JDK 21 ou superior (o `maven-enforcer-plugin` bloqueia versões anteriores).
- A Plataforma de Crédito acessa o Core Bancário via `RestClient`; a Mesa de Operações ainda usa Spring Cloud OpenFeign para chamar a Plataforma.
- Os endpoints antigos `/api/credito/*` da Plataforma foram mantidos apenas por retrocompatibilidade com o Angular e a Mesa; novos clientes devem usar `/api/v1`.
- O Core Bancário deve estar disponível antes das operações da Plataforma de Crédito que dependem de seus dados.
- A Mesa de Operações depende da Plataforma de Crédito.
- O Angular também depende da Plataforma de Crédito para os recursos integrados.
- Os dados locais de teste podem ser reinicializados conforme a configuração de persistência de cada aplicação.
- A aprovação de crédito não equivale automaticamente ao lançamento do valor na conta bancária.
- O extrato do Angular contém dados demonstrativos.

---

## 📚 Documentação Complementar

Para detalhes específicos de cada aplicação, consulte:

- [Core Bancário](./fiap-bank-core-bancario/README.md)
- [Plataforma de Crédito](./fiap-bank-plataforma-credito/README.md)
- [Mesa de Operações](./fiap-bank-mesa-operacoes-credito/README.md)
- [Internet Banking Mobile](./fiap-bank-internet-banking-mobile/README.md)

---

## 🎓 Projeto Acadêmico

Projeto desenvolvido no contexto do **Checkpoint 5 da FIAP**, com foco em:

- Java e Spring Boot;
- Domain-Driven Design (DDD);
- Arquitetura em módulos Maven (domain, infrastructure, application e presentation);
- APIs RESTful versionadas, com Bean Validation e tratamento global de erros;
- Persistência com Spring Data JPA;
- Integração entre serviços (RestClient na Plataforma e OpenFeign na Mesa);
- Interfaces web com Angular e Thymeleaf;
- Testes automatizados;
- Controle de versão com Git e GitHub.

---

---

**FIAP Bank — Esteira de Crédito**
