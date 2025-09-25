import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-landing-page',
    imports: [],
    templateUrl: './landing-page.component.html',
    styleUrl: './landing-page.component.scss',
    standalone: true
})
export class LandingPageComponent {

    isMenuOpen = false;
    showFooterNav = false;

    constructor(private router: Router) {}

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