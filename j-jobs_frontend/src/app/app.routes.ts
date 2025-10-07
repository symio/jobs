// src/app/app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./pages/pages.routes').then((r) => r.routes),
  },
  // Route pour les utilisateurs connectés (à ajouter plus tard)
  // {
  //   path: 'dashboard',
  //   loadChildren: () => import('./dashboard/dashboard.routes').then(r => r.routes),
  //   canActivate: [authGuard] // guard pour vérifier l'authentification
  // },
  {
    path: '**',
    redirectTo: '', // redirige vers la landing page si route inconnue
  },
];
