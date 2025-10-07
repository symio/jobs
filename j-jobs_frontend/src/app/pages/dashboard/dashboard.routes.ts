// src/app/pages/dashboard/dashboard.routes.ts
import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { JobDetailsComponent } from './job-details/job-details.component';
import { JobFormComponent } from './job-form-component/job-form.component';

export const routes: Routes = [
  {
    path: '',
    component: DashboardComponent,
  },
  {
    path: 'details',
    component: JobDetailsComponent,
  },
  {
    path: 'job-form',
    component: JobFormComponent,
  },
];
