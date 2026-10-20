import { Rol } from './rol';

export interface ItemMenu {
  ruta: string;
  titulo: string;
  subtitulo: string;
  encabezado: string; // título grande de la barra superior
  icono: string; // clase de Bootstrap Icons
  roles: Rol[];
}

const TODOS: Rol[] = ['ADMIN', 'ALMACEN', 'VENTAS'];

export const MENU: ItemMenu[] = [
  { ruta: 'dashboard', titulo: 'Dashboard', subtitulo: 'Inicio y Resumen', encabezado: 'Resumen de Operación', icono: 'bi-speedometer2', roles: TODOS },
  { ruta: 'inventario', titulo: 'Inventario', subtitulo: 'Catálogo de Productos', encabezado: 'Control de Productos', icono: 'bi-box-seam', roles: TODOS },
  { ruta: 'registrar-producto', titulo: 'Registrar Producto', subtitulo: 'Nuevo Ingreso', encabezado: 'Registro de Nuevo Producto', icono: 'bi-plus-circle', roles: ['ADMIN', 'ALMACEN'] },
  { ruta: 'movimientos', titulo: 'Movimientos', subtitulo: 'Entradas y Salidas', encabezado: 'Historial de Movimientos', icono: 'bi-arrow-left-right', roles: TODOS },
  { ruta: 'alertas', titulo: 'Alertas', subtitulo: 'Stock Mínimo Crítico', encabezado: 'Alertas de Stock Crítico', icono: 'bi-exclamation-triangle', roles: TODOS },
  { ruta: 'reportes', titulo: 'Reportes', subtitulo: 'Rendimiento y Ventas', encabezado: 'Módulo de Reportes', icono: 'bi-bar-chart-line', roles: ['ADMIN'] },
  { ruta: 'usuarios', titulo: 'Gestión de Usuarios', subtitulo: 'Accesos y Permisos', encabezado: 'Roles y Accesos de Personal', icono: 'bi-people', roles: ['ADMIN'] },
];

// Pantallas que no están en el menú pero cuelgan de una opción
export interface Subpantalla {
  prefijo: string;
  titulo: string;
  encabezado: string;
}

export const SUBPANTALLAS: Subpantalla[] = [
  { prefijo: '/inventario/detalle', titulo: 'Detalle', encabezado: 'Detalle del Producto' },
];