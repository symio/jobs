import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ApiurlService } from '@app/environments/apiurl.service';
import { OAuth2Service } from './oauth2.service';
import { catchError, Observable, of, tap, throwError } from 'rxjs';

export interface UserRegisterResponse {
    id: number;
    name: string;
    firstname: string;
    email: string;
    gdproptin: boolean;
    createdAt: string;
    updatedAt: string;
    enabled: boolean;
    username: string;
    accountNonExpired: boolean;
    accountNonLocked: boolean;
    credentialsNonExpired: boolean;
    roleId: number;
    _links?: Record<string, any>;
}

export interface UserRegister {
    name: string;
    firstname: string;
    email: string;
    password: string;
    gdproptin: boolean;
}

export interface LostPasswordStep2 {
    key: string;
    email: string;
    newPassword: string;
    passwordConfirm: string;
}

@Injectable({
    providedIn: 'root',
})
export class UserService {
    private readonly apiBaseUrl: string;

    constructor(
        private http: HttpClient,
        private oauth2Service: OAuth2Service,
        private apiurlService: ApiurlService,
    ) {
        this.apiBaseUrl = this.apiurlService.getApiBaseUrl();
    }

    activateRegisteredUser(key: string| null = null): Observable<void> {
        const headers = this.oauth2Service.buildRequestHeaders(false);
        if(key === null) {
            return new Observable;
        }
        return this.http
            .post<void>(`${this.apiBaseUrl}/register/activate`, { key: key }, { headers })
            .pipe(
                tap((response) => this.handleActivateRegisteredUserSuccess(response)),
                catchError((err) => this.handleAuthError(err)),
            );
    }
    
    //@todo
    passwordLoststep1(email: string| null = null): Observable<void> {
        const headers = this.oauth2Service.buildRequestHeaders(false);
        if(email === null) {
            return new Observable;
        }
        return this.http
            .post<void>(`${this.apiBaseUrl}/register/password-lost/step1`, { email: email }, { headers })
            .pipe(
                tap((response) => this.handleActivateRegisteredUserSuccess(response)),
                catchError((err) => this.handleAuthError(err)),
            );
    }
    
    deactivatePasswordStep1(key: string| null = null): Observable<void> {
        const headers = this.oauth2Service.buildRequestHeaders(false);
        if(key === null) {
            return new Observable;
        }
        return this.http
            .post<void>(`${this.apiBaseUrl}/register/password-lost/step1/deactivate`, { key: key }, { headers })
            .pipe(
                tap((response) => this.handleDeactivateRegisteredUserSuccess(response)),
                catchError((err) => this.handleAuthError(err)),
            );
    }
    
    //@todo
    passwordLoststep2(lostPasswordStep2: LostPasswordStep2| null = null): Observable<void> {
        const headers = this.oauth2Service.buildRequestHeaders(false);
        if(lostPasswordStep2?.key === null) {
            return new Observable;
        }
        return this.http
            .post<void>(`${this.apiBaseUrl}/register/password-lost/step2`, lostPasswordStep2, { headers })
            .pipe(
                tap((response) => this.handleActivateRegisteredUserSuccess(response)),
                catchError((err) => this.handleAuthError(err)),
            );
    }
    
    deactivatePasswordStep2(key: string| null = null): Observable<void> {
        const headers = this.oauth2Service.buildRequestHeaders(false);
        if(key === null) {
            return new Observable;
        }
        return this.http
            .post<void>(`${this.apiBaseUrl}/register/password-lost/step2/deactivate`, { key: key }, { headers })
            .pipe(
                tap((response) => this.handleDeactivateRegisteredUserSuccess(response)),
                catchError((err) => this.handleAuthError(err)),
            );
    }
    
    
    deactivateRegisteredUser(key: string| null = null): Observable<void> {
        const headers = this.oauth2Service.buildRequestHeaders(false);
        if(key === null) {
            return new Observable;
        }
        return this.http
            .post<void>(`${this.apiBaseUrl}/register/deactivate`, { key: key }, { headers })
            .pipe(
                tap((response) => this.handleDeactivateRegisteredUserSuccess(response)),
                catchError((err) => this.handleAuthError(err)),
            );
    }
    
    registerUser(userRegister: UserRegister): Observable<UserRegisterResponse> {
        const headers = this.oauth2Service.buildRequestHeaders(false);

        return this.http
            .post<UserRegisterResponse>(`${this.apiBaseUrl}/users`, userRegister, {
                headers,
            })
            .pipe(
                tap((response) => this.handleRegisterSuccess(response)),
                catchError((err) => this.handleAuthError(err)),
            );
    }

    private handleDeactivateRegisteredUserSuccess(response: void) {
    }
    private handleActivateRegisteredUserSuccess(response: void) {
    }
    private handleRegisterSuccess(response: UserRegisterResponse) {
        //    console.log('Utilisateur enregistré avec succès:', response);
    }

    private handleAuthError(error: any) {
        return throwError(() => error);
    }
}
