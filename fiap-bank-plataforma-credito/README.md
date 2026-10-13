# FIAP Bank - Plataforma de Crédito 💳

Microsserviço de **Esteira e Motor de Decisão de Crédito** do FIAP Bank. Responsável por processar propostas de crédito, avaliar regras de negócio e risco, comunicar-se com o Core Bancário via **Spring Cloud OpenFeign**, simular verificações no SCR (Banco Central) e Bureau de Crédito (Serasa/SPC), e notificar clientes.

A API foi projetada com arquitetura **Non-RESTful (All POST)**, na qual todas as operações (consultas, inclusões e alterações de status) utilizam o método HTTP `POST` com verbos de ação semânticos no path.

---

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.1**
- **Spring Cloud OpenFeign 2024.0.0** (comunicação declarativa com o Core Bancário)
- **Spring Data JPA**
- **Spring Boot Actuator**
- **H2 Database** (em memória)
- **Maven**

---

## 📋 Pré-requisitos

- **Java JDK 21** instalado e configurado no `PATH`.
- **Apache Maven 3.9+** instalado.
- *(Opcional)* Serviço **Core Bancário** rodando na porta `8080` (caso indisponível, o cliente Feign utiliza dados de fallback automaticamente).

---

## 🚀 Como Executar a Aplicação

### 1. Compilar o projeto

Na raiz do diretório `fiap-bank-plataforma-credito`:

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
java -jar target/fiap-bank-plataforma-credito-1.0.0-SNAPSHOT.jar
```

A aplicação estará acessível em: **`http://localhost:8081`**

---

## ⚙️ Variáveis de Ambiente e Configuração

As seguintes propriedades podem ser customizadas no arquivo `src/main/resources/application.yml` ou passadas como variáveis de ambiente:

| Propriedade | Variável de Ambiente | Valor Padrão | Descrição |
| :--- | :--- | :--- | :--- |
| `server.port` | `SERVER_PORT` | `8081` | Porta HTTP da aplicação |
| `core.bancario.url` | `CORE_BANCARIO_URL` | `http://localhost:8080/api/v1` | URL base da API do Core Bancário |

---

## 🗄️ Console do Banco de Dados H2

- **URL:** `http://localhost:8081/h2-console`
- **JDBC URL:** `jdbc:h2:mem:plataformacreditodb`
- **User Name:** `sa`
- **Password:** *(deixe em branco)*

---

## 🌐 Endpoints Non-RESTful (All POST)

Base URL: `http://localhost:8081/api/credito`

### 1. Solicitar Crédito (Ação: `solicitarCredito`)
Envia uma nova proposta para o motor de risco de crédito.
- **Endpoint:** `POST /api/credito/solicitarCredito`
- **Exemplo de Body:**
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

### 2. Consultar Status por CPF (Ação: `consultarStatusCredito`)
Consulta todas as solicitações de um cliente informado pelo corpo da requisição.
- **Endpoint:** `POST /api/credito/consultarStatusCredito`
- **Exemplo de Body:**
```json
{
  "cpfCliente": "12345678900"
}
```

### 3. Listar Todas as Solicitações (Ação: `listarTodasSolicitacoes`)
Retorna todas as propostas para acompanhamento pela Mesa de Operações.
- **Endpoint:** `POST /api/credito/listarTodasSolicitacoes`
- **Body:** `{}` *(ou vazio)*

### 4. Buscar Solicitação por ID (Ação: `buscarSolicitacaoPorId`)
- **Endpoint:** `POST /api/credito/buscarSolicitacaoPorId`
- **Exemplo de Body:**
```json
{
  "id": 1
}
```

### 5. Aprovar Solicitação (Ação: `aprovarSolicitacao`)
- **Endpoint:** `POST /api/credito/aprovarSolicitacao`
- **Exemplo de Body:**
```json
{
  "id": 1,
  "parecerOperador": "Proposta aprovada conforme política de crédito."
}
```

### 6. Reprovar Solicitação (Ação: `reprovarSolicitacao`)
- **Endpoint:** `POST /api/credito/reprovarSolicitacao`
- **Exemplo de Body:**
```json
{
  "id": 1,
  "parecerOperador": "Margem consignável insuficiente."
}
```

### 7. Consultar Cliente no Core Bancário (Ação: `consultarClienteCore`)
Executa a chamada direta ao Core Bancário via Spring Cloud OpenFeign.
- **Endpoint:** `POST /api/credito/consultarClienteCore`
- **Exemplo de Body:**
```json
{
  "cpfCliente": "12345678900"
}
```
