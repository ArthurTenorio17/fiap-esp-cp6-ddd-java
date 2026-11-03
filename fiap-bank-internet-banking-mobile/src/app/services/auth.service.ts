import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AppConfigService } from './app-config.service';

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(
    private http: HttpClient,
    private appConfig: AppConfigService
  ) {}

  login(email: string, senha: string): Observable<LoginResponse> {
    const origin = new URL(
      this.appConfig.plataformaCreditoUrl
    ).origin;

    return this.http.post<LoginResponse>(
      `${origin}/api/auth/login`,
      { email, senha }
    ).pipe(
      tap(response => {
        sessionStorage.setItem(
          'access_token',
          response.accessToken
        );
      })
    );
  }

  getToken(): string | null {
    return sessionStorage.getItem('access_token');
  }

  logout(): void {
    sessionStorage.removeItem('access_token');
  }

  private claims(): Record<string, unknown> | null {
    const token = this.getToken();

    if (!token) {
      return null;
    }

    try {
      const payload = token.split('.')[1];

      if (!payload) {
        return null;
      }

      const base64 = payload
        .replace(/-/g, '+')
        .replace(/_/g, '/');

      return JSON.parse(
        atob(base64)
      ) as Record<string, unknown>;

    } catch {
      return null;
    }
  }

  isAuthenticated(): boolean {
    const claims = this.claims();

    return !!claims &&
      typeof claims['exp'] === 'number' &&
      claims['exp'] * 1000 > Date.now();
  }

  dadosCliente(): { cpf: string } | null {
    if (!this.isAuthenticated()) {
      return null;
    }

    const claims = this.claims();
    const roles = claims?.['roles'];
    const cpf = claims?.['cpf'];

    if (
      !Array.isArray(roles) ||
      !roles.includes('CLIENTE') ||
      typeof cpf !== 'string'
    ) {
      return null;
    }

    const cpfLimpo = cpf.replace(/\D/g, '');

    return cpfLimpo.length === 11
      ? { cpf: cpfLimpo }
      : null;
  }
}
