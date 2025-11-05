import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
    ReactiveFormsModule, FormBuilder,
    FormGroup, Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { OAuth2Service } from '@app/services/oauth2.service';
import { SanitizationService } from '@app/services/sanitization.service';
import { LostPasswordStep2, UserService } from '@app/services/user.service';
import { Observable, Subscription } from 'rxjs';

@Component({
    standalone: true,
    selector: 'app-passwordlost_lost-password',
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './lost-password.component.html',
    styleUrl: './lost-password.component.scss'
})
export class LostPasswordComponent {
    private authSubscription: Subscription | null = null;
    mainTitle = "Demande de changement de mot de passe"; 
    newPasswordForm!: FormGroup;
    emailValue: string | null = null;
    loading = false;
    error = '';
    returnUrl = '/login';
    passwordVisible = false;

    constructor(
        private route: ActivatedRoute,
        private formBuilder: FormBuilder,
        private userService: UserService,
        private oAuth2Service: OAuth2Service,
        private sanitizationService: SanitizationService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.authSubscription = this.oAuth2Service
            .isAuthenticated()
            .subscribe((isAuthenticated: boolean) => {
                if (isAuthenticated) {
                    this.router.navigate([this.returnUrl]);
                }
            });
        
        this.newPasswordForm = this.formBuilder.group(
        {
            email: ['', [Validators.required, Validators.email]],
        });
    }
    
    ngOnDestroy(): void {
        if (this.authSubscription) {
            this.authSubscription.unsubscribe();
        }
    }
    
    private markFormGroupTouched(): void {
        Object.keys(this.newPasswordForm.controls).forEach((key) => {
            const control = this.newPasswordForm.get(key);
            if (control) {
                control.markAsTouched();
            }
        });
    }

    onSubmit(): void {
        if (this.newPasswordForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.loading = true;
        this.error = '';

        const formValue = this.newPasswordForm.value;
        
        const fieldsToCheck = ['email'];
        const hasDangerousContent = fieldsToCheck.some((field) => {
            const value = formValue[field];
            return this.sanitizationService.containsDangerousContent(value);
        });

        if (hasDangerousContent) {
            this.error = 'Des caractères invalides ont été détectés dans vos informations.';
            this.loading = false;
            return;
        }
        
        this.emailValue= formValue.email;
        
        if(this.emailValue != null && this.emailValue?.length > 1) {
            this.newPasswordForUser();
        }
    }
    
    newPasswordForUser(): void {
        this.loading = true;
        this.error = '';
        
        this.userService.passwordLoststep1(this.emailValue).subscribe({
            next: (response) => {
                this.newPasswordForm.reset();
                this.router.navigate([this.returnUrl]);
                this.loading = false;
            },
            error: (error) => {
                console.error('Erreur de création de compte:', error);
                this.error = 'Données invalides. Veuillez réessayer.';
                this.loading = false;
            },
            complete: () => {
                this.loading = false;
            },
        });
    }
    
    get email() {
        return this.newPasswordForm.get('email');
    }
    
}
