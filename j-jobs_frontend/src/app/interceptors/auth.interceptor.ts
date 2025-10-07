// src/app/interceptors/auth.interceptor.ts
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { OAuth2Service } from '@app/services/oauth2.service';
import { catchError, Observable, switchMap, throwError, EMPTY } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;

  constructor(private oauth2Service: OAuth2Service) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler,
  ): Observable<HttpEvent<any>> {
    console.log('[AuthInterceptor] intercepting:', req.url);
    if (this.isExcluded(req.url)) {
      return next.handle(req);
    }

    const token = this.oauth2Service.getToken();
    const authReq = token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 403 && !this.isRefreshing) {
          this.isRefreshing = true;

          return this.oauth2Service.refreshToken().pipe(
            switchMap((res) => {
              this.isRefreshing = false;
              const newToken = res.access_token;
              const retryReq = req.clone({
                setHeaders: { Authorization: `Bearer ${newToken}` },
              });
              return next.handle(retryReq);
            }),
            catchError((refreshErr) => {
              this.isRefreshing = false;
              return EMPTY;
            }),
          );
        }
        return throwError(() => error);
      }),
    );
  }

  private isExcluded(url: string): boolean {
    return (
      url.includes('/authorize/token') ||
      url.includes('/authorize/refresh') ||
      url.includes('/authorize/cleanup')
    );
  }
}
