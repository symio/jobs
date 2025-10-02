// src/app/app.ts
import { Component, signal, OnInit, HostListener } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { HeaderComponent } from '@app/components/header/header.component';
import { FooterComponent } from '@app/components/footer/footer.component';
import { MobileMenuComponent } from '@app/components/mobile-menu/mobile-menu.component';
import { OAuth2Service } from '@services/oauth2.service';
import { PageTitleService } from '@services/page-title.service';
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from '@app/interceptors/auth.interceptor';

@Component({
    selector: 'app-root',
    imports: [CommonModule, RouterOutlet, HeaderComponent, FooterComponent, MobileMenuComponent],
    templateUrl: './app.html',
    styleUrl: './app.scss',
})
export class App implements OnInit {
    protected readonly title = signal('j-jobs_frontend');
    isMenuOpen = false;
    showFooterNav = false;
    isAuthenticated = false;
    pageTitle = '';

    constructor(
        private oauth2Service: OAuth2Service,
        private router: Router,
        private pageTitleService: PageTitleService
    ) { }

    ngOnInit(): void {
        this.oauth2Service.isAuthenticated$.subscribe(
            (isAuth: boolean) => {
                this.isAuthenticated = isAuth;
//                if (!isAuth) {
//                    const currentUrl = this.router.url;
//                    // ✅ Exclure landing, login et register
//                    if (currentUrl !== '/' && currentUrl !== '/login' && currentUrl !== '/register') {
//                        this.router.navigate(['/']);
//                    }
//                }
            }
        );

        this.pageTitleService.pageTitle$.subscribe(
            (title: string) => {
                this.pageTitle = title;
            }
        );

        this.checkScreenSize();
    }

    @HostListener('window:resize')
    onResize(): void {
        this.checkScreenSize();
    }

    private checkScreenSize(): void {
        const width = window.innerWidth;

        if (width >= 992) {
            // Desktop: menu footer toujours visible, pas de menu mobile
            this.showFooterNav = true;
            this.isMenuOpen = false;
        } else {
            // Mobile/Tablette: menu footer masqué par défaut
            if (!this.isMenuOpen) {
                this.showFooterNav = false;
            }
        }
    }

    onMenuToggled(): void {
        const width = window.innerWidth;

        if (width < 768) {
            // Mobile: menu overlay plein écran
            this.isMenuOpen = !this.isMenuOpen;
            this.showFooterNav = !this.showFooterNav;
        } else if (width < 992) {
            // Tablette: menu dans le footer
            this.showFooterNav = !this.showFooterNav;
            this.isMenuOpen = !this.isMenuOpen;
        }
    }

    onMenuClosed(): void {
        this.isMenuOpen = false;
        if (window.innerWidth < 992) {
            this.showFooterNav = false;
        }
    }
}
