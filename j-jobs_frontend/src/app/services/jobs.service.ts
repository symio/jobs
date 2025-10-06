// src/app/services/jobs.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApiurlService } from '@app/environments/apiurl.service';
import { OAuth2Service } from './oauth2.service';
import { Observable } from 'rxjs';

export interface HalLink {
    href: string;
}

export interface HalLinks {
    [key: string]: HalLink;
}

export interface JobHasStatus {
    job_status: string;
    offer_status: string;
    applied_at: string;
    _links?: HalLinks;
}

export interface Job {
    position: string;
    description: string;
    compagny: string;
    city: string;
    from_official_dom: boolean;
    created_at: string;
    updated_at: string;
    job_has_statuses: JobHasStatus[];
    contract: string;
    workTime: string;
    workMode: string;
    offerStatus: string;
    _links?: HalLinks;
}

export interface PageInfo {
    size: number;
    total_elements: number;
    total_pages: number;
    number: number;
}

export interface SearchJobsRequest {
    contract: string | null;
    eventBefore: string | null;
    eventAfter: string | null;
    officialdom: boolean;
    offerStatus: string | null;
    sort: string;
    textual: string | null;
    workTime: string | null;
    workMode: string | null;
}

export interface GetJobsResponse {
    _embedded: {
        jobs: Job[];
    };
    _links?: HalLinks;
    page: PageInfo;
}

@Injectable({
    providedIn: 'root'
})
export class JobsService {
    private readonly apiUrl: string;

    constructor(
        private http: HttpClient,
        private apiurlService: ApiurlService,
        private oAuth2Service: OAuth2Service
    ) {
        this.apiUrl = `${this.apiurlService.getApiBaseUrl()}/jobs`;
    }

    searchJobs(search: SearchJobsRequest, page: number = 0, size: number = 3): Observable<GetJobsResponse> {
        const headers = this.oAuth2Service.buildRequestHeadersHal(true);
        const url = `${this.apiUrl}/search?page=${page}&size=${size}`;

        return this.http.post<GetJobsResponse>(url, search, { headers: headers });
    }
    
    getJobs(page: number = 0, size: number = 3): Observable<GetJobsResponse> {
        const headers = this.oAuth2Service.buildRequestHeadersHal(true);
        const url = `${this.apiUrl}?page=${page}&size=${size}`;

        return this.http.get<GetJobsResponse>(url, { headers: headers });
    }

    getJobByHalLink(href: string): Observable<Job> {
        const url = new URL(href);
        const headers = this.oAuth2Service.buildRequestHeadersHal(true);
        const fullUrl = `${this.apiurlService.getApiBaseUrl()}${url.pathname}`;

        return this.http.get<Job>(fullUrl, { headers: headers });
    }
}
