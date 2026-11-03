import { TestBed } from '@angular/core/testing';
import { LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { AppComponent } from './app.component';

registerLocaleData(localePt, 'pt-BR');

function tokenFalso(claims: Record<string, unknown>): string {
  return `h.${btoa(JSON.stringify(claims))}.s`;
}

const TOKEN_CLIENTE = tokenFalso({
  exp: Math.floor(Date.now() / 1000) + 3600,
  roles: ['CLIENTE'],
  cpf: '98765432100'
});

describe('AppComponent', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: 'pt-BR' }
      ]
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('deve exibir a tela de login sem token', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const tela = fixture.nativeElement as HTMLElement;

    expect(fixture.componentInstance.autenticado).toBeFalse();
    expect(tela.querySelector('#login-email')).toBeTruthy();
  });

  it('deve carregar a conta real do Core ao iniciar com token de cliente', () => {
    sessionStorage.setItem('access_token', TOKEN_CLIENTE);

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    http.expectOne(r => r.url.endsWith('/api/v1/contas/cliente/98765432100'))
      .flush({
        statusConta: 'ATIVA',
        saldo: 45800.5,
        agencia: '0001',
        numeroConta: '78901-2'
      });

    http.expectOne(r => r.url.endsWith('/api/v1/solicitacoes')).flush([]);

    expect(fixture.componentInstance.autenticado).toBeTrue();
    expect(fixture.componentInstance.contaInfo.saldo).toBe(45800.5);
    expect(fixture.componentInstance.contaInfo.numeroConta).toBe('78901-2');
  });

  it('deve voltar ao login quando a Plataforma responder 401', () => {
    sessionStorage.setItem('access_token', TOKEN_CLIENTE);

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    // Em produção o interceptor remove o token antes de o erro chegar aqui.
    sessionStorage.removeItem('access_token');

    http.expectOne(r => r.url.includes('/contas/cliente/'))
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(fixture.componentInstance.autenticado).toBeFalse();
    expect(fixture.componentInstance.erroLogin).toContain('expirada');
    expect(sessionStorage.getItem('access_token')).toBeNull();
  });

  it('não deve apagar o que foi digitado quando chega um segundo 401', () => {
    sessionStorage.setItem('access_token', TOKEN_CLIENTE);

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const app = fixture.componentInstance;

    sessionStorage.removeItem('access_token');

    http.expectOne(r => r.url.includes('/contas/cliente/'))
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    app.emailLogin = 'digitando@fiapbank.com';

    http.expectOne(r => r.url.endsWith('/api/v1/solicitacoes'))
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(app.emailLogin).toBe('digitando@fiapbank.com');
  });

  it('resposta 401 atrasada não deve derrubar um login mais recente', () => {
    sessionStorage.setItem('access_token', TOKEN_CLIENTE);

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    // O token continua válido: o 401 pertence a uma requisição anterior.
    http.expectOne(r => r.url.includes('/contas/cliente/'))
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(fixture.componentInstance.autenticado).toBeTrue();
    expect(sessionStorage.getItem('access_token')).toBe(TOKEN_CLIENTE);
  });

  it('deve informar falta de permissão em 403 sem encerrar a sessão', () => {
    sessionStorage.setItem('access_token', TOKEN_CLIENTE);

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    http.expectOne(r => r.url.includes('/contas/cliente/'))
      .flush({}, { status: 403, statusText: 'Forbidden' });

    expect(fixture.componentInstance.autenticado).toBeTrue();
    expect(fixture.componentInstance.feedbackMessage).toContain('permissão');
  });

  it('não deve aceitar usuário sem perfil CLIENTE', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    app.emailLogin = 'teste@fiapbank.com';
    app.senhaLogin = 'senha-de-teste';
    app.fazerLogin();

    http.expectOne(r => r.url.endsWith('/api/auth/login')).flush({
      accessToken: tokenFalso({
        exp: Math.floor(Date.now() / 1000) + 3600,
        roles: ['OPERADOR'],
        cpf: '11122233344'
      }),
      tokenType: 'Bearer',
      expiresIn: 7200
    });

    expect(app.autenticado).toBeFalse();
    expect(app.erroLogin).toContain('CLIENTE');
    expect(sessionStorage.getItem('access_token')).toBeNull();
  });

  it('deve informar senha incorreta em 401 no login', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    app.emailLogin = 'cliente@fiapbank.com';
    app.senhaLogin = 'errada';
    app.fazerLogin();

    http.expectOne(r => r.url.endsWith('/api/auth/login'))
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(app.erroLogin).toBe('E-mail ou senha incorretos.');
  });
});
