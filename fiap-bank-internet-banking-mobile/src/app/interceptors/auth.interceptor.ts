import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AppConfigService } from '../services/app-config.service';

export const authInterceptor: HttpInterceptorFn = (
  request,
  next
) => {
  const config = inject(AppConfigService);

  const token = sessionStorage.getItem('access_token');

  const apiOrigin = new URL(
    config.plataformaCreditoUrl
  ).origin;

  const url = new URL(
    request.url,
    window.location.origin
  );

  // O token só é enviado à Plataforma de Crédito, nunca a outras origens.
  const protegida =
    url.origin === apiOrigin &&
    (
      url.pathname.startsWith('/api/v1/') ||
      url.pathname.startsWith('/api/credito/')
    );

  if (!token || !protegida) {
    return next(request);
  }

  const authenticatedRequest = request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authenticatedRequest).pipe(
    catchError((erro: HttpErrorResponse) => {
      // 401: o token foi recusado, não adianta reenviá-lo. Uma resposta atrasada
      // não pode apagar o token de um login mais recente.
      if (
        erro.status === 401 &&
        sessionStorage.getItem('access_token') === token
      ) {
        sessionStorage.removeItem('access_token');
      }

      return throwError(() => erro);
    })
  );
};
