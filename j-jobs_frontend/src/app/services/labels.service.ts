// src/app/services/labels.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ApiurlService } from '@app/environments/apiurl.service';
import { catchError, Observable, tap, throwError } from 'rxjs';

export type WorkTimeEnum = Record<string, string>;
export type OfferStatusEnum = Record<string, string>;
export type JobStatusEnum = Record<string, string>;
export type ContractEnum = Record<string, string>;
export type WorkModeEnum = Record<string, string>;

export interface LabelsResponse {
  WorkTimeEnum: WorkTimeEnum;
  OfferStatusEnum: OfferStatusEnum;
  JobStatusEnum: JobStatusEnum;
  ContractEnum: ContractEnum;
  WorkModeEnum: WorkModeEnum;
}

@Injectable({
  providedIn: 'root',
})
export class LabelsService {
  private readonly apiBaseUrl: string;

  constructor(
    private http: HttpClient,
    private apiurlService: ApiurlService,
  ) {
    this.apiBaseUrl = this.apiurlService.getApiBaseUrl();
  }

  fetchLabels(): Observable<LabelsResponse> {
    return this.http.get<LabelsResponse>(`${this.apiBaseUrl}/labels`).pipe(
      tap((response) => this.storeEnums(response)),
      catchError((err) => {
        console.error('[LabelsService] Erreur fetchLabels:', err);
        return throwError(() => err);
      }),
    );
  }

  private storeEnums(labels: LabelsResponse): void {
    (Object.keys(labels) as (keyof LabelsResponse)[]).forEach((key) => {
      localStorage.setItem(key, JSON.stringify(labels[key]));
    });
  }

  private getEnum<T>(enumName: keyof LabelsResponse): T | null {
    const data = localStorage.getItem(enumName);
    return data ? (JSON.parse(data) as T) : null;
  }

  // --- Getters ---
  getWorkTimeEnum(): WorkTimeEnum | null {
    return this.getEnum<WorkTimeEnum>('WorkTimeEnum');
  }
  getOfferStatusEnum(): OfferStatusEnum | null {
    return this.getEnum<OfferStatusEnum>('OfferStatusEnum');
  }
  getJobStatusEnum(): JobStatusEnum | null {
    return this.getEnum<JobStatusEnum>('JobStatusEnum');
  }
  getContractEnum(): ContractEnum | null {
    return this.getEnum<ContractEnum>('ContractEnum');
  }
  getWorkModeEnum(): WorkModeEnum | null {
    return this.getEnum<WorkModeEnum>('WorkModeEnum');
  }
}
