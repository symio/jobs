// j-jobs_frontend/src/app.environments/apiurl.service.ts
import { Injectable } from '@angular/core';
import { environment } from './environment';

@Injectable({
  providedIn: 'root',
})
export class ApiurlService {
  getApiBaseUrl(): string {
    const { protocol, hostname } = window.location;

    if (!protocol || !hostname) {
      return environment.apiUrl.fallback;
    }

//    const apiPort = environment.apiUrl.getPort(protocol);
//    return `${protocol}//${hostname}:${apiPort}/api`;
    return `${protocol}//${hostname}/api`;
  }
}
