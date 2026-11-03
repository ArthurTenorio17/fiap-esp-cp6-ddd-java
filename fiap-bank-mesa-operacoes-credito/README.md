# FIAP Bank - Mesa de Operações de Crédito 🖥️

Front-end Web de **Mesa de Operações de Crédito** do FIAP Bank. Permite que operadores bancários e analistas de crédito monitorem o painel de propostas, analisem detalhadamente o perfil financeiro de clientes (score, restrições no SCR, dados da conta bancária) e tomem decisões de aprovação ou reprovação de crédito com registro de parecer técnico.

A comunicação com a **Plataforma de Crédito** é realizada de forma declarativa utilizando o **Spring Cloud OpenFeign** (`@FeignClient`).

---

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.1**
- **Spring Cloud OpenFeign (Spring Cloud 2025.1.2)**
- **Thymeleaf** (renderização server-side de templates HTML)
- **Spring Web**
- **Maven**

---

## 📋 Pré-requisitos

- **Java JDK 21** instalado e configurado no `PATH`.
- **Apache Maven 3.9+** instalado.
- O serviço **Plataforma de Crédito** deve estar em execução na porta `8081` para carregar e gerenciar as solicitações reais.

---

## 🚀 Como Executar a Aplicação

### 1. Compilar o projeto

Na raiz do diretório `fiap-bank-mesa-operacoes-credito`:

```bash
mvn clean compile
```

### 2. Iniciar a aplicação via Maven

```bash
mvn spring-boot:run
```

### 3. Executar via arquivo JAR

Para gerar o pacote e executar:

```bash
mvn clean package -DskipTests
java -jar target/fiap-bank-mesa-operacoes-credito-1.0.0-SNAPSHOT.jar
```

A aplicação web estará acessível no navegador em: **`http://localhost:8082`**

---

## ⚙️ Variáveis de Ambiente e Configuração

As seguintes propriedades podem ser customizadas no arquivo `src/main/resources/application.yml` ou passadas via variáveis de ambiente:

| Propriedade | Variável de Ambiente | Valor Padrão | Descrição |
| :--- | :--- | :--- | :--- |
| `server.port` | `SERVER_PORT` | `8082` | Porta HTTP da aplicação |
| `plataforma.credito.url` | `PLATAFORMA_CREDITO_URL` | `http://localhost:8081` | URL base da Plataforma de Crédito consumida via Feign |
| `mesa.auth.cookie-secure` | `MESA_COOKIE_SECURE` | `false` | Use `true` quando a Mesa for servida por HTTPS |

---

## 🔐 Autenticação e Propagação do Token

1. O operador entra em `/login`; a Mesa autentica o usuário em `POST /api/auth/login` na Plataforma.
2. O JWT fica em um cookie `HttpOnly` e `SameSite=Strict`, sem `HttpSession`.
3. O `RequestInterceptor` de `FeignClientConfig` envia `Authorization: Bearer <token>` em cada chamada à Plataforma.
4. Em 401, a Mesa limpa o cookie e volta ao login. Em 403, informa que o usuário não tem perfil de OPERADOR ou ADMIN. Em falhas de comunicação, o dashboard entra em modo offline.

Usuários de demonstração: `teste@fiapbank.com` / `Teste@123` (OPERADOR) e `admin@fiapbank.com` / `Admin@123` (ADMIN).

---

## 🖥️ Páginas e Funcionalidades

### 1. Dashboard de Operações (`/` ou `/dashboard`)
- **Cards de Métricas:** Total de solicitações, propostas em análise, aprovadas e reprovadas.
- **Filtros por Status:** Permite visualizar propostas filtradas (`TODOS`, `EM_ANALISE`, `APROVADO`, `REPROVADO`).
- **Tabela de Solicitações:** Listagem completa com ID, Nome, CPF, Tipo de Crédito, Valor Solicitado, Score, Status e botão de ação para análise.

### 2. Análise Detalhada da Proposta (`/solicitacao/{id}`)
- Exibe o resumo cadastral do cliente e condições do crédito (parcelas, renda informada).
- Apresenta o resultado do motor de risco: Score de Crédito do Bureau, restrições no SCR (Banco Central).
- Formulário para registrar o parecer do operador bancário e botões para **Aprovar** ou **Reprovar** a proposta.
