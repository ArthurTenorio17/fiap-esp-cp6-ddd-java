import {
  ApplicationConfig,
  APP_INITIALIZER,
  LOCALE_ID
} from '@angular/core';

import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';

import { provideRouter } from '@angular/router';

import {
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';

import { routes } from './app.routes';
import { AppConfigService } from './services/app-config.service';
import { authInterceptor } from './interceptors/auth.interceptor';

registerLocaleData(localePt, 'pt-BR');

export function initAppConfig(
  config: AppConfigService
) {
  return () => config.loadAppConfig();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),

    provideHttpClient(
      withInterceptors([authInterceptor])
    ),

    {
      provide: LOCALE_ID,
      useValue: 'pt-BR'
    },

    {
      provide: APP_INITIALIZER,
      useFactory: initAppConfig,
      deps: [AppConfigService],
      multi: true
    }
  ]
};
