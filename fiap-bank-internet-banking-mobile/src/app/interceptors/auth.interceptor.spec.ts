import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { authInterceptor } from './auth.interceptor';
import { AppConfigService } from '../services/app-config.service';

describe('authInterceptor', () => {
  const API = 'http://localhost:8081';

  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        {
          provide: AppConfigService,
          useValue: { plataformaCreditoUrl: `${API}/api/credito` }
        }
      ]
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
    sessionStorage.clear();
  });

  it('deve anexar o Bearer token às rotas protegidas', () => {
    sessionStorage.setItem('access_token', 'abc');

    http.get(`${API}/api/v1/solicitacoes`).subscribe();

    const req = controller.expectOne(`${API}/api/v1/solicitacoes`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush([]);
  });

  it('deve anexar o Bearer token às rotas legadas', () => {
    sessionStorage.setItem('access_token', 'abc');

    http.post(`${API}/api/credito/solicitarCredito`, {}).subscribe();

    const req = controller.expectOne(`${API}/api/credito/solicitarCredito`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush({});
  });

  it('não deve enviar o token no login', () => {
    sessionStorage.setItem('access_token', 'abc');

    http.post(`${API}/api/auth/login`, {}).subscribe();

    const req = controller.expectOne(`${API}/api/auth/login`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('não deve vazar o token para outras origens', () => {
    sessionStorage.setItem('access_token', 'abc');

    http.get('https://outro-site.com/api/v1/solicitacoes').subscribe();

    const req = controller.expectOne('https://outro-site.com/api/v1/solicitacoes');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('não deve enviar header sem token', () => {
    http.get(`${API}/api/v1/solicitacoes`).subscribe();

    const req = controller.expectOne(`${API}/api/v1/solicitacoes`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush([]);
  });

  it('deve descartar o token ao receber 401 e repassar o erro', () => {
    sessionStorage.setItem('access_token', 'abc');

    let status = 0;

    http.get(`${API}/api/v1/solicitacoes`).subscribe({
      error: (erro) => { status = erro.status; }
    });

    controller.expectOne(`${API}/api/v1/solicitacoes`)
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(status).toBe(401);
    expect(sessionStorage.getItem('access_token')).toBeNull();
  });

  it('401 atrasado não deve apagar o token de um novo login', () => {
    sessionStorage.setItem('access_token', 'antigo');

    http.get(`${API}/api/v1/solicitacoes`).subscribe({ error: () => {} });

    sessionStorage.setItem('access_token', 'novo');

    controller.expectOne(`${API}/api/v1/solicitacoes`)
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(sessionStorage.getItem('access_token')).toBe('novo');
  });

  it('deve manter o token ao receber 403', () => {
    sessionStorage.setItem('access_token', 'abc');

    http.get(`${API}/api/v1/solicitacoes`).subscribe({ error: () => {} });

    controller.expectOne(`${API}/api/v1/solicitacoes`)
      .flush(null, { status: 403, statusText: 'Forbidden' });

    expect(sessionStorage.getItem('access_token')).toBe('abc');
  });
});
