// src/app/pages/register/register.component.ts
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { OAuth2Service } from '@services/oauth2.service';
import { UserRegister, UserService } from '@services/user.service';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent implements OnInit, OnDestroy {
  private authSubscription: Subscription | null = null;
  registerForm!: FormGroup;
  loading = false;
  error = '';
  returnUrl = '/login';

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService,
    private oAuth2Service: OAuth2Service,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  ngOnInit(): void {
    this.registerForm = this.formBuilder.group(
      {
        name: ['', [Validators.required]],
        firstname: ['', [Validators.required]],
        email: ['', [Validators.required, Validators.email]],
        password: [
          '',
          [
            Validators.required,
            Validators.pattern(
              /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&_\-\|~#])[A-Za-z\d@$!%*?&_\-\|~#]{8,}$/,
            ),
          ],
        ],
        passwordConfirm: ['', [Validators.required]],
        gdproptin: [false, [Validators.requiredTrue]],
      },
      {
        validators: this.passwordsMatchValidator,
      },
    );

    // Rediriger si déjà connecté
    this.authSubscription = this.oAuth2Service
      .isAuthenticated()
      .subscribe((isAuthenticated: boolean) => {
        if (isAuthenticated) {
          this.router.navigate([this.returnUrl]);
        }
      });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.registerForm.controls).forEach((key) => {
      const control = this.registerForm.get(key);
      if (control) {
        control.markAsTouched();
      }
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.loading = true;
    this.error = '';

    const userRegister: UserRegister = {
      name: this.name?.value,
      firstname: this.firstname?.value,
      email: this.email?.value,
      password: this.password?.value,
      gdproptin: this.gdproptin?.value,
    };

    this.userService.registerUser(userRegister).subscribe({
      next: (response) => {
        this.registerForm.reset();
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
  get name() {
    return this.registerForm.get('name');
  }
  get firstname() {
    return this.registerForm.get('firstname');
  }
  get email() {
    return this.registerForm.get('email');
  }
  get password() {
    return this.registerForm.get('password');
  }
  get passwordConfirm() {
    return this.registerForm.get('passwordConfirm');
  }
  get gdproptin() {
    return this.registerForm.get('gdproptin');
  }

  /**
   * Vérifie que password et passwordConfirm correspondent
   */
  private passwordsMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const passwordConfirm = form.get('passwordConfirm')?.value;
    return password === passwordConfirm ? null : { passwordsMismatch: true };
  }
}
