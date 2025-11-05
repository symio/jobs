import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
    ReactiveFormsModule, FormBuilder,
    FormGroup, Validators, AbstractControl
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { OAuth2Service } from '@app/services/oauth2.service';
import { SanitizationService } from '@app/services/sanitization.service';
import { UserService } from '@app/services/user.service';
import { Observable, Subscription } from 'rxjs';

@Component({
    template: '',
})
export abstract class AbstractKeyPageComponent implements OnInit, OnDestroy {
    protected authSubscription: Subscription | null = null;
    emailKey: string | null = null;
    loading = false;
    error = '';
    returnUrl = '/login';
    keyForm!: FormGroup; 
    
    public abstract mainTitle: string;
    
    constructor(
        protected route: ActivatedRoute,
        protected formBuilder: FormBuilder,
        protected userService: UserService,
        protected oAuth2Service: OAuth2Service,
        protected sanitizationService: SanitizationService,
        protected router: Router
    ) { }

    ngOnInit(): void {
        this.authSubscription = this.oAuth2Service
            .isAuthenticated()
            .subscribe((isAuthenticated: boolean) => {
                if (isAuthenticated) {
                    this.router.navigate([this.returnUrl]);
                }
            });
        
        this.keyForm = this.formBuilder.group({
            key: ['', [Validators.required]],
        });
        
        this.emailKey = this.route.snapshot.queryParamMap.get('key');
        
        if (this.emailKey != null && this.emailKey.length > 1) {
            this.emailKey = this.sanitizationService.decodeHtml(this.emailKey);
            this.executeProcess(this.emailKey);
        }
    }
    
    ngOnDestroy(): void {
        this.authSubscription?.unsubscribe();
    }
    
    protected abstract processKey(key: string): Observable<void>;
    
    private markFormGroupTouched(): void {
        Object.keys(this.keyForm.controls).forEach((key) => {
            const control = this.keyForm.get(key);
            if (control) {
                control.markAsTouched();
            }
        });
    }

    onSubmit(): void {
        if (this.keyForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        this.loading = true;
        this.error = '';

        const formValue = this.keyForm.value;
        
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
        
        if (this.emailKey != null && this.emailKey.length > 1) {
            this.executeProcess(this.emailKey);
        } else {
            this.loading = false;
        }
    }
    
    protected executeProcess(key: string): void {
        this.loading = true;
        this.error = '';
        
        this.processKey(key).subscribe({ 
            next: () => {
                this.keyForm.reset();
                this.router.navigate([this.returnUrl]);
                this.loading = false;
            },
            error: (error) => {
                console.error('Erreur du processus:', error);
                this.error = 'Données invalides. Veuillez réessayer.';
                this.loading = false;
            },
            complete: () => {
                this.loading = false;
            },
        });
    }
    
    get key(): AbstractControl | null {
        return this.keyForm.get('key');
    }
}
