# FIAP Bank - Core Bancário 🏦

Microsserviço de **Core Bancário** responsável pela gestão central de clientes e contas correntes/poupança do FIAP Bank. O projeto segue os princípios de **Domain-Driven Design (DDD)** com segregação estrita em arquitetura multi-módulo Maven.

---

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.1+**
- **Spring Data JPA**
- **H2 Database** (em memória)
- **Maven** (projeto multi-módulo)

---

## 📋 Pré-requisitos

- **Java JDK 21** instalado e configurado nas variáveis de ambiente.
- **Apache Maven 3.9+** instalado.

---

## 🚀 Como Executar a Aplicação

### 1. Compilar os módulos Maven

Na raiz do diretório `fiap-bank-core-bancario`:

```bash
mvn clean install
```

### 2. Iniciar a aplicação

Você pode iniciar o serviço diretamente da raiz do módulo:

```bash
mvn -pl fiap-bank-core-bancario-presentation spring-boot:run
```

Ou navegando até o módulo de apresentação:

```bash
cd fiap-bank-core-bancario-presentation
mvn spring-boot:run
```

### 3. Executar via arquivo JAR

Após o empacotamento com `mvn clean package`:

```bash
java -jar fiap-bank-core-bancario-presentation/target/fiap-bank-core-bancario-presentation-1.0.0-SNAPSHOT.jar
```

A aplicação estará acessível em: **`http://localhost:8080`**

---

## 🗄️ Console do Banco de Dados H2

O banco de dados H2 em memória pode ser acessado pelo navegador:

- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:corebancariodb`
- **User Name:** `sa`
- **Password:** *(deixe em branco)*

---

## 📌 Dados Iniciais (Carga Automática)

Ao subir o serviço, os seguintes dados são carregados automaticamente via `DataInitializer`:

- **Cliente 1:** Ana Silva | CPF: `12345678900` | Agência: `0001` | Conta: `12345-6` | Saldo: `R$ 12.500,00`
- **Cliente 2:** Carlos Eduardo Oliveira | CPF: `98765432100` | Agência: `0001` | Conta: `78901-2` | Saldo: `R$ 45.800,50`

---

## 🌐 Endpoints RESTful Principais

### Clientes (`/api/v1/clientes`)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/v1/clientes` | Lista todos os clientes cadastrados |
| `GET` | `/api/v1/clientes/{id}` | Busca cliente por UUID |
| `GET` | `/api/v1/clientes/cpf/{cpf}` | Busca cliente por CPF |
| `POST` | `/api/v1/clientes` | Cadastra um novo cliente |
| `PUT` | `/api/v1/clientes/{id}` | Atualiza dados do cliente por UUID |
| `DELETE` | `/api/v1/clientes/{id}` | Remove um cliente por UUID |

### Contas Bancárias (`/api/v1/contas`)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/v1/contas` | Lista todas as contas bancárias |
| `GET` | `/api/v1/contas/{numeroConta}` | Busca detalhes de uma conta por número |
| `GET` | `/api/v1/contas/cliente/{cpf}` | Busca conta bancária associada a um CPF |
| `POST` | `/api/v1/contas` | Abre uma nova conta bancária para um cliente |
| `PUT` | `/api/v1/contas/{numeroConta}/saldo` | Atualiza o saldo da conta (depósito ou saque) |
| `DELETE` | `/api/v1/contas/{numeroConta}` | Encerra uma conta bancária |
