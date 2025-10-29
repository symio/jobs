import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
    ReactiveFormsModule, FormBuilder,
    FormGroup, Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { OAuth2Service } from '@app/services/oauth2.service';
import { SanitizationService } from '@app/services/sanitization.service';
import { UserService } from '@app/services/user.service';
import { Observable, Subscription } from 'rxjs';

@Component({
    standalone: true,
    selector: 'app-activate',
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './activate.component.html',
    styleUrl: './activate.component.scss'
})
export class ActivateComponent implements OnInit, OnDestroy {
    private authSubscription: Subscription | null = null;
    emailKey: string | null = null;
    loading = false;
    error = '';
    returnUrl = '/login';
    activateForm!: FormGroup;
    
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
        
        this.activateForm = this.formBuilder.group({key: ['', [Validators.required]],});
        
        this.emailKey = this.route.snapshot.queryParamMap.get('key');
        
        if(this.emailKey != null && this.emailKey?.length > 1) {
            this.emailKey = this.sanitizationService.decodeHtml(this.emailKey);
            this.activateUser();
        }
    }
    
    ngOnDestroy(): void { }
    
    private markFormGroupTouched(): void {
        Object.keys(this.activateForm.controls).forEach((key) => {
            const control = this.activateForm.get(key);
            if (control) {
                control.markAsTouched();
            }
        });
    }

    onSubmit(): void {
        if (this.activateForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.loading = true;
        this.error = '';

        const formValue = this.activateForm.value;
        
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
            this.activateUser();
        }
    }
    
    activateUser(): void {
        this.loading = true;
        this.error = '';
        
        console.log("key : ", this.emailKey);
        
        this.userService.activateRegisteredUser(this.emailKey).subscribe({
            next: (response) => {
                this.activateForm.reset();
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
        return this.activateForm.get('key');
    }
}
