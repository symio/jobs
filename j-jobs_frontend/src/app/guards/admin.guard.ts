// core/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { OAuth2Service } from '@services/oauth2.service';
import { Observable, combineLatest, map, take } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
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
    const isAdminSync = this.oauth2Service.isAdmin();
    
    if (isAuthSync && isAdminSync) {
      return true;
    }
    
    // Si pas authentifié ou pas admin en synchrone, vérifier avec les observables
    return combineLatest([
      this.oauth2Service.isAuthenticated$,
      this.oauth2Service.userInfo$
    ]).pipe(
      take(1), // Prendre seulement la première valeur
      map(([isAuthenticated, userInfo]) => {
        const isAdmin = userInfo?.isAdmin || false;
        
        if (isAuthenticated && isAdmin) {
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
