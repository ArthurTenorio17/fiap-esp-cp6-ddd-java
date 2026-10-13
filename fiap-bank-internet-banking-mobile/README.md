# FIAP Bank - Internet Banking Mobile 📱

Aplicação Web Mobile responsiva (**Mobile-First**) do cliente final do **FIAP Bank**, desenvolvida em **Angular 17+**. 

Permite que os clientes consultem seu saldo e dados da conta, simulem propostas de crédito em tempo real (Empréstimo Pessoal, Financiamento Imobiliário, Crédito Automotivo e Consignado), enviem propostas para a esteira da Plataforma de Crédito e acompanhem o status de suas solicitações.

---

## 🛠️ Tecnologias Utilizadas

- **Angular 17.3+** (Standalone Components)
- **TypeScript 5.4+**
- **RxJS 7.8+**
- **HTML5 & CSS3** (layout responsivo com estética premium e fluida para smartphone)
- **Node.js / npm**

---

## 📋 Pré-requisitos

- **Node.js** (versão 18.x LTS ou 20.x LTS recomendada).
- **npm** (gerenciador de pacotes, incluído com o Node.js).
- *(Recomendado)* Serviço **Plataforma de Crédito** em execução na porta `8081`.

---

## 🚀 Como Executar a Aplicação

### 1. Instalar as dependências

Navegue até o diretório do projeto e execute:

```bash
cd fiap-bank-internet-banking-mobile
npm install
```

### 2. Iniciar o servidor de desenvolvimento

Execute o comando:

```bash
npm run start
```
*(ou se possuir o Angular CLI globalmente: `ng serve`)*

A aplicação estará disponível em: **`http://localhost:4200`**

O servidor inclui suporte a *Hot Reloading*, atualizando automaticamente a página ao salvar alterações no código.

---

## ⚙️ Configuração de Endpoints

A URL da API da **Plataforma de Crédito** é configurada em:

1. **Tempo de Execução (Runtime):** `src/assets/config/app-config.json`
   ```json
   {
     "plataformaCreditoUrl": "http://localhost:8081/api/credito"
   }
   ```
2. **Tempo de Compilação (Build):** `src/environments/environment.ts` (desenvolvimento) e `src/environments/environment.prod.ts` (produção).

---

## 📱 Funcionalidades da Aplicação

- **Início (Home):** Visão geral do saldo em conta, atalhos rápidos e resumo dos produtos disponíveis.
- **Simulador e Solicitação de Crédito:**
  - Escolha do tipo de linha de crédito (*Pessoal, Imobiliário, Automotivo, Consignado*).
  - Ajuste dinâmico de valor e quantidade de parcelas com cálculo automático da parcela estimada e taxas.
  - Submissão da proposta para análise na esteira da Plataforma de Crédito via chamada HTTP POST.
- **Histórico de Solicitações:** Consulta em tempo real de todas as propostas vinculadas ao CPF do cliente, com badges de status (*Aprovado, Em Análise, Reprovado*) e pareceres.
- **Extrato Bancário:** Visualização dos lançamentos e movimentações de crédito/débito.
- **Consulta ao Core:** Visualização das informações retornadas diretamente do Core Bancário via integração Feign.

---

## 📦 Build para Produção

Para gerar os artefatos estáticos otimizados para produção:

```bash
npm run build
```

Os arquivos compilados serão gerados no diretório `dist/fiap-bank-internet-banking-mobile/`.

---

## 🧪 Executar Testes Unitários

Para rodar os testes unitários com Karma e Jasmine:

```bash
npm test
```
