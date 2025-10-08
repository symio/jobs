// j-jobs_frontend/src/app/environments/environment.ts
import { env } from './.env';

export const environment = {
  production: true,
  version: env['npm_package_version'],
  defaultLanguage: 'fr-FR',
  supportedLanguages: ['fr-FR'],
  apiUrl: {
    httpPort: '4200',
    httpsPort: '4243',
    fallback: 'http://${window.location.hostname}:4200/jobs',
    getPort(protocol: string): string {
      return protocol === 'https:' ? this.httpsPort : this.httpPort;
    },
  },
};
/**/
