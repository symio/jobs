// core/services/oauth2.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { environment } from '@env/environment';

export interface OAuth2TokenResponse {
    access_token: string;
    token_type: string;
    expires_in: number;
    scope: string;
    originally_expires: number;
}

export interface OAuth2Credentials {
    clientId: string;
    clientSecret: string;
    rememberMe: boolean;
}

export interface UserInfo {
    clientId: string;
    scope: string;
    authority: string;
    isAdmin: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class OAuth2Service {
    private tokenSubject = new BehaviorSubject<string | null>(this.getStoredToken());
    private userInfoSubject = new BehaviorSubject<UserInfo | null>(this.getStoredUserInfo());
    private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.checkInitialAuth());

    public token$ = this.tokenSubject.asObservable();
    public userInfo$ = this.userInfoSubject.asObservable();
    public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

    constructor(private http: HttpClient) { }

    /**
     * Authentification avec Client Credentials Flow
     */
    login(credentials: OAuth2Credentials): Observable<OAuth2TokenResponse> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded'
        });

        let scopes = "access";
        if (credentials.rememberMe === true) {
            scopes += " rememberme";
        }

        const body = new URLSearchParams({
            grant_type: 'client_credentials',
            client_id: credentials.clientId,
            client_secret: credentials.clientSecret,
            scope: scopes
        });

        return this.authenticate(`${environment.apiUrl}/authorize/token`, body.toString(), headers);
    }

    refreshToken(): Observable<OAuth2TokenResponse> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        const body = {
            remember_me_token: this.getStoredToken()
        };

        return this.authenticate(`${environment.apiUrl}/authorize/refresh`, body, headers);
    }

    /**
     * Déconnexion
     */
    logout(): Observable<void> {
        return new Observable<void>((observer) => {
            try {
                this.removeStoredToken();
                this.removeStoredUserInfo();
                this.tokenSubject.next(null);
                this.userInfoSubject.next(null);
                this.isAuthenticatedSubject.next(false);

                observer.next();
                observer.complete();
            } catch (error) {
                observer.error(error);
            }
        });
    }

    /**
     * Vérifier si l'utilisateur est connecté
     */
    isAuthenticated(): Observable<boolean> {
        const token = this.getStoredToken();
        if (!token) {
            this.isAuthenticatedSubject.next(false);
            return of(false);
        }

        return this.isTokenExpired().pipe(
            map(isExpired => !isExpired),
            tap(isValid => {
                if (this.isAuthenticatedSubject.value !== isValid) {
                    this.isAuthenticatedSubject.next(isValid);
                }
            })
        );
    }

    isAuthenticatedSync(): boolean {
        return this.isAuthenticatedSubject.value;
    }

    /**
     * Vérifier si l'utilisateur a un rôle spécifique
     */
    hasRole(role: string): boolean {
        const userInfo = this.userInfoSubject.value;
        if (!userInfo) return false;

        const hasRole = userInfo.authority === `ROLE_${role.toUpperCase()}`;

        return hasRole;
    }

    /**
     * Vérifier si l'utilisateur est admin
     */
    isAdmin(): boolean {
        const userInfo = this.userInfoSubject.value;
        const isAdmin = userInfo?.isAdmin || false;

        return isAdmin;
    }

    /**
     * Obtenir les informations utilisateur
     */
    getUserInfo(): UserInfo | null {
        return this.userInfoSubject.value;
    }

    /**
     * Obtenir le token actuel
     */
    getToken(): string | null {
        return this.tokenSubject.value;
    }

    // Méthodes privées pour la gestion du stockage
    private checkInitialAuth(): boolean {
        const decoded = this.decodeTokenExpirations();
        if (!decoded) return false;

        const now = Math.floor(Date.now() / 1000);
        const isExpired = decoded.exp < now;

        // Si exp dépassé → on est forcément déconnecté
        if (isExpired) return false;

        // Si only originally_expires dépassé → on est encore connecté mais on devra refresh dès que possible
        return true;
    }

    private storeToken(token: string): void {
        localStorage.setItem('oauth2_token', token);
    }

    private getStoredToken(): string | null {
        return localStorage.getItem('oauth2_token');
    }

    private removeStoredToken(): void {
        localStorage.removeItem('oauth2_token');
    }

    private storeUserInfo(userInfo: UserInfo): void {
        localStorage.setItem('user_info', JSON.stringify(userInfo));
    }

    private getStoredUserInfo(): UserInfo | null {
        const stored = localStorage.getItem('user_info');
        return stored ? JSON.parse(stored) : null;
    }

    private removeStoredUserInfo(): void {
        localStorage.removeItem('user_info');
    }

    private authenticate(url: string, body: any, headers: HttpHeaders): Observable<OAuth2TokenResponse> {
        return this.http.post<OAuth2TokenResponse>(url, body, { headers })
            .pipe(
                tap(response => this.handleAuthSuccess(response)),
                catchError(err => this.handleAuthError(err))
            );
    }

    private handleAuthSuccess(response: OAuth2TokenResponse): void {
        this.storeToken(response.access_token);
        this.extractAndStoreUserInfo(response.access_token);
        this.tokenSubject.next(response.access_token);
        this.isAuthenticatedSubject.next(true);
    }

    private handleAuthError(err: any): Observable<never> {
        console.error('[OAuth2Service] Erreur d’authentification :', err);
        this.logout();
        return throwError(() => err);
    }

    /**
     * Extraire les informations utilisateur du token JWT
     */
    private extractAndStoreUserInfo(token: string): void {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));

            const userInfo: UserInfo = {
                clientId: payload.client_id || payload.sub,
                scope: payload.scope || '',
                authority: payload.authority || '',
                isAdmin: payload.isAdmin || false
            };

            this.storeUserInfo(userInfo);
            this.userInfoSubject.next(userInfo);
        } catch (error) {
            console.error('[OAuth2Service] Erreur lors de l\'extraction des infos utilisateur:', error);
        }
    }

    /**
 * Méthode privée pour décoder le token et extraire les infos d'expiration
 */
    private decodeTokenExpirations(): { exp: number, originallyExpires: number } | null {
        try {
            const token = this.getStoredToken();
            if (token == null)
                return null;

            const payload = JSON.parse(atob(token.split('.')[1]));

            if (!payload.exp || !payload.originally_expires) {
                return null;
            }

            return {
                exp: payload.exp, // en secondes
                originallyExpires: Math.floor(payload.originally_expires / 1000) // conversion ms → s
            };
        } catch (error) {
            console.error('[OAuth2Service] Erreur lors du décodage du token:', error);
            return null;
        }
    }

    /**
     * Vérifier si le token est expiré
     */
    private isTokenExpired(): Observable<boolean> {
        const decoded = this.decodeTokenExpirations();
        if (!decoded) return of(true);

        const now = Math.floor(Date.now() / 1000);
        const isOriginalExpired = decoded.originallyExpires < now;
        const isExpired = decoded.exp < now;

        if (isOriginalExpired) {
            // ✅ mutualisation : refreshToken passe par authenticate()
            return this.refreshToken().pipe(
                map(() => false),
                catchError(() => of(true))
            );
        }

        // access_token toujours valable → juste vérifier exp
        return of(isExpired);
    }
}
