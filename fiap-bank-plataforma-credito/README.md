# FIAP Bank — Plataforma de Crédito 💳

**Engenharia de Software — FIAP | Turma 2ESPG**

**Checkpoint 6 — Domain Driven Design (DDD) — Java**

**Responsável pelo CP6:** Arthur Araújo Tenório — RM 562272

---

## 📖 Sobre o Projeto

A Plataforma de Crédito é um microsserviço do ecossistema FIAP Bank, responsável pelo recebimento, processamento e acompanhamento de solicitações de crédito.

O sistema utiliza regras de negócio para analisar propostas, consultar informações bancárias, verificar indicadores de risco e determinar se uma solicitação deve ser aprovada, reprovada ou encaminhada para análise manual.

No CP6, o projeto foi ampliado com mecanismos de autenticação, autorização e testes automatizados.

### Principais funcionalidades

- Criação de solicitações de crédito.
- Consulta de solicitações por CPF e identificador.
- Análise de risco utilizando SCR e score de crédito.
- Aprovação ou reprovação automática conforme regras de negócio.
- Encaminhamento de solicitações para análise manual.
- Aprovação e reprovação pela Mesa de Operações.
- Integração com o Core Bancário.
- Autenticação com JWT.
- Controle de acesso baseado em perfis (RBAC).
- Autorização de consultas conforme o CPF do cliente.
- Validação de dados e tratamento padronizado de erros.
- Testes automatizados de regras de negócio e segurança.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Finalidade |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4.1.1 | Framework de aplicação |
| Spring Web | APIs REST |
| Spring Security | Autenticação e autorização |
| JWT / OAuth2 Resource Server | Tokens de acesso |
| Spring Data JPA | Persistência |
| H2 Database | Banco de dados local |
| Spring RestClient | Integração com o Core Bancário |
| Jakarta Bean Validation | Validação de requisições |
| JUnit 5 | Testes automatizados |
| Mockito | Simulação de dependências |
| MockMvc | Testes HTTP |
| Maven | Gerenciamento de dependências e build |
| Git e GitHub | Versionamento |

---

## 🏗️ Arquitetura DDD

O projeto utiliza arquitetura Maven multimódulo, com separação de responsabilidades.

| Módulo | Responsabilidade |
|---|---|
| `fiap-bank-plataforma-credito-domain` | Entidades, regras de domínio, contratos e exceções |
| `fiap-bank-plataforma-credito-application` | Serviços de aplicação, casos de uso e DTOs |
| `fiap-bank-plataforma-credito-infrastructure` | Persistência JPA e integração com sistemas externos |
| `fiap-bank-plataforma-credito-presentation` | Controllers REST, segurança, configuração e inicialização |

### Fluxo arquitetural

```text
Cliente HTTP
    |
    v
Presentation
    |
    v
Application
    |
    v
Domain
    |
    v
Contratos de persistência e integração
    |
    v
Adaptadores de Infrastructure
    |
    +---- H2 Database
    |
    +---- Core Bancário
    |
    +---- Serviços de consulta de risco
```

A arquitetura utiliza contratos e adaptadores para separar as regras de negócio dos detalhes técnicos de infraestrutura.

O Maven Enforcer também é utilizado para verificar determinadas restrições de dependência entre módulos.

---

## 🔐 Autenticação JWT — CP6

A Plataforma de Crédito utiliza autenticação baseada em JSON Web Token (JWT).

O usuário realiza login com suas credenciais e recebe um token que deve ser enviado nas requisições protegidas.

### Endpoint de autenticação

`POST /api/auth/login`

Exemplo de requisição:

```json
{
  "email": "cliente@fiapbank.com",
  "senha": "SENHA_CONFIGURADA_NO_AMBIENTE"
}
```

Exemplo da estrutura de resposta:

```json
{
  "accessToken": "TOKEN_JWT_GERADO",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

O token deve ser utilizado no cabeçalho HTTP:

```http
Authorization: Bearer TOKEN_JWT
```

### Características de segurança

- Autenticação stateless.
- Senhas verificadas com BCrypt.
- Tokens JWT assinados com HS256.
- Validação de assinatura e expiração.
- Tempo de expiração configurável.
- Proteção de endpoints por autenticação e autorização.

**Importante:** senhas, tokens e segredos JWT reais não devem ser publicados no repositório.

---

## 🛡️ Controle de Acesso — RBAC

O CP6 implementa controle de acesso baseado em perfis de usuário.

### Perfis utilizados

| Perfil | Responsabilidade |
|---|---|
| `CLIENTE` | Consultar suas próprias solicitações e realizar operações permitidas ao cliente |
| `OPERADOR` | Consultar solicitações e realizar operações de análise e decisão de crédito |
| `ADMIN` | Perfil administrativo, conforme as permissões configuradas |

### Autorização por CPF

Além do RBAC, o sistema possui verificação de acesso por CPF.

O objetivo é impedir que um cliente autenticado consulte solicitações pertencentes a outro cliente.

Exemplo:

- Cliente autenticado com CPF `12345678900`.
- Consulta ao próprio CPF: permitida.
- Consulta ao CPF de outro cliente: negada.
- Operador autorizado: pode consultar solicitações conforme suas permissões.

A autorização utiliza informações do usuário autenticado e do JWT, com integração ao Spring Security.

### Respostas esperadas

| Código HTTP | Significado |
|---|---|
| `200 OK` | Requisição processada |
| `201 Created` | Solicitação criada |
| `400 Bad Request` | Dados inválidos |
| `401 Unauthorized` | Autenticação ausente ou inválida |
| `403 Forbidden` | Usuário sem permissão |
| `404 Not Found` | Solicitação não encontrada |
| `409 Conflict` | Operação incompatível com o estado da solicitação |
| `500 Internal Server Error` | Erro interno inesperado |
| `502 Bad Gateway` | Falha na comunicação com serviço externo |

---

## 💰 Motor de Decisão de Crédito

O motor de crédito analisa as solicitações utilizando regras de negócio.

### Fluxo de processamento

1. Recebe a solicitação e valida os dados.
2. Consulta restrições no SCR.
3. Consulta o score do cliente no Bureau de Crédito.
4. Consulta informações no Core Bancário.
5. Aplica as regras de concessão.
6. Persiste a solicitação.
7. Envia uma notificação.
8. Retorna o resultado da análise.

### Regras implementadas

| Condição | Resultado |
|---|---|
| Cliente com restrição no SCR | `REPROVADO` |
| Score inferior a 500 | `REPROVADO` |
| Parcela estimada superior a 30% da renda | `EM_ANALISE` |
| Sem restrições e dentro dos critérios | `APROVADO` |

A parcela estimada é calculada pela divisão do valor solicitado pela quantidade de parcelas, sem inclusão de juros nessa estimativa simplificada.

### Exemplo

Uma solicitação de R$ 60.000,00 em 12 parcelas, com renda mensal de R$ 3.000,00, produz uma parcela estimada de R$ 5.000,00.

Como esse valor ultrapassa 30% da renda, a solicitação é encaminhada para análise manual.

---

## 🌐 API REST — Endpoints

**URL base:** `http://localhost:8081`

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Autenticar usuário e gerar JWT |

### Solicitações de crédito

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/v1/solicitacoes` | Listar solicitações autorizadas |
| GET | `/api/v1/solicitacoes?cpf=12345678900` | Consultar solicitações por CPF |
| POST | `/api/v1/solicitacoes` | Criar solicitação |
| PUT | `/api/v1/solicitacoes/{id}/aprovacao` | Aprovar solicitação |
| PUT | `/api/v1/solicitacoes/{id}/reprovacao` | Reprovar solicitação |

As permissões dependem do perfil autenticado e das verificações de autorização aplicadas aos endpoints.

### Exemplo de criação de solicitação

```bash
curl -i -X POST \
  "http://localhost:8081/api/v1/solicitacoes" \
  -H "Authorization: Bearer $TOKEN_CLIENTE" \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": "12345678900",
    "nomeCliente": "Ana Silva",
    "tipoCredito": "EMPRESTIMO_PESSOAL",
    "valorSolicitado": 15000.00,
    "quantidadeParcelas": 24,
    "rendaMensal": 7500.00
  }'
```

### Consultar solicitações do cliente

```bash
curl -i \
  "http://localhost:8081/api/v1/solicitacoes?cpf=12345678900" \
  -H "Authorization: Bearer $TOKEN_CLIENTE"
```

### Listar solicitações como operador

```bash
curl -i \
  "http://localhost:8081/api/v1/solicitacoes" \
  -H "Authorization: Bearer $TOKEN_OPERADOR"
```

### Aprovar solicitação

```bash
curl -i -X PUT \
  "http://localhost:8081/api/v1/solicitacoes/3/aprovacao" \
  -H "Authorization: Bearer $TOKEN_OPERADOR" \
  -H "Content-Type: application/json" \
  -d '{
    "solicitacaoId": 3,
    "parecerOperador": "Aprovado pela Mesa de Operacoes"
  }'
```

### Reprovar solicitação

```bash
curl -i -X PUT \
  "http://localhost:8081/api/v1/solicitacoes/4/reprovacao" \
  -H "Authorization: Bearer $TOKEN_OPERADOR" \
  -H "Content-Type: application/json" \
  -d '{
    "solicitacaoId": 4,
    "motivoReprovacao": "Reprovado apos analise"
  }'
```

---

## 🔄 Endpoints Legados

Para preservar a compatibilidade com os consumidores existentes, a Plataforma mantém controllers legados.

**URL base:** `/api/credito`

Entre as rotas documentadas anteriormente estão:

- `POST /api/credito/solicitarCredito`
- `POST /api/credito/consultarStatusCredito`
- `POST /api/credito/listarTodasSolicitacoes`
- `POST /api/credito/buscarSolicitacaoPorId`
- `POST /api/credito/aprovarSolicitacao`
- `POST /api/credito/reprovarSolicitacao`
- `POST /api/credito/consultarClienteCore`

Essas rotas são destinadas à compatibilidade com aplicações existentes. A autenticação e a autorização aplicáveis devem ser respeitadas.

Para novas integrações, recomenda-se utilizar os endpoints versionados.

---

## 🔗 Integração com o Core Bancário

A Plataforma de Crédito utiliza Spring RestClient para consultar informações bancárias.

**Endereço padrão do Core:** `http://localhost:8080/api/v1`

O Core Bancário deve estar disponível para as operações que dependem dessa integração.

Quando ocorre uma falha de comunicação, a aplicação utiliza tratamento de exceções para retornar uma resposta HTTP adequada.

---

## ⚙️ Configuração da Aplicação

### Pré-requisitos

- JDK 21.
- Maven 3.9 ou superior.
- IntelliJ IDEA ou outra IDE Java.
- Core Bancário disponível para testes integrados.

### Propriedades principais

| Propriedade | Descrição |
|---|---|
| `server.port` | Porta HTTP, configurada como 8081 |
| `spring.datasource.url` | Conexão com o banco H2 |
| `core.bancario.url` | URL do Core Bancário |
| `security.jwt.secret` | Chave de assinatura JWT |
| `security.jwt.expiration-minutes` | Tempo de validade do token |
| `security.jwt.issuer` | Emissor do token |

### Configuração da chave JWT

A chave JWT deve ser configurada por variável de ambiente, sem inserir o segredo real no código ou no GitHub.

Exemplo no Git Bash:

```bash
export JWT_SECRET="CHAVE_BASE64_VALIDA_COM_PELO_MENOS_32_BYTES"
```

O nome da variável deve corresponder à configuração utilizada no `application.yml`.

---

## 🚀 Como Compilar e Executar

A partir da pasta `fiap-bank-plataforma-credito`:

### Compilar e executar os testes

```bash
mvn clean verify
```

### Gerar os artefatos

```bash
mvn clean package
```

### Iniciar a aplicação

Com as dependências do projeto instaladas:

```bash
cd fiap-bank-plataforma-credito-presentation
mvn spring-boot:run
```

Também é possível iniciar a classe principal `PlataformaCreditoApplication` pelo IntelliJ IDEA.

A aplicação utiliza a porta `8081`.

### Executar testes

```bash
mvn test
```

---

## 🧪 Testes Automatizados — CP6

O projeto utiliza JUnit 5, Mockito, MockMvc e ferramentas de teste do Spring.

### Categorias de testes

**Testes de controllers**

- Requisições HTTP.
- Validação de entradas.
- Respostas dos endpoints.

**Testes do motor de crédito**

- Aprovação automática.
- Reprovação por restrições.
- Reprovação por score insuficiente.
- Encaminhamento para análise manual.
- Aprovação e reprovação de solicitações.
- Falhas na comunicação com o Core Bancário.

**Testes de segurança**

- Autorização baseada em perfis.
- Acesso do cliente ao próprio CPF.
- Negação de acesso a CPF de terceiros.
- Permissões de operadores.
- Verificação de regras de autorização.

### Execução

```bash
mvn clean test
```

Nas validações realizadas durante o desenvolvimento do CP6, o Maven apresentou `BUILD SUCCESS`, sem falhas ou erros na execução informada dos testes.

A quantidade de testes deve ser conferida no relatório mais recente do Maven antes da entrega final.

---

## 🧾 Tratamento Global de Exceções

A API utiliza `GlobalExceptionHandler`, com `@RestControllerAdvice`, para padronizar respostas de erro.

Exemplo:

```json
{
  "timestamp": "2026-11-02T16:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Voce nao tem permissao para acessar este recurso."
}
```

O tratamento contempla validações, solicitações inexistentes, conflitos de estado, acesso negado, falhas de integração e erros internos.

---

## 🔒 Boas Práticas

- Separação de responsabilidades em módulos.
- Uso de contratos e adaptadores.
- Autenticação JWT.
- Autorização por perfil e CPF.
- Validação das entradas.
- Tratamento global de exceções.
- Testes automatizados.
- Controle de versão com Git.
- Configurações sensíveis fora do repositório.
- Manutenção de compatibilidade com endpoints legados.

O banco H2 e os usuários de exemplo são destinados ao desenvolvimento e à demonstração acadêmica.

---

## 👥 Créditos e Participação

### Checkpoint 6 — Desenvolvimento individual

**Arthur Araújo Tenório — RM 562272**

Responsável pela evolução da Plataforma de Crédito no CP6, incluindo autenticação JWT, autorização RBAC, controle de acesso por CPF, testes e validação das funcionalidades.

### Checkpoint 5 — Projeto anterior

- Arthur Araújo Tenório — RM 562272
- Breno Gonçalves Báo — RM 564037
- Vinicius Cavalcanti dos Reis — RM 562063

O CP6 dá continuidade à base desenvolvida no checkpoint anterior.

---

## 🎓 Projeto Acadêmico

**FIAP — Engenharia de Software**

**Checkpoint 6 — Domain Driven Design — Java**

O projeto demonstra a aplicação de conceitos de arquitetura de software, APIs REST, persistência, integração entre sistemas, autenticação, autorização, regras de negócio e testes automatizados em um cenário bancário.

**FIAP Bank — Plataforma de Crédito**