import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClienteCoreInfo, SolicitacaoCreditoResponse, SolicitarCreditoPayload } from '../models/credito.model';
import { AppConfigService } from './app-config.service';

@Injectable({
  providedIn: 'root'
})
export class CreditoService {

  constructor(
    private http: HttpClient,
    private appConfigService: AppConfigService
  ) {}

  private get apiUrl(): string {
    return this.appConfigService.plataformaCreditoUrl;
  }

  solicitarCredito(payload: SolicitarCreditoPayload): Observable<SolicitacaoCreditoResponse> {
    return this.http.post<SolicitacaoCreditoResponse>(`${this.apiUrl}/solicitarCredito`, payload);
  }

  consultarStatus(cpf: string): Observable<SolicitacaoCreditoResponse[]> {
    const cpfClean = cpf.replace(/\D/g, '');
    return this.http.post<SolicitacaoCreditoResponse[]>(`${this.apiUrl}/consultarStatusCredito`, { cpfCliente: cpfClean });
  }

  consultarCoreBancario(cpf: string): Observable<ClienteCoreInfo> {
    const cpfClean = cpf.replace(/\D/g, '');
    return this.http.post<ClienteCoreInfo>(`${this.apiUrl}/consultarClienteCore`, { cpfCliente: cpfClean });
  }
}
