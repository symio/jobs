// src/app/pages/pages.routes.ts
import { Routes } from '@angular/router';
import { LandingPageComponent } from './landing-page/landing-page.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { AuthGuard } from '@app/guards/auth.guard';
import { MentionsLegalesComponent } from './mentions-legales/mentions-legales.component';
import { RgpdComponent } from './rgpd/rgpd.component';
import { AideComponent } from './aide/aide.component';
import { ActivateComponent } from './register/activate/activate.component';
import { DeactivateComponent } from './register/deactivate/deactivate.component';

export const routes: Routes = [
  {
    path: '',
    component: LandingPageComponent,
  },
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'register',
    component: RegisterComponent,
  },
  {
    path: 'register/activate',
    component: ActivateComponent,
  },
  {
    path: 'register/deactivate',
    component: DeactivateComponent,
  },
  {
    path: 'mentions-legales',
    component: MentionsLegalesComponent,
  },
  {
    path: 'confidentialite',
    component: RgpdComponent,
  },
  {
    path: 'aide',
    component: AideComponent,
  },
  {
    path: 'dashboard',
    loadChildren: () =>
      import('./dashboard/dashboard.routes').then((r) => r.routes),
    canActivate: [AuthGuard],
  },
];
