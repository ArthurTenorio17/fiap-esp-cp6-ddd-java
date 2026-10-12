import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreditoService } from './services/credito.service';
import { ClienteCoreInfo, SolicitacaoCreditoResponse, TransacaoExtrato } from './models/credito.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  activeTab: 'home' | 'credito' | 'extrato' | 'historico' | 'core' = 'home';
  hideBalance: boolean = false;
  loading: boolean = false;
  feedbackMessage: string | null = null;
  feedbackType: 'success' | 'warning' | 'error' = 'success';

  // Customer State
  cpfCliente: string = '12345678900';
  nomeCliente: string = 'Ana Silva';
  rendaMensal: number = 7500;

  // Core Bank Account Info
  contaInfo: ClienteCoreInfo = {
    statusConta: 'ATIVA',
    saldo: 12500.00,
    agencia: '0001',
    numeroConta: '12345-6'
  };

  // Form Credit Request State
  selectedTipoCredito: string = 'EMPRÉSTIMO PESSOAL';
  valorSolicitado: number = 15000;
  quantidadeParcelas: number = 24;

  // Credit Types List
  tiposCredito = [
    { id: 'EMPRÉSTIMO PESSOAL', nome: 'Empréstimo Pessoal', taxa: '1.49% a.m.', icone: 'fa-user-tag' },
    { id: 'CRÉDITO IMOBILIÁRIO', nome: 'Crédito Imobiliário', taxa: '0.85% a.m.', icone: 'fa-house-chimney' },
    { id: 'CRÉDITO AUTOMOTIVO', nome: 'Financiamento Auto', taxa: '1.19% a.m.', icone: 'fa-car' },
    { id: 'CONSIGNADO', nome: 'Consignado FIAP', taxa: '0.99% a.m.', icone: 'fa-file-signature' }
  ];

  // Solicitacoes List
  solicitacoes: SolicitacaoCreditoResponse[] = [];

  // Extrato de Movimentacoes
  filtroExtrato: 'TODAS' | 'ENTRADAS' | 'SAIDAS' = 'TODAS';
  transacoes: TransacaoExtrato[] = [
    {
      id: 'TX-901',
      descricao: 'Pix Recebido - Maria Santos',
      tipo: 'ENTRADA',
      categoria: 'PIX',
      valor: 2500.00,
      data: new Date(Date.now() - 3600000 * 2).toISOString(),
      icone: 'fa-qrcode text-emerald'
    },
    {
      id: 'TX-902',
      descricao: 'Pagamento Fatura FIAP Card',
      tipo: 'SAIDA',
      categoria: 'PAGAMENTO',
      valor: 450.00,
      data: new Date(Date.now() - 3600000 * 18).toISOString(),
      icone: 'fa-credit-card text-danger'
    },
    {
      id: 'TX-903',
      descricao: 'Crédito Pessoal Aprovado FIAP Bank',
      tipo: 'ENTRADA',
      categoria: 'CREDITO',
      valor: 15000.00,
      data: new Date(Date.now() - 3600000 * 48).toISOString(),
      icone: 'fa-hand-holding-dollar text-primary'
    },
    {
      id: 'TX-904',
      descricao: 'Transferência Enviada - João Pereira',
      tipo: 'SAIDA',
      categoria: 'TRANSFERENCIA',
      valor: 120.50,
      data: new Date(Date.now() - 3600000 * 72).toISOString(),
      icone: 'fa-arrow-up-right-from-square text-amber'
    }
  ];

  constructor(private creditoService: CreditoService) {}

  ngOnInit(): void {
    this.carregarDadosCore();
    this.carregarSolicitacoes();
  }

  carregarDadosCore(): void {
    this.creditoService.consultarCoreBancario(this.cpfCliente).subscribe({
      next: (info) => {
        if (info && info.saldo !== undefined) {
          this.contaInfo = info;
        }
      },
      error: (err) => {
        console.error('Erro ao consultar dados no Core Bancário via Plataforma:', err);
      }
    });
  }

  carregarSolicitacoes(): void {
    this.loading = true;
    this.creditoService.consultarStatus(this.cpfCliente).subscribe({
      next: (data) => {
        this.solicitacoes = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao buscar histórico de solicitações:', err);
        this.solicitacoes = [];
        this.loading = false;
      }
    });
  }

  get transacoesFiltradas(): TransacaoExtrato[] {
    if (this.filtroExtrato === 'ENTRADAS') {
      return this.transacoes.filter(t => t.tipo === 'ENTRADA');
    }
    if (this.filtroExtrato === 'SAIDAS') {
      return this.transacoes.filter(t => t.tipo === 'SAIDA');
    }
    return this.transacoes;
  }

  get totalEntradas(): number {
    return this.transacoes
      .filter(t => t.tipo === 'ENTRADA')
      .reduce((acc, t) => acc + t.valor, 0);
  }

  get totalSaidas(): number {
    return this.transacoes
      .filter(t => t.tipo === 'SAIDA')
      .reduce((acc, t) => acc + t.valor, 0);
  }

  get parcelaEstimada(): number {
    if (!this.quantidadeParcelas || this.quantidadeParcelas <= 0) return 0;
    const taxaMes = this.getTaxaDecimal();
    const pv = this.valorSolicitado;
    const n = this.quantidadeParcelas;
    if (taxaMes === 0) return pv / n;
    return (pv * taxaMes * Math.pow(1 + taxaMes, n)) / (Math.pow(1 + taxaMes, n) - 1);
  }

  get totalPagar(): number {
    return this.parcelaEstimada * this.quantidadeParcelas;
  }

  getTaxaDecimal(): number {
    switch (this.selectedTipoCredito) {
      case 'CRÉDITO IMOBILIÁRIO': return 0.0085;
      case 'CRÉDITO AUTOMOTIVO': return 0.0119;
      case 'CONSIGNADO': return 0.0099;
      default: return 0.0149;
    }
  }

  solicitarCredito(): void {
    if (!this.valorSolicitado || this.valorSolicitado <= 0) {
      this.feedbackType = 'error';
      this.feedbackMessage = 'Por favor, informe um valor desejado válido para o crédito.';
      return;
    }

    this.loading = true;
    this.feedbackMessage = null;

    const payload = {
      cpfCliente: this.cpfCliente,
      nomeCliente: this.nomeCliente,
      tipoCredito: this.selectedTipoCredito,
      valorSolicitado: this.valorSolicitado,
      quantidadeParcelas: this.quantidadeParcelas,
      rendaMensal: this.rendaMensal
    };

    this.creditoService.solicitarCredito(payload).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.status === 'APROVADO') {
          this.feedbackType = 'success';
          this.feedbackMessage = `Crédito de R$ ${this.valorSolicitado.toLocaleString('pt-BR')} APROVADO com sucesso!`;
          
          // Adiciona lancamento no extrato
          this.transacoes.unshift({
            id: `TX-${Math.floor(Math.random() * 900) + 100}`,
            descricao: `Crédito ${res.tipoCredito} Aprovado`,
            tipo: 'ENTRADA',
            categoria: 'CREDITO',
            valor: res.valorSolicitado,
            data: new Date().toISOString(),
            icone: 'fa-hand-holding-dollar text-primary'
          });
          this.contaInfo.saldo += res.valorSolicitado;
        } else if (res.status === 'EM_ANALISE') {
          this.feedbackType = 'warning';
          this.feedbackMessage = `Sua solicitação de R$ ${this.valorSolicitado.toLocaleString('pt-BR')} foi encaminhada para a Mesa de Operações.`;
        } else {
          this.feedbackType = 'error';
          this.feedbackMessage = `Solicitação não aprovada: ${res.parecerOperador}`;
        }
        this.carregarSolicitacoes();
        setTimeout(() => this.activeTab = 'historico', 1500);
      },
      error: (err) => {
        this.loading = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Erro ao processar solicitação na Plataforma de Crédito. Verifique se o servidor backend está online.';
        console.error('Erro na solicitação de crédito:', err);
      }
    });
  }

  selectTipo(tipo: string): void {
    this.selectedTipoCredito = tipo;
  }

  toggleBalanceVisibility(): void {
    this.hideBalance = !this.hideBalance;
  }

  setTab(tab: 'home' | 'credito' | 'extrato' | 'historico' | 'core'): void {
    this.activeTab = tab;
    if (tab === 'historico') {
      this.carregarSolicitacoes();
    }
  }
}
