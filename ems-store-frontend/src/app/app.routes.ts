import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Layout } from './layout/layout/layout';
import { Dashboard } from './dashboard/dashboard/dashboard';
import { Inventario } from './inventario/inventario';
import { RegistrarProducto } from './registrar-producto/registrar-producto';
import { Movimientos } from './movimientos/movimientos';
import { Alertas } from './alertas/alertas';
import { Reportes } from './reportes/reportes';
import { Usuarios } from './usuarios/usuarios';

export const routes: Routes = [
  { path: 'login', component: Login, title: 'Iniciar sesión | EMS Store' },
  {
    path: '',
    component: Layout,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: Dashboard, title: 'Dashboard | EMS Store' },
      { path: 'inventario', component: Inventario, title: 'Inventario | EMS Store' },
      { path: 'registrar-producto', component: RegistrarProducto, title: 'Registrar producto | EMS Store' },
      { path: 'movimientos', component: Movimientos, title: 'Movimientos | EMS Store' },
      { path: 'alertas', component: Alertas, title: 'Alertas | EMS Store' },
      { path: 'reportes', component: Reportes, title: 'Reportes | EMS Store' },
      { path: 'usuarios', component: Usuarios, title: 'Gestión de Usuarios | EMS Store' },
    ],
  },
  { path: '**', redirectTo: '' },
];