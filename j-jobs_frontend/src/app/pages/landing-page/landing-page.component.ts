// src/app/pages/landing-page/landing-page.component.ts
import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { OAuth2Service } from '@app/services/oauth2.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-landing-page',
    imports: [CommonModule, RouterModule],
    templateUrl: './landing-page.component.html',
    styleUrl: './landing-page.component.scss',
    standalone: true
})
export class LandingPageComponent implements OnInit, OnDestroy {

    isMenuOpen = false;
    showFooterNav = false;
    isAuthenticated = false;
    private authSubscription: Subscription | null = null;

    constructor (
        private router: Router,
        private oauth2Service: OAuth2Service
    ) { }

    ngOnInit(): void {
        // S'abonner au statut d'authentification
        this.authSubscription = this.oauth2Service.isAuthenticated$.subscribe(
            (isAuth: boolean) => {
                this.isAuthenticated = isAuth;
                if (isAuth) {
                   this.router.navigate(["/dashboard"]);
                }
            }
        );
    }
    
    ngOnDestroy(): void {
        if (this.authSubscription) {
            this.authSubscription.unsubscribe();
        }
    }

    connectUser(): void {
        this.router.navigate(['/login']);
    }

    registerUser(): void {
        this.router.navigate(['/register']);
    }

    onMenuToggled(): void {
        this.isMenuOpen = !this.isMenuOpen;
        // En tablette, afficher le menu dans le footer
        if (window.innerWidth >= 768 && window.innerWidth < 992) {
            this.showFooterNav = !this.showFooterNav;
        }
    }

    onMenuClosed(): void {
        this.isMenuOpen = false;
        this.showFooterNav = false;
    }
}