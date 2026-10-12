import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AppConfig {
  plataformaCreditoUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class AppConfigService {
  private config: AppConfig = {
    plataformaCreditoUrl: environment.plataformaCreditoUrl
  };

  constructor(private http: HttpClient) {}

  loadAppConfig(): Promise<void> {
    return firstValueFrom(this.http.get<AppConfig>('assets/config/app-config.json'))
      .then(data => {
        if (data && data.plataformaCreditoUrl) {
          this.config = data;
        }
      })
      .catch(err => {
        console.warn('Não foi possível carregar assets/config/app-config.json, utilizando fallback de environment:', err);
      });
  }

  get plataformaCreditoUrl(): string {
    return this.config.plataformaCreditoUrl || environment.plataformaCreditoUrl;
  }
}
