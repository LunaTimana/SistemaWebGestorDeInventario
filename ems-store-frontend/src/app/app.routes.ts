import { Routes } from '@angular/router';
import { Login } from './auth/login/login';

export const routes: Routes = [
  { path: 'login', component: Login, title: 'Iniciar sesión | EMS Store' },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
];