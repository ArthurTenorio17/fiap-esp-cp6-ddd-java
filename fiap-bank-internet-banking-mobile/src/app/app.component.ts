import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { CreditoService } from './services/credito.service';
import { AuthService } from './services/auth.service';

import {
  ClienteCoreInfo,
  SolicitacaoCreditoResponse,
  TransacaoExtrato
} from './models/credito.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {

  autenticado = false;
  emailLogin = '';
  senhaLogin = '';
  erroLogin: string | null = null;
  carregandoLogin = false;

  activeTab: 'home' | 'credito' | 'extrato' | 'historico' | 'core' = 'home';

  hideBalance = false;
  loading = false;

  feedbackMessage: string | null = null;
  feedbackType: 'success' | 'warning' | 'error' = 'success';

  cpfCliente = '';
  nomeCliente = 'Cliente FIAP Bank';
  rendaMensal = 7500;

  // Preenchido pela consulta ao Core Bancário após o login.
  contaInfo: ClienteCoreInfo = this.contaNaoCarregada();

  selectedTipoCredito = 'EMPRÉSTIMO PESSOAL';
  valorSolicitado = 15000;
  quantidadeParcelas = 24;

  tiposCredito = [
    {
      id: 'EMPRÉSTIMO PESSOAL',
      nome: 'Empréstimo Pessoal',
      taxa: '1.49% a.m.',
      icone: 'fa-user-tag'
    },
    {
      id: 'CRÉDITO IMOBILIÁRIO',
      nome: 'Crédito Imobiliário',
      taxa: '0.85% a.m.',
      icone: 'fa-house-chimney'
    },
    {
      id: 'CRÉDITO AUTOMOTIVO',
      nome: 'Financiamento Auto',
      taxa: '1.19% a.m.',
      icone: 'fa-car'
    },
    {
      id: 'CONSIGNADO',
      nome: 'Consignado FIAP',
      taxa: '0.99% a.m.',
      icone: 'fa-file-signature'
    }
  ];

  solicitacoes: SolicitacaoCreditoResponse[] = [];

  filtroExtrato: 'TODAS' | 'ENTRADAS' | 'SAIDAS' = 'TODAS';

  // Extrato demonstrativo.
  transacoes: TransacaoExtrato[] = [
    {
      id: 'TX-901',
      descricao: 'Pix Recebido - Maria Santos',
      tipo: 'ENTRADA',
      categoria: 'PIX',
      valor: 2500,
      data: new Date(Date.now() - 7200000).toISOString(),
      icone: 'fa-qrcode text-emerald'
    },
    {
      id: 'TX-902',
      descricao: 'Pagamento Fatura FIAP Card',
      tipo: 'SAIDA',
      categoria: 'PAGAMENTO',
      valor: 450,
      data: new Date(Date.now() - 64800000).toISOString(),
      icone: 'fa-credit-card text-danger'
    },
    {
      id: 'TX-903',
      descricao: 'Crédito Pessoal Aprovado FIAP Bank',
      tipo: 'ENTRADA',
      categoria: 'CREDITO',
      valor: 15000,
      data: new Date(Date.now() - 172800000).toISOString(),
      icone: 'fa-hand-holding-dollar text-primary'
    },
    {
      id: 'TX-904',
      descricao: 'Transferência Enviada - João Pereira',
      tipo: 'SAIDA',
      categoria: 'TRANSFERENCIA',
      valor: 120.50,
      data: new Date(Date.now() - 259200000).toISOString(),
      icone: 'fa-arrow-up-right-from-square text-amber'
    }
  ];

  constructor(
    private creditoService: CreditoService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.iniciarSessaoCliente();
    }
  }

  fazerLogin(): void {
    if (!this.emailLogin.trim() || !this.senhaLogin) {
      this.erroLogin = 'Informe e-mail e senha.';
      return;
    }

    this.carregandoLogin = true;
    this.erroLogin = null;

    this.authService.login(
      this.emailLogin.trim(),
      this.senhaLogin
    ).subscribe({
      next: () => {
        this.carregandoLogin = false;
        this.senhaLogin = '';

        if (!this.iniciarSessaoCliente()) {
          this.authService.logout();
          this.erroLogin =
            'Este acesso exige um usuário CLIENTE com CPF válido.';
        }
      },

      error: (err) => {
        this.carregandoLogin = false;

        this.erroLogin = err.status === 401
          ? 'E-mail ou senha incorretos.'
          : err.status === 400
            ? 'Informe um e-mail válido.'
            : 'Plataforma de Crédito indisponível. Tente novamente.';
      }
    });
  }

  private contaNaoCarregada(): ClienteCoreInfo {
    return {
      statusConta: 'INDISPONÍVEL',
      saldo: 0,
      agencia: '----',
      numeroConta: '----'
    };
  }

  private encerrarSessaoExpirada(): void {
    // Consultas paralelas podem receber 401 juntas: só a primeira encerra a
    // sessão, e uma resposta atrasada não derruba um login mais recente.
    if (!this.autenticado || this.authService.isAuthenticated()) {
      return;
    }

    this.sair();
    this.erroLogin = 'Sessão expirada. Entre novamente.';
  }

  // Retorna a mensagem para a tela; em 401, volta ao login.
  private mensagemErroHttp(
    err: HttpErrorResponse,
    mensagemPadrao: string
  ): string {
    if (err.status === 401) {
      this.encerrarSessaoExpirada();
      return '';
    }

    if (err.status === 403) {
      return 'Você não tem permissão para esta operação.';
    }

    if (err.status === 0) {
      return 'Plataforma de Crédito indisponível. Tente novamente.';
    }

    return err.error?.message || mensagemPadrao;
  }

  private iniciarSessaoCliente(): boolean {
    const dados = this.authService.dadosCliente();

    if (!dados) {
      return false;
    }

    this.cpfCliente = dados.cpf;
    this.autenticado = true;
    this.carregarDadosCore();
    this.carregarSolicitacoes();

    return true;
  }

  sair(): void {
    this.authService.logout();

    this.autenticado = false;
    this.emailLogin = '';
    this.senhaLogin = '';
    this.erroLogin = null;

    this.cpfCliente = '';
    this.contaInfo = this.contaNaoCarregada();
    this.solicitacoes = [];
    this.feedbackMessage = null;
    this.activeTab = 'home';
  }

  carregarDadosCore(): void {
    this.creditoService.consultarCoreBancario(
      this.cpfCliente
    ).subscribe({
      next: (info) => {
        if (info && info.saldo !== undefined) {
          this.contaInfo = info;
        }
      },

      error: (err: HttpErrorResponse) => {
        const mensagem = this.mensagemErroHttp(
          err,
          'Não foi possível consultar a conta no Core Bancário.'
        );

        if (mensagem) {
          this.feedbackType = 'warning';
          this.feedbackMessage = mensagem;
        }
      }
    });
  }

  carregarSolicitacoes(): void {
    if (!this.autenticado || !this.cpfCliente) {
      return;
    }

    this.loading = true;

    this.creditoService.consultarStatus(
      this.cpfCliente
    ).subscribe({
      next: (data) => {
        this.solicitacoes = data || [];
        this.loading = false;
      },

      error: (err: HttpErrorResponse) => {
        this.solicitacoes = [];
        this.loading = false;

        const mensagem = this.mensagemErroHttp(
          err,
          'Não foi possível consultar suas solicitações.'
        );

        if (mensagem) {
          this.feedbackType = 'error';
          this.feedbackMessage = mensagem;
        }
      }
    });
  }

  get transacoesFiltradas(): TransacaoExtrato[] {
    if (this.filtroExtrato === 'ENTRADAS') {
      return this.transacoes.filter(
        t => t.tipo === 'ENTRADA'
      );
    }

    if (this.filtroExtrato === 'SAIDAS') {
      return this.transacoes.filter(
        t => t.tipo === 'SAIDA'
      );
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
    if (this.quantidadeParcelas <= 0) {
      return 0;
    }

    const taxa = this.getTaxaDecimal();
    const valor = this.valorSolicitado;
    const parcelas = this.quantidadeParcelas;

    if (taxa === 0) {
      return valor / parcelas;
    }

    return (
      valor * taxa * Math.pow(1 + taxa, parcelas)
    ) / (
      Math.pow(1 + taxa, parcelas) - 1
    );
  }

  get totalPagar(): number {
    return this.parcelaEstimada * this.quantidadeParcelas;
  }

  getTaxaDecimal(): number {
    switch (this.selectedTipoCredito) {
      case 'CRÉDITO IMOBILIÁRIO':
        return 0.0085;

      case 'CRÉDITO AUTOMOTIVO':
        return 0.0119;

      case 'CONSIGNADO':
        return 0.0099;

      default:
        return 0.0149;
    }
  }

  solicitarCredito(): void {
    if (!this.autenticado) {
      this.feedbackType = 'error';
      this.feedbackMessage = 'Faça login para continuar.';
      return;
    }

    if (
      !this.valorSolicitado ||
      this.valorSolicitado <= 0 ||
      !Number.isInteger(this.quantidadeParcelas) ||
      this.quantidadeParcelas <= 0
    ) {
      this.feedbackType = 'error';
      this.feedbackMessage =
        'Informe um valor e uma quantidade de parcelas válidos.';
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
          this.feedbackMessage =
            `Crédito de R$ ${this.valorSolicitado.toLocaleString('pt-BR')} aprovado!`;

        } else if (res.status === 'EM_ANALISE') {
          this.feedbackType = 'warning';
          this.feedbackMessage =
            'Solicitação encaminhada para a Mesa de Operações.';

        } else {
          this.feedbackType = 'error';
          this.feedbackMessage =
            `Solicitação não aprovada: ${res.parecerOperador}`;
        }

        this.carregarSolicitacoes();

        setTimeout(() => {
          if (this.autenticado) {
            this.activeTab = 'historico';
          }
        }, 1500);
      },

      error: (err: HttpErrorResponse) => {
        this.loading = false;

        const mensagem = this.mensagemErroHttp(
          err,
          'Erro ao processar solicitação na Plataforma de Crédito.'
        );

        if (mensagem) {
          this.feedbackType = 'error';
          this.feedbackMessage = mensagem;
        }
      }
    });
  }

  selectTipo(tipo: string): void {
    this.selectedTipoCredito = tipo;
  }

  toggleBalanceVisibility(): void {
    this.hideBalance = !this.hideBalance;
  }

  setTab(
    tab: 'home' | 'credito' | 'extrato' | 'historico' | 'core'
  ): void {
    this.activeTab = tab;

    if (tab === 'historico') {
      this.carregarSolicitacoes();
    }
  }
}
