import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Categoria, Marca, ProductoService, VarianteProducto, MovimientoInventario } from '../../services/producto';

@Component({
  selector: 'app-detalle-producto',
  imports: [RouterLink],
  templateUrl: './detalle-producto.html',
  styleUrl: './detalle-producto.scss',
})
export class DetalleProducto implements OnInit {
  cargando = true;
  error = '';
  producto = {
    marca: '',
    nombre: '',
    sku: '',
    categoria: '',
    talla: '',
    color: '',
    precio: 'S/ 0.00',
    stock: '0 unidades',
    estado: 'SIN STOCK',
    productoId: 0
  };

  historial: { titulo: string; fecha: string; cantidad: string; clase: string }[] = [];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly productoService: ProductoService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  get puedeEditar(): boolean { try { return ['Administrador', 'Almacén'].includes(JSON.parse(sessionStorage.getItem('emsStoreUser') ?? '{}').rol ?? ''); } catch { return false; } }

  ngOnInit(): void {
    const sku = this.route.snapshot.paramMap.get('id') ?? '';
    forkJoin({
      productos: this.productoService.obtenerProductos(),
      variantes: this.productoService.obtenerVariantes(),
      categorias: this.productoService.obtenerCategorias(),
      marcas: this.productoService.obtenerMarcas(),
      movimientos: this.productoService.obtenerMovimientos()
    }).subscribe({
      next: ({ productos, variantes, categorias, marcas, movimientos }) => {
        const variante: VarianteProducto | undefined = variantes.find(v => v.codigoUnico === sku);
        if (!variante) {
          this.error = `No se encontró el producto con SKU ${sku}.`;
        } else {
          const base = productos.find(p => p.id === variante.productoId);
          if (!base) {
            this.error = 'No se encontró el modelo asociado a esta variante.';
          } else {
            const categoria: Categoria | undefined = categorias.find(c => c.id === base.categoriaId);
            const marca: Marca | undefined = marcas.find(m => m.id === base.marcaId);
            const stock = Number(variante.stockActual ?? 0);
            const minimo = Number(variante.stockMinimo ?? 0);
            this.historial = (movimientos as MovimientoInventario[]).filter(m => m.sku === variante.codigoUnico).slice(0, 8).map(m => ({
              titulo: `${m.tipo === 'entrada' ? 'Entrada' : 'Salida'} · ${m.motivo}`,
              fecha: new Date(m.fecha).toLocaleString('es-PE'),
              cantidad: `${m.tipo === 'entrada' ? '+' : '-'}${m.cantidad} uds`,
              clase: m.tipo === 'entrada' ? 'verde' : 'rojo'
            }));
            this.producto = {
              marca: marca?.nombre ?? `Marca #${base.marcaId}`,
              nombre: base.nombreModelo,
              sku: variante.codigoUnico,
              categoria: categoria?.nombre ?? `Categoría #${base.categoriaId}`,
              talla: variante.talla,
              color: variante.color,
              precio: `S/ ${Number(base.precioVenta ?? 0).toFixed(2)}`,
              stock: `${stock} ${stock === 1 ? 'unidad' : 'unidades'}`,
              estado: stock <= 0 ? 'SIN STOCK' : stock <= minimo ? 'BAJO STOCK' : 'STOCK ADECUADO',
              productoId: base.id
            };
          }
        }
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar detalle:', err);
        this.error = 'No se pudo cargar el detalle desde la base de datos.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }
}
