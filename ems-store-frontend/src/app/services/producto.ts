import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Producto {
  id: number;
  nombreModelo: string;
  marcaId: number;
  categoriaId: number;
  proveedorId?: number | null;
  diseno?: string | null;
  descripcion?: string | null;
  imagenUrl?: string | null;
  costoUnitario: number;
  precioVenta: number;
}

export interface VarianteProducto {
  id: number;
  productoId: number;
  talla: string;
  color: string;
  codigoUnico: string;
  stockActual: number;
  stockMinimo: number;
}

export interface Categoria {
  id: number;
  nombre: string;
}

export interface Marca {
  id: number;
  nombre: string;
}

export interface Proveedor {
  id: number;
  nombre: string;
  contacto?: string;
  telefono?: string;
  correo?: string;
}


export interface DashboardResumen {
  totalProductos: number; totalUnidades: number; valorComercial: number | string;
  alertasStockBajo: number; movimientosHoy: number;
  categorias: { nombre: string; unidades: number; porcentaje: number }[];
  alertas: { sku: string; producto: string; talla: string; stock: number; minimo: number; deficit: number }[];
  movimientos: { id: number; fecha: string; tipo: string; motivo: string; cantidad: number; sku: string; producto: string; responsable: string; documento?: string; destino_origen?: string }[];
}
export interface ResumenAlertas { criticos: number; riesgo: number; total: number; alertas: { varianteId:number; sku:string; producto:string; talla:string; color:string; stock:number; minimo:number; deficit:number; estado:string; ultimoIngreso:string|null }[] }
export interface ReporteCategoria { categoriaId:number; categoria:string; productos:number; unidades:number; valorVenta:number; valorCosto:number; precioPromedio:number }
export interface UsuarioSistema { id:number; nombre:string; apellido:string; correo:string; rol:string; estado:'activo'|'inactivo'; ultimaConexion:string|null; fechaCreacion:string|null }

export type ProductoPayload = Omit<Producto, 'id'>;
export type VariantePayload = Omit<VarianteProducto, 'id'>;

export interface MovimientoInventario {
  id: number; fecha: string; tipo: 'entrada' | 'salida'; motivo: string; cantidad: number;
  documento: string; destinoOrigen: string; comentario: string; sku: string; talla: string; color: string;
  producto: string; responsable: string; proveedor: string;
}

export interface MovimientoPayload {
  varianteId: number; tipo: 'entrada' | 'salida'; motivo: string; cantidad: number;
  proveedorId?: number | null; documento?: string; destinoOrigen?: string; comentario?: string;
}


@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  obtenerProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/productos`);
  }

  obtenerProducto(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/productos/${id}`);
  }

  crearProducto(producto: ProductoPayload): Observable<Producto> {
    return this.http.post<Producto>(`${this.apiUrl}/productos`, producto);
  }

  actualizarProducto(id: number, producto: ProductoPayload): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/productos/${id}`, producto);
  }

  crearProductoCompleto(datos: { producto: ProductoPayload; variante: Omit<VariantePayload, 'productoId'> }): Observable<{ producto: Producto; variante: VarianteProducto }> {
    return this.http.post<{ producto: Producto; variante: VarianteProducto }>(`${this.apiUrl}/productos/completo`, datos);
  }

  actualizarProductoCompleto(
    id: number,
    varianteId: number,
    datos: { producto: ProductoPayload; variante: Omit<VariantePayload, 'productoId'> }
  ): Observable<{ producto: Producto; variante: VarianteProducto }> {
    return this.http.put<{ producto: Producto; variante: VarianteProducto }>(`${this.apiUrl}/productos/${id}/completo/${varianteId}`, datos);
  }

  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/productos/${id}`);
  }

  obtenerVariantes(): Observable<VarianteProducto[]> {
    return this.http.get<VarianteProducto[]>(`${this.apiUrl}/variantes`);
  }

  obtenerVariantesProducto(productoId: number): Observable<VarianteProducto[]> {
    return this.http.get<VarianteProducto[]>(`${this.apiUrl}/variantes?productoId=${productoId}`);
  }

  crearVariante(variante: VariantePayload): Observable<VarianteProducto> {
    return this.http.post<VarianteProducto>(`${this.apiUrl}/variantes`, variante);
  }

  actualizarVariante(id: number, variante: VariantePayload): Observable<VarianteProducto> {
    return this.http.put<VarianteProducto>(`${this.apiUrl}/variantes/${id}`, variante);
  }

  eliminarVariante(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/variantes/${id}`);
  }

  obtenerCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.apiUrl}/categorias`);
  }

  obtenerMarcas(): Observable<Marca[]> {
    return this.http.get<Marca[]>(`${this.apiUrl}/marcas`);
  }

  obtenerProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${this.apiUrl}/proveedores`);
  }

  exportarInventarioExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/inventario/exportar/excel`, { responseType: 'blob' });
  }

  importarInventarioExcel(archivo: File): Observable<{ mensaje: string; filasActualizadas: number; movimientosRegistrados: number; filasOmitidas: number; errores: string[] }> {
    const form = new FormData();
    form.append('archivo', archivo);
    return this.http.post<{ mensaje: string; filasActualizadas: number; movimientosRegistrados: number; filasOmitidas: number; errores: string[] }>(`${this.apiUrl}/inventario/importar/excel`, form);
  }

  obtenerMovimientos(): Observable<MovimientoInventario[]> {
    return this.http.get<MovimientoInventario[]>(`${this.apiUrl}/movimientos`);
  }

  registrarMovimiento(movimiento: MovimientoPayload): Observable<{ mensaje: string; stockAnterior: number; stockNuevo: number; varianteId: number }> {
    return this.http.post<{ mensaje: string; stockAnterior: number; stockNuevo: number; varianteId: number }>(`${this.apiUrl}/movimientos`, movimiento);
  }

  obtenerDashboard(): Observable<DashboardResumen> { return this.http.get<DashboardResumen>(`${this.apiUrl}/dashboard/resumen`); }
  obtenerAlertas(): Observable<ResumenAlertas> { return this.http.get<ResumenAlertas>(`${this.apiUrl}/alertas`); }
  obtenerReporteCategorias(): Observable<ReporteCategoria[]> { return this.http.get<ReporteCategoria[]>(`${this.apiUrl}/reportes/stock-categoria`); }
  exportarMovimientosExcel(): Observable<Blob> { return this.http.get(`${this.apiUrl}/reportes/movimientos/excel`, { responseType: 'blob' }); }
  obtenerUsuarios(): Observable<UsuarioSistema[]> { return this.http.get<UsuarioSistema[]>(`${this.apiUrl}/usuarios`); }
  crearUsuario(usuario: { nombre:string; apellido:string; correo:string; rol:string; contrasena?:string }): Observable<{mensaje:string; contrasenaTemporal:string}> { return this.http.post<{mensaje:string; contrasenaTemporal:string}>(`${this.apiUrl}/usuarios`, usuario); }
  cambiarRolUsuario(id:number, rol:string): Observable<{mensaje:string}> { return this.http.put<{mensaje:string}>(`${this.apiUrl}/usuarios/${id}/rol`, {rol}); }
  cambiarEstadoUsuario(id:number, estado:'activo'|'inactivo'): Observable<{mensaje:string}> { return this.http.patch<{mensaje:string}>(`${this.apiUrl}/usuarios/${id}/estado`, {estado}); }
  obtenerMiCuenta(): Observable<{id:number;nombre:string;apellido:string;correo:string;rol:string;debe_cambiar_contrasena:boolean}> { return this.http.get<any>(`${this.apiUrl}/auth/me`); }
  actualizarMiCuenta(datos:{nombre:string;apellido:string;correo:string}): Observable<any> { return this.http.put<any>(`${this.apiUrl}/auth/me`,datos); }
  cambiarMiContrasena(actual:string,nueva:string): Observable<{mensaje:string}> { return this.http.put<{mensaje:string}>(`${this.apiUrl}/auth/me/contrasena`,{actual,nueva}); }
}
