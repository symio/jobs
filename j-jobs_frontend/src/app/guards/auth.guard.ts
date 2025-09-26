// core/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { OAuth2Service } from '@services/oauth2.service';
import { Observable, map, take } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private oauth2Service: OAuth2Service,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> {
    // Vérification synchrone d'abord
    const isAuthSync = this.oauth2Service.isAuthenticated();
    
    if (isAuthSync) {
      return true;
    }
    
    // Si pas authentifié en synchrone, vérifier avec l'observable
    return this.oauth2Service.isAuthenticated$.pipe(
      take(1), // Prendre seulement la première valeur
      map(isAuthenticated => {
        if (isAuthenticated) {
          return true;
        } else {
          // Rediriger vers la page de connexion
          this.router.navigate(['/auth/login'], {
            queryParams: { returnUrl: state.url }
          });
          return false;
        }
      })
    );
  }
}
