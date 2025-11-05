// src/app/pages/register/activate/activate.component.ts
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
    selector: 'app-passwordlost_new-password',
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './new-password.component.html',
    styleUrl: './new-password.component.scss'
})
export class NewPasswordComponent {
    private authSubscription: Subscription | null = null;
    mainTitle = "Validation de changement de mot de passe"; 
    newPasswordForm!: FormGroup;
    emailKey: string | null = null;
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
            key: ['', [Validators.required]],
            newPassword: [
                '',
                [
                    Validators.required,
                    Validators.pattern(
                        /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_\-\|~#])[A-Za-z\d@$!%*?&_\-\|~#]{8,}$/,
                    ),
                ],
            ],
            passwordConfirm: ['', [Validators.required]]
        }, {
                validators: this.passwordsMatchValidator,
        },);
        
        this.emailKey = this.route.snapshot.queryParamMap.get('key');
        
        if(this.emailKey != null && this.emailKey?.length > 1) {
            this.emailKey = this.sanitizationService.decodeHtml(this.emailKey);
            this.newPasswordForm.patchValue({
                key: this.emailKey
            });
        }
    }
    
    ngOnDestroy(): void {
        if (this.authSubscription) {
            this.authSubscription.unsubscribe();
        }
    }
    
    togglePasswordVisibility(): void {
        this.passwordVisible = !this.passwordVisible;
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
        
        const fieldsToCheck = ['key'];
        const hasDangerousContent = fieldsToCheck.some((field) => {
            const value = formValue[field];
            return this.sanitizationService.containsDangerousContent(value);
        });

        if (hasDangerousContent) {
            this.error = 'Des caractères invalides ont été détectés dans vos informations.';
            this.loading = false;
            return;
        }
        
        this.emailKey = formValue.key;
        
        if(this.emailKey != null && this.emailKey?.length > 1) {
            this.newPasswordForUser(formValue as LostPasswordStep2);
        }
    }
    
    newPasswordForUser(formValue: LostPasswordStep2): void {
        this.loading = true;
        this.error = '';
        
        this.userService.passwordLoststep2(formValue).subscribe({
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
    
    // Getters pour faciliter l'accès aux contrôles du formulaire
    get key() {
        return this.newPasswordForm.get('key');
    }
    get newPassword() {
        return this.newPasswordForm.get('password');
    }
    get passwordConfirm() {
        return this.newPasswordForm.get('passwordConfirm');
    }
    
    /**
     * Vérifie que password et passwordConfirm correspondent
     */
    private passwordsMatchValidator(form: FormGroup) {
        const password = form.get('newPassword')?.value;
        const passwordConfirm = form.get('passwordConfirm')?.value;
        return password === passwordConfirm ? null : { passwordsMismatch: true };
    }
}
