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
    selector: 'app-deactivate',
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './deactivate.component.html',
    styleUrl: './deactivate.component.scss'
})
export class DeactivateComponent implements OnInit, OnDestroy {
    private authSubscription: Subscription | null = null;
    emailKey: string | null = null;
    loading = false;
    error = '';
    returnUrl = '/login';
    deactivateForm!: FormGroup;
    
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
        
        this.deactivateForm = this.formBuilder.group({key: ['', [Validators.required]],});
        
        this.emailKey = this.route.snapshot.queryParamMap.get('key');
        
        if(this.emailKey != null && this.emailKey?.length > 1) {
            this.emailKey = this.sanitizationService.decodeHtml(this.emailKey);
            this.deactivateUser();
        }
    }
    
    ngOnDestroy(): void { }
    
    private markFormGroupTouched(): void {
        Object.keys(this.deactivateForm.controls).forEach((key) => {
            const control = this.deactivateForm.get(key);
            if (control) {
                control.markAsTouched();
            }
        });
    }

    onSubmit(): void {
        if (this.deactivateForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.loading = true;
        this.error = '';

        const formValue = this.deactivateForm.value;
        
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
            this.deactivateUser();
        }
    }
    
    deactivateUser(): void {
        this.loading = true;
        this.error = '';
        
        console.log("key : ", this.emailKey);
        
        this.userService.deactivateRegisteredUser(this.emailKey).subscribe({
            next: (response) => {
                this.deactivateForm.reset();
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
        return this.deactivateForm.get('key');
    }
}
