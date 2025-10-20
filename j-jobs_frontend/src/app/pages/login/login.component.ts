// src/app/pages/login/login.component.ts
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
    ReactiveFormsModule,
    FormBuilder,
    FormGroup,
    Validators,
} from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { OAuth2Service } from '@services/oauth2.service';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { SanitizationService } from '@services/sanitization.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
    loginForm!: FormGroup;
    loading = false;
    error = '';
    returnUrl = '';
    rememberMe = false;
    passwordVisible = false;
    private authSubscription: Subscription | null = null;

    constructor(
        private formBuilder: FormBuilder,
        private oauth2Service: OAuth2Service,
        private router: Router,
        private route: ActivatedRoute,
        private sanitizationService: SanitizationService,
    ) { }

    ngOnDestroy(): void {
        if (this.authSubscription) {
            this.authSubscription.unsubscribe();
        }
    }

    ngOnInit(): void {
        this.loginForm = this.formBuilder.group({
            clientId: ['', [Validators.required, Validators.email]],
            clientSecret: ['', [Validators.required]],
        });

        this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';

        this.authSubscription = this.oauth2Service
            .isAuthenticated()
            .subscribe((isAuthenticated: boolean) => {
                if (isAuthenticated) {
                    this.router.navigate([this.returnUrl]);
                }
            });
    }

    togglePasswordVisibility(): void {
        this.passwordVisible = !this.passwordVisible;
    }

    onRememberMeChange(event: any): void {
        this.rememberMe = event.target.checked;
    }

    private markFormGroupTouched(): void {
        Object.keys(this.loginForm.controls).forEach((key) => {
            const control = this.loginForm.get(key);
            if (control) {
                control.markAsTouched();
            }
        });
    }

    onSubmit(): void {
        if (this.loginForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.loading = true;
        this.error = '';

        const formValue = this.loginForm.value;

        const sanitizedEmail = this.sanitizationService.sanitizeEmail(formValue.clientId);

        if (!sanitizedEmail || sanitizedEmail === '') {
            this.error = "L'adresse email est invalide.";
            this.loading = false;
            return;
        }

        const credentials = {
            clientId: sanitizedEmail,
            clientSecret: formValue.clientSecret,
            rememberMe: this.rememberMe,
        };

        this.oauth2Service.login(credentials).subscribe({
            next: (response) => {
                this.router.navigate([this.returnUrl]);
            },
            error: (error) => {
                console.error('Erreur de connexion:', error);
                this.error = 'Identifiants invalides. Veuillez réessayer.';
                this.loading = false;
            },
            complete: () => {
                this.loading = false;
            },
        });
    }

    // Getters pour faciliter l'accès aux contrôles du formulaire
    get clientId() {
        return this.loginForm.get('clientId');
    }

    get clientSecret() {
        return this.loginForm.get('clientSecret');
    }
}
