import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ApiurlService } from '@app/environments/apiurl.service';
import { OAuth2Service } from './oauth2.service';
import { catchError, Observable, tap, throwError } from 'rxjs';

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

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly apiBaseUrl: string;

    constructor(
        private http: HttpClient,
        private oauth2Service: OAuth2Service,
        private apiurlService: ApiurlService
    ) {
        this.apiBaseUrl = this.apiurlService.getApiBaseUrl();
    }

    registerUser(userRegister: UserRegister): Observable<UserRegisterResponse> {
        const headers = this.oauth2Service.buildRequestHeaders(false);

        return this.http.post<UserRegisterResponse>(`${this.apiBaseUrl}/users`, userRegister, { headers })
            .pipe(
                tap(response => this.handleRegisterSuccess(response)),
                catchError(err => this.handleAuthError(err))
            );
    }

    private handleRegisterSuccess(response: UserRegisterResponse) {
        console.log('✅ Utilisateur enregistré avec succès:', response);
    }

    private handleAuthError(error: any) {
        return throwError(() => error);
    }
}
