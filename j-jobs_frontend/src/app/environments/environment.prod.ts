import { env } from './.env';

export const environment = {
  production: true,
  version: env['npm_package_version'],
  defaultLanguage: 'fr-FR',
  supportedLanguages: ['fr-FR'],
  apiUrl: 'http://localhost:8080',
};
