
# FIAP Bank - Plataforma de Crédito e Segurança 🏦🔐

----
## 🎓 Identificação Acadêmica

| | |
| :--- | :--- |
| **Aluno responsável** | Arthur Araújo Tenório |
| **RM** | 562272 |
| **Curso** | Engenharia de Software |
| **Instituição** | FIAP |
| **Disciplina** | Domain Driven Design — Java |
| **Atividade** | Checkpoint 6 |
| **Turma** | 2ESPG |

### 👥 Integrantes

| Nome | RM |
| :--- | :--- |
| Arthur Araújo Tenório | 562272 |
| Breno Gonçalves Báo | 564037 |
| Vinicius Cavalcanti dos Reis | 562063 |

-----

Monorepo da **Esteira de Crédito do FIAP Bank**. No Checkpoint 6, a Plataforma de Crédito passa a ser um **OAuth2 Resource Server stateless**: toda requisição protegida apresenta um JWT assinado, validado de forma autônoma, com controle de acesso por perfil. O Internet Banking Mobile e a Mesa de Operações propagam o token por meio de interceptadores HTTP.

---

## 🏗️ Arquitetura e Módulos

| Projeto | Tecnologia | Porta | Documentação |
| :--- | :--- | :---: | :--- |
| `fiap-bank-core-bancario` | Java 21, Spring Boot, JPA, DDD, Maven multimódulo | `8080` | [README](./fiap-bank-core-bancario/README.md) |
| `fiap-bank-plataforma-credito` | Java 21, Spring Boot, Spring Security, OAuth2 Resource Server, JPA, DDD, Maven multimódulo | `8081` | [README](./fiap-bank-plataforma-credito/README.md) |
| `fiap-bank-mesa-operacoes-credito` | Java 21, Spring Boot, Thymeleaf, OpenFeign | `8082` | [README](./fiap-bank-mesa-operacoes-credito/README.md) |
| `fiap-bank-internet-banking-mobile` | Angular 17, TypeScript | `4200` | [README](./fiap-bank-internet-banking-mobile/README.md) |

```text
Internet Banking Mobile (Angular - 4200)         Mesa de Operações (Thymeleaf - 8082)
  HttpInterceptor: Authorization: Bearer            Feign RequestInterceptor: Authorization: Bearer
                    |                                                |
                    +----------------------+-------------------------+
                                           v
                     Plataforma de Crédito (Resource Server - 8081)
                     Emite o JWT em /api/auth/login e valida as demais rotas
                                           |
                                           v
                              Core Bancário (Spring Boot - 8080)
```

### Segurança e camadas DDD

As dependências do Spring Security ficam **somente no módulo `presentation`**. O `domain` define as portas (`TokenGeracaoPort`, `SenhaVerificacaoPort`, `UsuarioConsultaPort`) sem conhecer o framework.

| Classe | Responsabilidade |
| :--- | :--- |
| `SecurityConfig` | `SecurityFilterChain`, sessão `STATELESS`, CSRF e `frameOptions` desativados, rotas públicas, CORS e `JwtAuthenticationConverter` |
| `JwtConfig` | `JwtEncoder` e `JwtDecoder` (Nimbus): assinatura HS256, expiração e emissor |
| `TokenService` | Emissão do token com as claims `iss`, `sub`, `iat`, `exp`, `cpf`, `conta`, `roles` e `scope` |
| `CpfAuthorizationService` | Garante que um CLIENTE opere somente sobre o próprio CPF |
| `FeignClientConfig` (Mesa) | `RequestInterceptor` que propaga o Bearer Token |
| `authInterceptor` (Angular) | Anexa o Bearer Token somente às rotas protegidas da Plataforma |

### Perfis de acesso

| Operação | CLIENTE | OPERADOR | ADMIN |
| :--- | :---: | :---: | :---: |
| Solicitar crédito | Próprio CPF | Qualquer CPF | Qualquer CPF |
| Consultar solicitações por CPF | Próprio CPF | Qualquer CPF | Qualquer CPF |
| Consultar conta no Core | Própria conta | Qualquer CPF | Qualquer CPF |
| Listar todas / buscar por ID | ❌ 403 | ✅ | ✅ |
| Aprovar / reprovar | ❌ 403 | ✅ | ✅ |

O CLIENTE é autorizado pela autoridade `SCOPE_ROLE_CLIENTE` (claim `scope`). OPERADOR e ADMIN são autorizados por `ROLE_OPERADOR` e `ROLE_ADMIN` (claim `roles`). Sem token, ou com token inválido, expirado ou de outro emissor, a resposta é **401**. Sem permissão, é **403**.

### Como a Mesa obtém o token

Um navegador não envia o header `Authorization` ao abrir uma página Thymeleaf. Por isso, a Mesa tem uma tela de login que autentica o operador na Plataforma e guarda o JWT em um cookie `HttpOnly` e `SameSite=Strict`. O `RequestInterceptor` do Feign usa o header `Authorization` recebido, se houver, ou o token desse cookie. Não há `HttpSession`, e cada ação usa a identidade do próprio operador.

---

## 🛠️ Requisitos de Execução

- **JDK 21** (exigido pelo `maven-enforcer-plugin`)
- **Apache Maven 3.9+**
- **Node.js e npm** (testado com Node 26 e npm 11)
- Navegador atualizado; o Chrome é necessário para `ng test`

---

## ⚙️ Variáveis de Ambiente

Nenhuma variável é necessária para a demonstração local.

| Variável | Aplicação | Padrão | Descrição |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Plataforma | `dev` | `dev` cria os usuários de teste e usa a chave de exemplo. Qualquer outro perfil, como `prod`, exige `JWT_SECRET` |
| `JWT_SECRET` | Plataforma | Chave de exemplo, só no perfil `dev` | Chave HS256 com no mínimo 32 bytes |
| `JWT_EXPIRATION_SECONDS` | Plataforma | `7200` | Validade do token |
| `JWT_ISSUER` | Plataforma | `fiap-bank-plataforma-credito` | Emissor do token e valor exigido na validação |
| `CORE_BANCARIO_URL` | Plataforma | `http://localhost:8080/api/v1` | URL do Core Bancário |
| `PLATAFORMA_CREDITO_URL` | Mesa | `http://localhost:8081` | URL da Plataforma |
| `MESA_COOKIE_SECURE` | Mesa | `false` | Use `true` quando a Mesa for servida por HTTPS |

**Atenção:** a chave de `application-dev.yml` é o exemplo público do enunciado, não um segredo real. Fora do ambiente local, gere uma chave própria e nunca a coloque no repositório:

```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="$(openssl rand -base64 48)"
```

---

## 🚦 Como Iniciar

Inicie nesta ordem, cada aplicação em seu próprio terminal, a partir da pasta principal do repositório. Se as portas estiverem ocupadas por uma execução anterior, encerre-a primeiro.

### 1. Core Bancário — 8080

```bash
cd fiap-bank-core-bancario
mvn clean install
cd fiap-bank-core-bancario-presentation
mvn spring-boot:run
```

### 2. Plataforma de Crédito — 8081

```bash
cd fiap-bank-plataforma-credito
mvn clean install
cd fiap-bank-plataforma-credito-presentation
mvn spring-boot:run
```

Console H2: `http://localhost:8081/h2-console` (JDBC URL `jdbc:h2:mem:plataformacreditodb`, usuário `sa`, sem senha).

### 3. Mesa de Operações — 8082

```bash
cd fiap-bank-mesa-operacoes-credito
mvn spring-boot:run
```

Abra `http://localhost:8082` e entre com um usuário OPERADOR ou ADMIN.

### 4. Internet Banking Mobile — 4200

```bash
cd fiap-bank-internet-banking-mobile
npm install
npm start
```

Abra `http://localhost:4200` e entre com o usuário CLIENTE.

---

## 🧪 Usuários e Dados de Demonstração

Criados apenas no perfil `dev`, somente para testes locais.

| E-mail | Senha | Perfil | Onde usar |
| :--- | :--- | :--- | :--- |
| `cliente@fiapbank.com` | `Cliente@123` | CLIENTE (CPF `98765432100`, conta `78901-2`) | Internet Banking |
| `teste@fiapbank.com` | `Teste@123` | OPERADOR | Mesa de Operações |
| `admin@fiapbank.com` | `Admin@123` | ADMIN | Mesa de Operações |

| Cliente no Core | CPF | Cenário |
| :--- | :--- | :--- |
| Ana Silva | `12345678900` | Conta `12345-6` |
| Carlos Eduardo Oliveira | `98765432100` | Conta `78901-2`, usada pelo usuário CLIENTE |
| Mariana Souza | `55544433399` | Restrição no SCR, reprovação automática |

**Regra do motor de crédito:** restrição no SCR ou score abaixo de 500 reprova; parcela acima de 30% da renda vai para a Mesa (`EM_ANALISE`); os demais casos são aprovados.

---

## 🔑 Autenticação JWT

### Obter o token

```bash
curl -i -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@fiapbank.com","senha":"Cliente@123"}'
```

```json
{ "accessToken": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiresIn": 7200 }
```

Conteúdo do token:

```json
{
  "iss": "fiap-bank-plataforma-credito",
  "sub": "98765432100",
  "iat": 1793664000,
  "exp": 1793671200,
  "cpf": "98765432100",
  "conta": "78901-2",
  "roles": ["CLIENTE"],
  "scope": "ROLE_CLIENTE",
  "email": "cliente@fiapbank.com",
  "perfil": "CLIENTE"
}
```

### Usar o token

```bash
TOKEN="cole-aqui-o-accessToken"

# 200: o cliente consulta as próprias solicitações
curl -i "http://localhost:8081/api/v1/solicitacoes?cpf=98765432100" \
  -H "Authorization: Bearer $TOKEN"

# 201: o cliente solicita crédito para o próprio CPF
curl -i -X POST http://localhost:8081/api/v1/solicitacoes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cpfCliente":"98765432100","nomeCliente":"Carlos Eduardo Oliveira","tipoCredito":"PESSOAL","valorSolicitado":5000.00,"quantidadeParcelas":12,"rendaMensal":4500.00}'

# 403: o cliente não pode listar todas as solicitações
curl -i http://localhost:8081/api/v1/solicitacoes \
  -H "Authorization: Bearer $TOKEN"

# 401: requisição sem token
curl -i http://localhost:8081/api/v1/solicitacoes
```

Com o token de um operador:

```bash
curl -i -X PUT http://localhost:8081/api/v1/solicitacoes/1/aprovacao \
  -H "Authorization: Bearer $TOKEN_OPERADOR" \
  -H "Content-Type: application/json" \
  -d '{"parecerOperador":"Renda verificada"}'
```

---

## 📡 Principais Endpoints

### Plataforma de Crédito

| Método | Rota | Acesso |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Público |
| `GET` | `/h2-console/**` | Público |
| `POST` | `/api/v1/solicitacoes` | CLIENTE (próprio CPF), OPERADOR, ADMIN |
| `GET` | `/api/v1/solicitacoes?cpf={cpf}` | CLIENTE (próprio CPF), OPERADOR, ADMIN |
| `GET` | `/api/v1/contas/cliente/{cpf}` | CLIENTE (próprio CPF), OPERADOR, ADMIN |
| `GET` | `/api/v1/solicitacoes` | OPERADOR, ADMIN |
| `GET` | `/api/v1/solicitacoes/{id}` | OPERADOR, ADMIN |
| `PUT` | `/api/v1/solicitacoes/{id}/aprovacao` | OPERADOR, ADMIN |
| `PUT` | `/api/v1/solicitacoes/{id}/reprovacao` | OPERADOR, ADMIN |

As rotas legadas `POST /api/credito/*` (`solicitarCredito`, `consultarStatusCredito`, `consultarClienteCore`, `buscarSolicitacaoPorId`, `listarTodasSolicitacoes`, `aprovarSolicitacao`, `reprovarSolicitacao`) seguem disponíveis, com as mesmas proteções. A Mesa as utiliza.

### Core Bancário

`/api/v1/clientes` e `/api/v1/contas`, incluindo `GET /api/v1/contas/cliente/{cpf}`. O enunciado do CP6 restringe a transformação em Resource Server à Plataforma: **o Core não exige autenticação** e deve ficar em rede interna.

---

## ✅ Testes

### Testes automatizados

| Projeto | Comando | Testes | Resultado |
| :--- | :--- | ---: | :--- |
| Core Bancário | `mvn clean test` | 61 | SUCCESS |
| Plataforma de Crédito | `mvn clean test` | 64 | SUCCESS |
| Mesa de Operações | `mvn clean test` | 16 | SUCCESS |
| Internet Banking Mobile | `npx ng test --watch=false --browsers=ChromeHeadless` | 16 | SUCCESS |
| **Total** | | **157** | **0 falhas** |

Em `SecurityIntegrationTest`, a cadeia de segurança real cobre emissão de claims, token ausente, malformado, expirado, de outro emissor ou com outra chave, 403 e as operações de cada perfil.

### Validação integrada

Em 02/11/2026, com Core, Plataforma e Mesa em execução e o Angular em um Chrome real, foram validados:

- login e claims do token para os três perfis;
- recusa de token ausente, inválido, expirado, de outro emissor, com outra chave, com `alg=none` ou com payload adulterado;
- 403 e operações permitidas para CLIENTE, OPERADOR e ADMIN;
- Bearer Token enviado pelo Angular, observado nas requisições de rede;
- aprovação e reprovação na Mesa, confirmadas na Plataforma;
- ausência de `JSESSIONID`, H2 Console e CORS;
- comportamento com Core ou Plataforma fora do ar.

A validação usou portas alternativas (18080 a 18082), pois as portas padrão estavam ocupadas naquele momento.

---

## ⚠️ Observações

- O extrato do Angular é **demonstrativo** e não representa transações reais.
- Saldo, agência e conta do Angular vêm do Core Bancário. A aprovação de crédito não altera o saldo.
- O nome exibido no Angular é um valor padrão editável, pois a conta do Core não informa o nome do titular.
- Os bancos H2 são em memória: os dados voltam ao estado inicial a cada reinício.
- Usuários e senhas de demonstração existem apenas no perfil `dev`.

---

## 📦 Informações de Entrega

- **GitHub:** monorepo com as quatro aplicações.
- **Microsoft Teams:** pasta do projeto em `.zip` (outros formatos não são aceitos), com o nome `RM562272_Arthur_Tenorio_CP6.zip`, e o link do GitHub, anexados pelo líder do grupo na tarefa correspondente.

---

**FIAP Bank — Esteira de Crédito**
