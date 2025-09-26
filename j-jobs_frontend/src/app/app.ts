import { Component, signal, OnInit, HostListener } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { MobileMenuComponent } from './components/mobile-menu/mobile-menu.component';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, HeaderComponent, FooterComponent, MobileMenuComponent],
    templateUrl: './app.html',
    styleUrl: './app.scss',
})
export class App implements OnInit {
    protected readonly title = signal('j-jobs_frontend');
    isMenuOpen = false;
    showFooterNav = false;

    ngOnInit(): void {
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
