// src/app/core/guards/permission.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { OAuth2Service } from '@services/oauth2.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionGuard implements CanActivate {
  constructor(
    private oauth2Service: OAuth2Service,
    private router: Router,
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const requiredRoles = route.data['roles'] as string[];

    // Vérif si authentifié
    if (!this.oauth2Service.isAuthenticated()) {
      this.router.navigate(['/auth/login']);
      return false;
    }

    // Si aucun rôle n'est requis => OK
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    // Vérif si l’utilisateur a au moins un rôle requis
    const hasRole = requiredRoles.some((role) =>
      this.oauth2Service.hasRole(role),
    );

    if (!hasRole) {
      this.router.navigate(['/unauthorized']);
      return false;
    }

    return true;
  }
}
