import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import {
  Categoria,
  Marca,
  Producto,
  ProductoPayload,
  ProductoService,
  Proveedor,
  VarianteProducto
} from '../services/producto';

@Component({
  selector: 'app-registrar-producto',
  imports: [RouterLink, FormsModule, DecimalPipe],
  templateUrl: './registrar-producto.html',
  styleUrl: './registrar-producto.scss',
})
export class RegistrarProducto implements OnInit {
  categorias: Categoria[] = [];
  marcas: Marca[] = [];
  proveedores: Proveedor[] = [];

  cargando = true;
  guardando = false;
  editando = false;
  modo: 'nuevo' | 'variante' = 'nuevo';
  productos: Producto[] = [];
  productoExistenteId: number | null = null;
  error = '';
  mensaje = '';

  private productoId: number | null = null;
  private varianteId: number | null = null;
  private readonly sufijoSku = Date.now().toString().slice(-6);

  productoForm = {
    nombreModelo: '',
    marcaId: 0,
    categoriaId: 0,
    proveedorId: null as number | null,
    diseno: '',
    descripcion: '',
    imagenUrl: '',
    costoUnitario: 0,
    precioVenta: 0
  };

  varianteForm = {
    talla: 'M',
    genero: 'Hombre',
    color: '',
    codigoUnico: '',
    stockActual: 0,
    stockMinimo: 5
  };

  constructor(
    private readonly productoService: ProductoService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const sku = this.route.snapshot.paramMap.get('sku');
    this.editando = !!sku;
    this.cargando = true;

    forkJoin({
      productos: this.productoService.obtenerProductos(),
      variantes: this.productoService.obtenerVariantes(),
      categorias: this.productoService.obtenerCategorias(),
      marcas: this.productoService.obtenerMarcas(),
      proveedores: this.productoService.obtenerProveedores()
    }).subscribe({
      next: ({ productos, variantes, categorias, marcas, proveedores }) => {
        this.categorias = categorias;
        this.marcas = marcas;
        this.proveedores = proveedores;
        this.productos = productos;

        if (sku) {
          const variante = variantes.find(v => v.codigoUnico === sku);
          if (!variante) {
            this.error = `No se encontró la variante con SKU ${sku}.`;
          } else {
            const producto = productos.find(p => p.id === variante.productoId);
            if (!producto) {
              this.error = 'No se encontró el producto asociado a esta variante.';
            } else {
              this.productoId = producto.id;
              this.varianteId = variante.id;
              this.productoForm = {
                nombreModelo: producto.nombreModelo ?? '',
                marcaId: Number(producto.marcaId),
                categoriaId: Number(producto.categoriaId),
                proveedorId: producto.proveedorId == null ? null : Number(producto.proveedorId),
                diseno: producto.diseno ?? '',
                descripcion: producto.descripcion ?? '',
                imagenUrl: producto.imagenUrl ?? '',
                costoUnitario: Number(producto.costoUnitario ?? 0),
                precioVenta: Number(producto.precioVenta ?? 0)
              };
              this.varianteForm = {
                talla: variante.talla,
                genero: variante.genero ?? 'Hombre',
                color: variante.color,
                codigoUnico: variante.codigoUnico,
                stockActual: Number(variante.stockActual ?? 0),
                stockMinimo: Number(variante.stockMinimo ?? 0)
              };
            }
          }
        } else {
          this.generarSku();
        }

        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar el formulario:', err);
        this.error = 'No se pudieron cargar las categorías, marcas o proveedores. Comprueba la conexión con el backend.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  generarSku(): void {
    if (this.editando) return;
    const marca = this.marcas.find(m => m.id === Number(this.productoForm.marcaId))?.nombre ?? 'EMS';
    const marcaCodigo = marca.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 3) || 'EMS';
    const nombreCodigo = this.productoForm.nombreModelo.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 5) || 'ITEM';
    const genero = (this.varianteForm.genero || 'Hombre').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase().slice(0, 1);
    const talla = (this.varianteForm.talla || 'UN').toUpperCase().replace(/\s/g, '');
    const color = this.varianteForm.color.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 3) || 'COL';
    this.varianteForm.codigoUnico = `${marcaCodigo}-${nombreCodigo}-${genero}-${talla}-${color}-${this.sufijoSku}`;
  }

  seleccionarProductoExistente(id: number | null): void {
    this.productoExistenteId = id;
    const producto = this.productos.find(p => p.id === Number(id));
    if (!producto) return;
    this.productoForm = {
      nombreModelo: producto.nombreModelo,
      marcaId: producto.marcaId,
      categoriaId: producto.categoriaId,
      proveedorId: producto.proveedorId ?? null,
      diseno: producto.diseno ?? '',
      descripcion: producto.descripcion ?? '',
      imagenUrl: producto.imagenUrl ?? '',
      costoUnitario: Number(producto.costoUnitario ?? 0),
      precioVenta: Number(producto.precioVenta ?? 0)
    };
    this.generarSku();
  }

  marcaNombre(marcaId: number): string {
    return this.marcas.find(m => m.id === Number(marcaId))?.nombre ?? '';
  }

  get gananciaUnidad(): number {
    return Number(this.productoForm.precioVenta || 0) - Number(this.productoForm.costoUnitario || 0);
  }

  get margenPorcentaje(): number {
    const precio = Number(this.productoForm.precioVenta || 0);
    return precio > 0 ? (this.gananciaUnidad / precio) * 100 : 0;
  }

  get valorInventario(): number {
    return Number(this.productoForm.costoUnitario || 0) * Number(this.varianteForm.stockActual || 0);
  }

  guardarProducto(): void {
    this.error = '';
    this.mensaje = '';

    const productoInvalido = (this.editando || this.modo === 'nuevo') && (!this.productoForm.nombreModelo.trim() || !this.productoForm.marcaId || !this.productoForm.categoriaId);
    if (productoInvalido ||
        !this.varianteForm.talla || !this.varianteForm.genero || !this.varianteForm.color.trim() ||
        Number(this.productoForm.costoUnitario) < 0 || Number(this.productoForm.precioVenta) < 0 ||
        Number(this.varianteForm.stockActual) < 0 || Number(this.varianteForm.stockMinimo) < 0) {
      this.error = 'Completa los campos obligatorios y utiliza valores numéricos iguales o mayores que cero.';
      return;
    }

    this.generarSku();
    const producto: ProductoPayload = {
      nombreModelo: this.productoForm.nombreModelo.trim(),
      marcaId: Number(this.productoForm.marcaId),
      categoriaId: Number(this.productoForm.categoriaId),
      proveedorId: this.productoForm.proveedorId ? Number(this.productoForm.proveedorId) : null,
      diseno: this.productoForm.diseno.trim() || null,
      descripcion: this.productoForm.descripcion.trim() || null,
      imagenUrl: this.productoForm.imagenUrl.trim() || null,
      costoUnitario: Number(this.productoForm.costoUnitario),
      precioVenta: Number(this.productoForm.precioVenta)
    };
    const variante = {
      talla: this.varianteForm.talla.trim(),
      genero: this.varianteForm.genero,
      color: this.varianteForm.color.trim(),
      codigoUnico: this.varianteForm.codigoUnico,
      stockActual: Number(this.varianteForm.stockActual),
      stockMinimo: Number(this.varianteForm.stockMinimo)
    };

    this.guardando = true;
    this.cdr.detectChanges();

    if (!this.editando && this.modo === 'variante') {
      if (!this.productoExistenteId) {
        this.error = 'Selecciona el producto al que quieres agregar esta variante.';
        this.guardando = false;
        return;
      }
      const existente = this.productos.find(p => p.id === this.productoExistenteId)!;
      this.productoService.crearVariante({
        productoId: existente.id,
        talla: variante.talla,
        genero: variante.genero,
        color: variante.color,
        codigoUnico: variante.codigoUnico,
        stockActual: 0,
        stockMinimo: variante.stockMinimo
      }).subscribe({
        next: creada => {
          const registrarStock = variante.stockActual > 0
            ? this.productoService.registrarMovimiento({ varianteId: creada.id, tipo: 'entrada', motivo: 'ajuste', cantidad: variante.stockActual, documento: 'Stock inicial', comentario: 'Stock inicial al agregar variante' })
            : null;
          if (!registrarStock) { this.router.navigate(['/inventario']); return; }
          registrarStock.subscribe({
            next: () => this.router.navigate(['/inventario']),
            error: err => {
              const detalle = err?.error?.detail || err?.error?.message;
              this.error = detalle ? `La variante se creó, pero no se pudo registrar el stock: ${detalle}` : 'La variante se creó, pero no se pudo registrar el stock inicial.';
              this.guardando = false;
              this.cdr.detectChanges();
            }
          });
        },
        error: err => {
          const detalle = err?.error?.detail || err?.error?.message;
          this.error = detalle ? `No se pudo agregar la variante: ${detalle}` : 'No se pudo agregar la variante. Verifica que el SKU y la combinación de talla, género y color no estén repetidos.';
          this.guardando = false;
          this.cdr.detectChanges();
        }
      });
      return;
    }

    const solicitud = this.editando && this.productoId !== null && this.varianteId !== null
      ? this.productoService.actualizarProductoCompleto(this.productoId, this.varianteId, { producto, variante })
      : this.productoService.crearProductoCompleto({ producto, variante });

    solicitud.subscribe({
      next: () => {
        this.guardando = false;
        this.cdr.detectChanges();
        this.router.navigate(['/inventario']);
      },
      error: (err) => {
        console.error('Error al guardar producto:', err);
        const detalle = err?.error?.detail || err?.error?.message;
        this.error = detalle
          ? `No se pudo guardar: ${detalle}`
          : 'No se pudo guardar el producto. Verifica que el SKU no esté duplicado y que los datos sean válidos.';
        this.guardando = false;
        this.cdr.detectChanges();
      }
    });
  }
}
