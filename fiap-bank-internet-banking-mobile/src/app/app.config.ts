
import {
  ApplicationConfig,
  APP_INITIALIZER,
  LOCALE_ID
} from '@angular/core';

import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';

import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';
import { AppConfigService } from './services/app-config.service';

// Registra o formato brasileiro para datas, números e moedas.
registerLocaleData(localePt, 'pt-BR');

export function initAppConfig(appConfigService: AppConfigService) {
  return () => appConfigService.loadAppConfig();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),

    // Define o português brasileiro como padrão da aplicação.
    {
      provide: LOCALE_ID,
      useValue: 'pt-BR'
    },

    // Mantém o carregamento das configurações da API.
    {
      provide: APP_INITIALIZER,
      useFactory: initAppConfig,
      deps: [AppConfigService],
      multi: true
    }
  ]
};
