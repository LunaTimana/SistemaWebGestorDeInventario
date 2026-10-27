import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import {
  Categoria,
  Marca,
  Producto,
  ProductoService,
  VarianteProducto
} from '../services/producto';

interface FilaInventario {
  id: number;
  varianteId: number;
  sku: string;
  marca: string;
  nombre: string;
  categoria: string;
  talla: string;
  color: string;
  stock: number;
  precio: number;
  estado: string;
}

@Component({
  selector: 'app-inventario',
  imports: [RouterLink, FormsModule],
  templateUrl: './inventario.html',
  styleUrl: './inventario.scss',
})
export class Inventario implements OnInit {
  productos: FilaInventario[] = [];
  categorias: Categoria[] = [];
  marcas: Marca[] = [];
  cargando = true;
  error = '';
  mensaje = '';
  busqueda = '';
  categoriaSeleccionada = '';
  tallaSeleccionada = '';
  eliminandoId: number | null = null;
  exportandoExcel = false;
  importandoExcel = false;

  private categoriasPorId: Record<number, string> = {};
  private marcasPorId: Record<number, string> = {};

  constructor(
    private readonly productoService: ProductoService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  get puedeEditar(): boolean { return ['Administrador', 'Almacén'].includes(this.rolActual()); }
  get puedeEliminar(): boolean { return this.rolActual() === 'Administrador'; }
  private rolActual(): string { try { return JSON.parse(sessionStorage.getItem('emsStoreUser') ?? '{}').rol ?? ''; } catch { return ''; } }

  ngOnInit(): void {
    this.cargarInventario();
  }

  cargarInventario(): void {
    this.cargando = true;
    this.error = '';
    this.cdr.detectChanges();

    forkJoin({
      productos: this.productoService.obtenerProductos(),
      variantes: this.productoService.obtenerVariantes(),
      categorias: this.productoService.obtenerCategorias(),
      marcas: this.productoService.obtenerMarcas()
    }).subscribe({
      next: ({ productos, variantes, categorias, marcas }) => {
        this.categorias = categorias;
        this.marcas = marcas;
        this.categoriasPorId = Object.fromEntries(categorias.map(c => [c.id, c.nombre]));
        this.marcasPorId = Object.fromEntries(marcas.map(m => [m.id, m.nombre]));

        this.productos = productos.flatMap((producto: Producto) =>
          variantes
            .filter((variante: VarianteProducto) => variante.productoId === producto.id)
            .map((variante: VarianteProducto) => ({
              id: producto.id,
              varianteId: variante.id,
              sku: variante.codigoUnico,
              marca: this.marcasPorId[producto.marcaId] ?? `Marca #${producto.marcaId}`,
              nombre: producto.nombreModelo,
              categoria: this.categoriasPorId[producto.categoriaId] ?? `Categoría #${producto.categoriaId}`,
              talla: variante.talla,
              color: variante.color,
              stock: Number(variante.stockActual ?? 0),
              precio: Number(producto.precioVenta ?? 0),
              estado: this.obtenerEstado(Number(variante.stockActual ?? 0), Number(variante.stockMinimo ?? 0))
            }))
        );

        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar el inventario:', err);
        this.error = 'No se pudo cargar el inventario. Verifica que el backend y la base de datos estén activos.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  obtenerEstado(stock: number, minimo: number): string {
    if (stock <= 0) return 'Sin Stock';
    if (stock <= minimo) return 'Bajo Stock';
    return 'Disponible';
  }

  get productosFiltrados(): FilaInventario[] {
    const texto = this.busqueda.trim().toLocaleLowerCase();
    return this.productos.filter(p => {
      const coincideBusqueda =
        p.nombre.toLocaleLowerCase().includes(texto) ||
        p.sku.toLocaleLowerCase().includes(texto) ||
        p.marca.toLocaleLowerCase().includes(texto);
      const coincideCategoria = !this.categoriaSeleccionada || p.categoria === this.categoriaSeleccionada;
      const coincideTalla = !this.tallaSeleccionada || p.talla.toLocaleLowerCase() === this.tallaSeleccionada.toLocaleLowerCase();
      return coincideBusqueda && coincideCategoria && coincideTalla;
    });
  }

  eliminarProducto(producto: FilaInventario): void {
    const confirmado = confirm(
      `¿Eliminar "${producto.nombre}" y todas sus variantes (incluida ${producto.sku})? Esta acción no se puede deshacer.`
    );
    if (!confirmado) return;

    this.eliminandoId = producto.id;
    this.error = '';
    this.mensaje = '';
    this.productoService.eliminarProducto(producto.id).subscribe({
      next: () => {
        this.mensaje = `Se eliminó "${producto.nombre}".`;
        this.eliminandoId = null;
        this.cargarInventario();
      },
      error: (err) => {
        console.error('No se pudo eliminar el producto:', err);
        this.error = err?.status === 409
          ? 'No se puede eliminar este producto porque tiene movimientos de inventario asociados.'
          : 'No se pudo eliminar el producto. Revisa el backend e inténtalo nuevamente.';
        this.eliminandoId = null;
        this.cdr.detectChanges();
      }
    });
  }

  editarProducto(producto: FilaInventario): void {
    this.router.navigate(['/inventario/editar', producto.sku]);
  }

  importarExcel(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;
    this.importandoExcel = true;
    this.error = '';
    this.mensaje = '';
    this.productoService.importarInventarioExcel(archivo).subscribe({
      next: (resultado) => {
        this.importandoExcel = false;
        this.mensaje = `${resultado.mensaje} Filas actualizadas: ${resultado.filasActualizadas}; movimientos registrados: ${resultado.movimientosRegistrados}; filas omitidas: ${resultado.filasOmitidas}.`;
        if (resultado.errores.length) this.error = resultado.errores.slice(0, 8).join(' | ') + (resultado.errores.length > 8 ? ' | Hay más errores en el resultado.' : '');
        this.cargarInventario();
        input.value = '';
      },
      error: (err) => {
        this.importandoExcel = false;
        this.error = err?.error?.detail || err?.error?.message || 'No se pudo importar el Excel. Verifica que incluya las columnas SKU, Stock actual y Stock mínimo.';
        input.value = '';
        this.cdr.detectChanges();
      }
    });
  }

  exportarExcel(): void {
    if (this.exportandoExcel) return;
    this.exportandoExcel = true;
    this.error = '';
    this.mensaje = '';
    this.productoService.exportarInventarioExcel().subscribe({
      next: (archivo) => {
        const url = URL.createObjectURL(archivo);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `EMS-Store-Inventario-${new Date().toISOString().slice(0, 10)}.xlsx`;
        enlace.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
        this.exportandoExcel = false;
        this.mensaje = 'El archivo Excel del inventario se descargó correctamente.';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al exportar el inventario a Excel:', err);
        this.exportandoExcel = false;
        this.error = 'No se pudo exportar el inventario. Verifica que el backend esté activo y revisa la consola del servidor.';
        this.cdr.detectChanges();
      }
    });
  }
}
