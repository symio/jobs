// j-jobs_frontend/src/app/environments/environment.ts
import { env } from './.env';

export const environment = {
  production: true,
  version: env['npm_package_version'],
  defaultLanguage: 'fr-FR',
  supportedLanguages: ['fr-FR'],
  apiUrl: {
    httpPort: '',
    httpsPort: '',
    fallback: 'http://${window.location.hostname}/jobs',
    getPort(protocol: string): string {
      return protocol === 'https:' ? this.httpsPort : this.httpPort;
    },
  },
};
/**/
