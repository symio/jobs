import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiurlService } from '@app/environments/apiurl.service';
import { catchError, Observable, tap, throwError, forkJoin, of } from 'rxjs';
import { OAuth2Service } from './oauth2.service';
import { LabelsService, OfferStatusEnum } from './labels.service'; // 👈 Import de OfferStatusEnum

export type StatCount = number;

export interface Stats {
    EN_COURS: StatCount;
    EN_ATTENTE: StatCount;
    REFUSE: StatCount;
    ENTRETIEN: StatCount;
}

@Injectable({
    providedIn: 'root',
})
export class StatusStatsService {
    private readonly apiBaseUrl: string;
    private readonly STATS_STORAGE_KEY = 'StatusStats';

    constructor(
        private http: HttpClient,
        private apiurlService: ApiurlService,
        private oAuth2Service: OAuth2Service,
        private labelsService: LabelsService,
    ) {
        this.apiBaseUrl = this.apiurlService.getApiBaseUrl();
    }

    fetchStats(): Observable<Stats> {
        const url = `${this.apiBaseUrl}/jobs/statuscount`;
        const headers = this.oAuth2Service.buildRequestHeaders(true);

        return this.http.get<Stats>(url, { headers: headers });
    }
        
    fetchStatsIndividually(): Observable<Stats> {
        const statuses = ['EN_COURS', 'EN_ATTENTE', 'REFUSE', 'ENTRETIEN'] as const;

        const statsObservables: Record<keyof Stats, Observable<number>> = {
            EN_COURS: this.fetchCountForStatus('EN_COURS'),
            EN_ATTENTE: this.fetchCountForStatus('EN_ATTENTE'),
            REFUSE: this.fetchCountForStatus('REFUSE'),
            ENTRETIEN: this.fetchCountForStatus('ENTRETIEN'),
        };

        return forkJoin(statsObservables).pipe(
            tap((response: Stats) => this.storeStats(response)),
            catchError((err) => {
                console.error('[StatusStatsService] Erreur fetchStats:', err);
                return of(
                    this.getStatsFromStorage() || {
                        EN_COURS: 0,
                        EN_ATTENTE: 0,
                        REFUSE: 0,
                        ENTRETIEN: 0,
                    },
                );
            }),
        );
    }
    
    private fetchCountForStatus(status: keyof Stats): Observable<number> {
        const url = `${this.apiBaseUrl}/jobs/countbystatus?status=${status}`;
        const headers = this.oAuth2Service.buildRequestHeaders(true);

        return this.http.get<number>(url, { headers: headers });
    }

    private storeStats(stats: Stats): void {
        localStorage.setItem(this.STATS_STORAGE_KEY, JSON.stringify(stats));
    }

    private getStatsFromStorage(): Stats | null {
        const data = localStorage.getItem(this.STATS_STORAGE_KEY);
        return data ? (JSON.parse(data) as Stats) : null;
    }

    getStats(): Stats | null {
        return this.getStatsFromStorage();
    }

    getEnCoursCount(): StatCount {
        return this.getStatsFromStorage()?.EN_COURS ?? 0;
    }

    getEnAttenteCount(): StatCount {
        return this.getStatsFromStorage()?.EN_ATTENTE ?? 0;
    }

    getRefuseCount(): StatCount {
        return this.getStatsFromStorage()?.REFUSE ?? 0;
    }

    getEntretienCount(): StatCount {
        return this.getStatsFromStorage()?.ENTRETIEN ?? 0;
    }
}
