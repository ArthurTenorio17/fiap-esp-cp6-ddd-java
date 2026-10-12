export interface SolicitarCreditoPayload {
  cpfCliente: string;
  nomeCliente: string;
  tipoCredito: string;
  valorSolicitado: number;
  quantidadeParcelas: number;
  rendaMensal: number;
}

export interface SolicitacaoCreditoResponse {
  id: number;
  cpfCliente: string;
  nomeCliente: string;
  tipoCredito: string;
  valorSolicitado: number;
  quantidadeParcelas: number;
  rendaMensal: number;
  scoreCredito: number;
  status: 'EM_ANALISE' | 'APROVADO' | 'REPROVADO' | 'DOCUMENTACAO_PENDENTE';
  parecerOperador: string;
  scrRestricoes: boolean;
  scoreBureau: number;
  dataSolicitacao: string;
  dataAtualizacao: string;
}

export interface ClienteCoreInfo {
  statusConta: string;
  saldo: number;
  agencia: string;
  numeroConta: string;
}

export interface TransacaoExtrato {
  id: string;
  descricao: string;
  tipo: 'ENTRADA' | 'SAIDA';
  categoria: 'PIX' | 'CREDITO' | 'TRANSFERENCIA' | 'PAGAMENTO';
  valor: number;
  data: string;
  icone: string;
}
