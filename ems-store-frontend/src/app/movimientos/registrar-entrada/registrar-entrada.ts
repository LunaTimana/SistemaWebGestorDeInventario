import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProductoService, Producto, VarianteProducto, Proveedor } from '../../services/producto';

interface OpcionVariante extends VarianteProducto { etiqueta: string; nombreProducto: string; }
@Component({ selector: 'app-registrar-entrada', imports: [RouterLink, FormsModule], templateUrl: './registrar-entrada.html', styleUrl: './registrar-entrada.scss' })
export class RegistrarEntrada implements OnInit {
  private readonly service = inject(ProductoService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  variantes: OpcionVariante[] = [];
  proveedores: Proveedor[] = [];
  varianteId: number | null = null;
  proveedorId: number | null = null;
  cantidad = 1;
  motivo = 'compra';
  documento = '';
  comentario = '';
  cargando = true;
  guardando = false;
  error = '';
  mensaje = '';
  ngOnInit(): void {
    forkJoin({ productos: this.service.obtenerProductos(), variantes: this.service.obtenerVariantes(), proveedores: this.service.obtenerProveedores() }).subscribe({
      next: ({ productos, variantes, proveedores }) => {
        this.variantes = variantes.map(v => ({ ...v, nombreProducto: productos.find(p => p.id === v.productoId)?.nombreModelo ?? `Producto #${v.productoId}`, etiqueta: `${v.codigoUnico} · ${productos.find(p => p.id === v.productoId)?.nombreModelo ?? `Producto #${v.productoId}`} · ${v.talla}/${v.color} (stock: ${v.stockActual})` }));
        this.proveedores = proveedores;
        const varianteSolicitada = Number(this.route.snapshot.queryParamMap.get('varianteId'));
        if (varianteSolicitada && this.variantes.some(v => v.id === varianteSolicitada)) this.varianteId = varianteSolicitada;
        this.cargando = false;
        this.cdr.markForCheck();
      }, error: () => { this.error = 'No se pudieron cargar productos y proveedores.'; this.cargando = false; this.cdr.markForCheck(); }
    });
  }
  get varianteSeleccionada(): OpcionVariante | undefined { return this.variantes.find(v => v.id === Number(this.varianteId)); }
  get stockProyectado(): number { return (this.varianteSeleccionada?.stockActual ?? 0) + Math.max(0, Number(this.cantidad) || 0); }
  registrar(): void {
    this.error = ''; this.mensaje = '';
    if (!this.varianteId || !this.cantidad || this.cantidad < 1 || !Number.isInteger(Number(this.cantidad))) { this.error = 'Selecciona una variante e ingresa una cantidad entera mayor que cero.'; return; }
    if (this.motivo === 'compra' && !this.proveedorId) { this.error = 'Selecciona el proveedor de la compra.'; return; }
    this.guardando = true;
    this.service.registrarMovimiento({ varianteId: Number(this.varianteId), tipo: 'entrada', motivo: this.motivo, cantidad: Number(this.cantidad), proveedorId: this.motivo === 'compra' ? Number(this.proveedorId) : null, documento: this.documento, comentario: this.comentario }).subscribe({
      next: r => { this.guardando = false; this.mensaje = `${r.mensaje} Stock actualizado: ${r.stockAnterior} → ${r.stockNuevo}.`; void this.router.navigate(['/movimientos']); },
      error: e => { this.guardando = false; this.error = e?.error?.detail || 'No se pudo registrar la entrada.'; }
    });
  }
}
