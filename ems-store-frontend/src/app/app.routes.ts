import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { authGuard } from './auth/auth-guard';
import { roleGuard } from './auth/role-guard';
import { Layout } from './layout/layout/layout';
import { Dashboard } from './dashboard/dashboard/dashboard';
import { Inventario } from './inventario/inventario';
import { RegistrarProducto } from './registrar-producto/registrar-producto';
import { Movimientos } from './movimientos/movimientos';
import { Alertas } from './alertas/alertas';
import { Reportes } from './reportes/reportes';
import { Usuarios } from './usuarios/usuarios';
import { DetalleProducto } from './inventario/detalle-producto/detalle-producto';
import { RegistrarEntrada } from './movimientos/registrar-entrada/registrar-entrada';
import { RegistrarSalida } from './movimientos/registrar-salida/registrar-salida';
import { ConfiguracionCuenta } from './configuracion/configuracion-cuenta/configuracion-cuenta';

export const routes: Routes = [
  { path: 'login', component: Login, title: 'Iniciar sesión | EMS Store' },
  {
    path: '', component: Layout, canActivate: [authGuard], children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: Dashboard, title: 'Dashboard | EMS Store' },
      { path: 'inventario', component: Inventario, title: 'Inventario | EMS Store' },
      { path: 'registrar-producto', component: RegistrarProducto, canActivate: [roleGuard], data: { roles: ['Administrador', 'Almacén'] }, title: 'Registrar producto | EMS Store' },
      { path: 'movimientos', component: Movimientos, title: 'Movimientos | EMS Store' },
      { path: 'alertas', component: Alertas, title: 'Alertas | EMS Store' },
      { path: 'reportes', component: Reportes, canActivate: [roleGuard], data: { roles: ['Administrador'] }, title: 'Reportes | EMS Store' },
      { path: 'usuarios', component: Usuarios, canActivate: [roleGuard], data: { roles: ['Administrador'] }, title: 'Gestión de Usuarios | EMS Store' },
      { path: 'inventario/detalle/:id', component: DetalleProducto, title: 'Detalle del Producto | EMS Store' },
      { path: 'inventario/editar/:sku', component: RegistrarProducto, canActivate: [roleGuard], data: { roles: ['Administrador', 'Almacén'] }, title: 'Editar producto | EMS Store' },
      { path: 'movimientos/entrada', component: RegistrarEntrada, title: 'Registrar entrada de stock | EMS Store' },
      { path: 'movimientos/salida', component: RegistrarSalida, title: 'Registrar salida de stock | EMS Store' },
      { path: 'configuracion', component: ConfiguracionCuenta, title: 'Configuración de cuenta | EMS Store' },
    ],
  },
  { path: '**', redirectTo: '' },
];
