// src/app/environments/environment.ts
import { env } from './.env';

export const environment = {
  production: false,
  version: env['npm_package_version'] + '-dev',
  defaultLanguage: 'fr-FR',
  supportedLanguages: ['fr-FR'],
  apiUrl: {
    httpPort: '8080',
    httpsPort: '8043',
    fallback: 'http://localhost:8080',
    getPort(protocol: string): string {
      return protocol === 'https:' ? this.httpsPort : this.httpPort;
    },
  },
};
