import { Injectable } from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import { Observable } from 'rxjs';

import {
  ClienteCoreInfo,
  SolicitacaoCreditoResponse,
  SolicitarCreditoPayload
} from '../models/credito.model';

import { AppConfigService } from './app-config.service';

@Injectable({
  providedIn: 'root'
})
export class CreditoService {

  constructor(
    private http: HttpClient,
    private config: AppConfigService
  ) {}

  private get apiUrl(): string {
    const origin = new URL(
      this.config.plataformaCreditoUrl
    ).origin;

    return `${origin}/api/v1`;
  }

  solicitarCredito(
    payload: SolicitarCreditoPayload
  ): Observable<SolicitacaoCreditoResponse> {

    return this.http.post<SolicitacaoCreditoResponse>(
      `${this.apiUrl}/solicitacoes`,
      payload
    );
  }

  consultarStatus(
    cpf: string
  ): Observable<SolicitacaoCreditoResponse[]> {

    const cpfLimpo = cpf.replace(/\D/g, '');

    const params = new HttpParams().set(
      'cpf',
      cpfLimpo
    );

    return this.http.get<SolicitacaoCreditoResponse[]>(
      `${this.apiUrl}/solicitacoes`,
      { params }
    );
  }

  consultarCoreBancario(
    cpf: string
  ): Observable<ClienteCoreInfo> {

    const cpfLimpo = cpf.replace(/\D/g, '');

    return this.http.get<ClienteCoreInfo>(
      `${this.apiUrl}/contas/cliente/${cpfLimpo}`
    );
  }
}
