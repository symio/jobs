import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RegisterComponent } from './register.component';
import { UserService, UserRegister } from '@services/user.service';
import { OAuth2Service } from '@services/oauth2.service';
import { of, throwError, BehaviorSubject } from 'rxjs';
import { CommonModule } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

const mockUserRegisterResponse: any = {
    id: 1,
    name: 'Runner',
    firstname: 'road',
    email: 'bip.bip@acme.com',
    password: 'ValidPass1!',
    passwordConfirm: 'ValidPass1!',
    gdproptin: true,
};

describe('RegisterComponent', () => {
    let component: RegisterComponent;
    let fixture: ComponentFixture<RegisterComponent>;
    let userService: jasmine.SpyObj<UserService>;
    let oAuth2Service: jasmine.SpyObj<OAuth2Service>;
    let router: Router;
    let isAuthenticatedSubject: BehaviorSubject<boolean>;

    const setupValidForm = () => {
        component.name?.setValue('Runner');
        component.firstname?.setValue('road');
        component.email?.setValue('bip.bip@acme.com');
        component.password?.setValue('ValidPass1!');
        component.passwordConfirm?.setValue('ValidPass1!');
        component.gdproptin?.setValue(true);
        component.registerForm.updateValueAndValidity();
    };

    beforeEach(async () => {
        const userServiceSpy = jasmine.createSpyObj('UserService', [
            'registerUser',
        ]);
        isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
        const oAuth2ServiceSpy = jasmine.createSpyObj('OAuth2Service', [
            'isAuthenticated',
        ]);
        oAuth2ServiceSpy.isAuthenticated.and.returnValue(isAuthenticatedSubject);

        await TestBed.configureTestingModule({
            imports: [
                RegisterComponent,
                ReactiveFormsModule,
                RouterTestingModule,
                HttpClientTestingModule,
            ],
            providers: [
                { provide: UserService, useValue: userServiceSpy },
                { provide: OAuth2Service, useValue: oAuth2ServiceSpy },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(RegisterComponent);
        component = fixture.componentInstance;
        userService = TestBed.inject(
            UserService,
        ) as jasmine.SpyObj<UserService>;
        oAuth2Service = TestBed.inject(
            OAuth2Service,
        ) as jasmine.SpyObj<OAuth2Service>;
        router = TestBed.inject(Router);
        spyOn(router, 'navigate');
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should redirect to home if user is already authenticated', () => {
        component.returnUrl = '/';
        isAuthenticatedSubject.next(true);
        fixture.detectChanges();
        expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should initialize the form with default values and validators', () => {
        expect(component.registerForm).toBeDefined();
        expect(component.registerForm.get('name')?.value).toBe('');
        expect(component.registerForm.get('email')?.valid).toBeFalsy();
        expect(component.registerForm.valid).toBeFalsy();
    });

    describe('Form Validations', () => {
        it('should validate name as required', () => {
            const control = component.registerForm.get('name');
            control?.setValue('');
            control?.updateValueAndValidity();

            expect(control?.hasError('required')).toBeTrue();
            expect(control?.valid).toBeFalse();
        });

        it('should validate email required', () => {
            component.email?.setValue('');
            expect(component.email?.hasError('required')).toBe(true);
        });

        it('should validate email format', () => {
            component.email?.setValue('invalid-email');
            expect(component.email?.hasError('email')).toBe(true);

            component.email?.setValue('valid.email@test.com');
            expect(component.email?.hasError('email')).toBe(false);
        });

        it('should validate password required', () => {
            component.password?.setValue('');
            expect(component.password?.hasError('required')).toBe(true);
            expect(component.passwordConfirm?.hasError('required')).toBe(true);
        });

        it('should validate passwords matching', () => {
            component.password?.setValue('Pass123!');
            component.passwordConfirm?.setValue('Pass1234!');
            component.registerForm.updateValueAndValidity();
            expect(component.registerForm.hasError('passwordsMismatch')).toBe(true);

            component.passwordConfirm?.setValue('Pass123!');
            component.registerForm.updateValueAndValidity();
            expect(component.registerForm.hasError('passwordsMismatch')).toBe(false);
        });

        it('should validate strong password pattern', () => {
            component.password?.setValue('Pass1!');
            component.password?.updateValueAndValidity();
            expect(component.password?.hasError('pattern')).toBe(true, `Le mot de passe 'Pass1!' (trop court) devrait avoir l'erreur 'pattern'.`);

            component.password?.setValue('Pass1234');
            component.password?.updateValueAndValidity();
            expect(component.password?.hasError('pattern')).toBe(true, `Le mot de passe 'Pass1234' (manque spécial) devrait avoir l'erreur 'pattern'.`);

            component.password?.setValue('PassWord123!');
            component.password?.updateValueAndValidity();
            expect(component.password?.hasError('pattern')).toBe(false, `Le mot de passe 'PassWord123!' valide ne devrait PAS avoir l'erreur 'pattern'.`);
        });

        it('should validate minimum password length of 8 characters', () => {
            component.password?.setValue('Test1!');
            component.password?.updateValueAndValidity();
            expect(component.password?.hasError('pattern')).toBe(true, "Moins de 8 caractères devrait échouer le pattern.");

            component.password?.setValue('Test123!');
            component.password?.updateValueAndValidity();
            expect(component.password?.hasError('pattern')).toBe(false, "8 caractères et fort devrait réussir le pattern.");
        });

        it('should validate gdproptin requiredTrue', () => {
            const control = component.registerForm.get('gdproptin');
            control?.setValue(false);
            control?.updateValueAndValidity();

            expect(control?.valid).toBeFalse();
        });
    });

    describe('Form State Management', () => {
        it('should maintain form state during multiple submissions', () => {
            userService.registerUser.and.returnValue(
                throwError(() => new Error('Error')),
            );

            component.name?.setValue('Runner');
            component.firstname?.setValue('road');
            component.onSubmit();

            expect(component.name?.value).toBe('Runner');
            expect(component.firstname?.value).toBe('road');
        });

        it('should clear error on successful submission', () => {
            component.error = 'Previous error';
            userService.registerUser.and.returnValue(of(mockUserRegisterResponse));

            setupValidForm();
            component.onSubmit();

            expect(component.error).toBe('');
            expect(router.navigate).toHaveBeenCalledWith([component.returnUrl]);
        });

        it('should set loading to true on submission and false on error', () => {
            setupValidForm();

            userService.registerUser.and.returnValue(
                throwError(() => new Error('Simulated HTTP Error')),
            );

            spyOn(console, 'error');

            component.loading = false;
            component.error = '';

            component.onSubmit();

            expect(component.loading).toBeFalse();
            expect(component.error).toBe('Données invalides. Veuillez réessayer.');
            expect(console.error).toHaveBeenCalledWith('Erreur de création de compte:', jasmine.any(Error));
        });

        it('should set loading to false on successful submission (Ligne 308 - ou similaire)', () => {
            component.loading = true;
            userService.registerUser.and.returnValue(of(mockUserRegisterResponse));

            setupValidForm();
            component.onSubmit();

            expect(component.loading).toBeFalse();
        });

        it('should reset form on successful submission', () => {
            userService.registerUser.and.returnValue(of(mockUserRegisterResponse));

            setupValidForm();
            component.onSubmit();

            expect(component.registerForm.value).toEqual({
                name: null,
                firstname: null,
                email: null,
                password: null,
                passwordConfirm: null,
                gdproptin: null,
            });
            expect(router.navigate).toHaveBeenCalledWith([component.returnUrl]);
        });

    });
});
