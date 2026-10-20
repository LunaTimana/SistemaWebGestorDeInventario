import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Layout } from './layout/layout/layout';
import { Dashboard } from './dashboard/dashboard/dashboard';

export const routes: Routes = [
  { path: 'login', component: Login, title: 'Iniciar sesión | EMS Store' },
  {
    path: '',
    component: Layout,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: Dashboard, title: 'Dashboard | EMS Store' },
    ],
  },
  { path: '**', redirectTo: '' },
];