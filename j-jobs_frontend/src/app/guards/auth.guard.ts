// src/app/guards/auth.guard.ts
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
    ) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        return this.oauth2Service.isAuthenticated$.pipe(
            take(1), // prendre la première valeur
            map(isAuthenticated => {
                if (isAuthenticated) return true;

                this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
                return false;
            })
        );
    }
}
