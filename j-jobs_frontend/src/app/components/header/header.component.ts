// src/app/components/header/header.component.ts
import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { OAuth2Service, UserInfo } from '@app/services/oauth2.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
  standalone: true,
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Output() menuToggled = new EventEmitter<void>();
  @Input() pageTitle: string = '';

  isAuthenticated = false;
  userInfo: UserInfo | null = null;
  isUserMenuOpen = false;
  private authSubscription: Subscription | null = null;
  private userInfoSubscription: Subscription | null = null;

  constructor(
    private router: Router,
    private oauth2Service: OAuth2Service,
  ) {}

  ngOnInit(): void {
    // S'abonner au statut d'authentification
    this.authSubscription = this.oauth2Service.isAuthenticated$.subscribe(
      (isAuth: boolean) => {
        this.isAuthenticated = isAuth;
        if (!isAuth) {
          this.userInfo = null;
          this.isUserMenuOpen = false;
        }
      },
    );

    // S'abonner aux informations utilisateur
    this.userInfoSubscription = this.oauth2Service.userInfo$.subscribe(
      (userInfo: UserInfo | null) => {
        this.userInfo = userInfo;
      },
    );
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    if (this.userInfoSubscription) {
      this.userInfoSubscription.unsubscribe();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    const userBadge = document.querySelector('.user-badge');

    // Fermer le menu si on clique en dehors
    if (userBadge && !userBadge.contains(target)) {
      this.isUserMenuOpen = false;
    }
  }

  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  toggleMenu(): void {
    this.menuToggled.emit();
  }

  getUserDisplayName(): string {
    if (!this.userInfo?.displayName) return '';

    return this.userInfo.displayName;
  }

  logout(): void {
    this.isUserMenuOpen = false;
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
