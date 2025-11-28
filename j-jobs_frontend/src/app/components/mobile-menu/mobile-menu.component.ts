import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuService, MenuData, MenuDataNoTitle, UserMenuData, } from '../../services/menu.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { OAuth2Service, UserInfo } from '@app/services/oauth2.service';
import { Subscription } from 'rxjs';
import { Router, RouterModule } from '@angular/router';

@Component({
    selector: 'app-mobile-menu',
    imports: [CommonModule, RouterModule],
    templateUrl: './mobile-menu.component.html',
    styleUrl: './mobile-menu.component.scss',
    standalone: true,
})
export class MobileMenuComponent implements OnInit {
    @Input() isMenuOpen: boolean = false;
    @Output() menuClosed = new EventEmitter<void>();
    @Input() isAuthenticated: boolean = false;
    
    userInfo: UserInfo | null = null;
    private authSubscription: Subscription | null = null;
    private userInfoSubscription: Subscription | null = null;

    menuData: MenuData = { title: '', items: [] };
    sideMenuData: MenuDataNoTitle = { items: [] };
    userMenuData: UserMenuData = { title: '', items: [] };

    constructor(
        private menuService: MenuService,
        private sanitizer: DomSanitizer,
        private oauth2Service: OAuth2Service,
        private router: Router,
    ) { }

    getIcon(name: string): SafeHtml {
        const iconHtml = this.menuService.getIcon(name);
        return this.sanitizer.bypassSecurityTrustHtml(iconHtml);
    }

    getUserDisplayName(): string {
        if (!this.userInfo?.displayName) return '';

        return this.userInfo.displayName;
    }

    ngOnInit(): void {
        // S'abonner au statut d'authentification
        this.authSubscription = this.oauth2Service.isAuthenticated$.subscribe(
            (isAuth: boolean) => {
                this.isAuthenticated = isAuth;
                if (!isAuth) {
                    this.userInfo = null;
                }
            },
        );

        // S'abonner aux informations utilisateur
        this.userInfoSubscription = this.oauth2Service.userInfo$.subscribe(
            (userInfo: UserInfo | null) => {
                this.userInfo = userInfo;
                this.userMenuData = this.menuService.getUserMenuData(this.getUserDisplayName());
            },
        );

        this.menuData = this.menuService.getMenuData();
        this.sideMenuData = this.menuService.getSideMenuData();
        this.userMenuData = this.menuService.getUserMenuData(this.getUserDisplayName());
    }

    ngOnDestroy(): void {
        if (this.authSubscription) {
            this.authSubscription.unsubscribe();
        }
        if (this.userInfoSubscription) {
            this.userInfoSubscription.unsubscribe();
        }
    }

    closeMenu(): void {
        this.menuClosed.emit();
    }


    logout(): void {
        this.closeMenu();
        this.oauth2Service.logout().subscribe({
            next: () => {
                this.router.navigate(['/']);
            },
            error: (error) => {
                console.error('Erreur lors de la déconnexion:', error);
            },
        });
    }
}
