# FIAP Bank - Plataforma de Crédito 💳

Microsserviço de **Esteira e Motor de Decisão de Crédito** do FIAP Bank, responsável pelo processamento de propostas de crédito, aplicação das regras de negócio, análise de risco e comunicação com o Core Bancário.

O projeto foi refatorado seguindo os princípios de **Domain-Driven Design (DDD)**, com separação em módulos Maven e disponibilização de uma API RESTful versionada em `/api/v1`.

Para garantir a retrocompatibilidade, a aplicação também mantém os endpoints legados, permitindo que clientes existentes continuem funcionando durante a migração.

---

## 🛠️ Tecnologias Utilizadas

- Java 21
- Spring Boot 4.1
- Spring Web
- Spring Data JPA
- Spring RestClient para comunicação com o Core Bancário
- Spring Boot Actuator
- H2 Database
- Maven
- JUnit e Mockito para testes

---

## 🏗️ Arquitetura DDD

A Plataforma de Crédito está organizada em quatro módulos Maven:

| Módulo | Responsabilidade |
|---|---|
| `fiap-bank-plataforma-credito-domain` | Entidades de domínio, regras de negócio e contratos de repositórios |
| `fiap-bank-plataforma-credito-application` | Casos de uso, serviços e DTOs |
| `fiap-bank-plataforma-credito-infrastructure` | Persistência JPA e integração HTTP com o Core Bancário |
| `fiap-bank-plataforma-credito-presentation` | Controllers REST, controllers legados e inicialização da aplicação |

Essa organização permite separar as responsabilidades e reduzir o acoplamento entre as camadas.

---

## 📋 Pré-requisitos

- Java JDK 21
- Apache Maven 3.9+
- Core Bancário disponível na porta `8080` para operações que dependem da integração

A Plataforma de Crédito utiliza a porta `8081`.

---

## 🚀 Como Compilar e Executar

Na pasta raiz do projeto `fiap-bank-plataforma-credito`, execute:

```bash
mvn clean install
```

Para iniciar a aplicação, execute:

```bash
mvn spring-boot:run -pl fiap-bank-plataforma-credito-presentation -am
```

A aplicação estará disponível em:

`http://localhost:8081`

Para executar os testes:

```bash
mvn test
```

---

## ⚙️ Configuração

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `server.port` | `8081` | Porta da Plataforma de Crédito |
| `core.bancario.url` | `http://localhost:8080/api/v1` | URL base do Core Bancário |

As configurações podem ser ajustadas conforme o ambiente de execução.

---

## 🌐 APIs RESTful

A Plataforma de Crédito disponibiliza endpoints RESTful versionados em `/api/v1`.

A API utiliza métodos HTTP apropriados para operações de criação, consulta e atualização de solicitações de crédito.

Os controllers RESTful coexistem com os controllers legados, permitindo uma migração gradual das aplicações clientes.

---

## 🔄 Endpoints Legados (Compatibilidade)

Os endpoints abaixo pertencem à API original da Plataforma de Crédito.

Eles continuam documentados para preservar a compatibilidade com clientes que utilizam rotas orientadas a ações e o método HTTP `POST`.

**URL base:** `http://localhost:8081/api/credito`

### 1. Solicitar Crédito

**Endpoint:** `POST /api/credito/solicitarCredito`

```json
{
  "cpfCliente": "12345678900",
  "nomeCliente": "Ana Silva",
  "tipoCredito": "EMPRESTIMO_PESSOAL",
  "valorSolicitado": 15000.00,
  "quantidadeParcelas": 24,
  "rendaMensal": 7500.00
}
```

### 2. Consultar Status por CPF

**Endpoint:** `POST /api/credito/consultarStatusCredito`

```json
{
  "cpfCliente": "12345678900"
}
```

### 3. Listar Todas as Solicitações

**Endpoint:** `POST /api/credito/listarTodasSolicitacoes`

Corpo da requisição:

```json
{}
```

### 4. Buscar Solicitação por ID

**Endpoint:** `POST /api/credito/buscarSolicitacaoPorId`

```json
{
  "id": 1
}
```

### 5. Aprovar Solicitação

**Endpoint:** `POST /api/credito/aprovarSolicitacao`

```json
{
  "id": 1,
  "parecerOperador": "Proposta aprovada conforme política de crédito."
}
```

### 6. Reprovar Solicitação

**Endpoint:** `POST /api/credito/reprovarSolicitacao`

```json
{
  "id": 1,
  "parecerOperador": "Margem consignável insuficiente."
}
```

### 7. Consultar Cliente no Core Bancário

**Endpoint:** `POST /api/credito/consultarClienteCore`

```json
{
  "cpfCliente": "12345678900"
}
```

---

## 🔗 Integração com o Core Bancário

A Plataforma de Crédito se comunica com o Core Bancário por meio de um adaptador de infraestrutura baseado em Spring `RestClient`.

Essa integração permite consultar informações bancárias necessárias ao processamento das propostas de crédito.

O Core Bancário disponibiliza sua API na porta `8080`, utilizando rotas versionadas em `/api/v1`.

---

## 🧪 Testes

O projeto utiliza JUnit, Mockito e ferramentas de teste do Spring para validar o comportamento dos componentes.

Entre os testes existentes estão:

- Testes do controller REST de crédito.
- Testes de requisições HTTP utilizando MockMvc.
- Testes do adaptador de integração com o Core Bancário.

Para executar a suíte de testes:

```bash
mvn test
```

---

## 🔐 Compatibilidade com Aplicações Legadas

A estratégia de migração adotada consiste em manter os contratos antigos enquanto a API RESTful é disponibilizada.

Os controllers legados atuam como pontos de compatibilidade, permitindo que aplicações existentes continuem utilizando suas rotas originais.

Essa abordagem reduz o risco de interrupções durante a refatoração e possibilita a atualização gradual dos consumidores da API.

---

## 👥 Integrantes

- Arthur Araujo Tenório — RM: 562272
- Breno Gonçalves Báo — RM: 564037
- Vinicius Cavalcanti dos Reis — RM: 562063

---

## 📚 Projeto Acadêmico

Projeto desenvolvido para o **Checkpoint 5 — FIAP Bank: Plataforma de Concessão de Crédito**, com foco em arquitetura DDD, APIs RESTful, persistência, validação, testes e retrocompatibilidade.