import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProductoService, VarianteProducto } from '../../services/producto';

interface OpcionVariante extends VarianteProducto { etiqueta: string; }
@Component({ selector: 'app-registrar-salida', imports: [RouterLink, FormsModule], templateUrl: './registrar-salida.html', styleUrl: './registrar-salida.scss' })
export class RegistrarSalida implements OnInit {
  private readonly service = inject(ProductoService);
  private readonly router = inject(Router);
  variantes: OpcionVariante[] = [];
  varianteId: number | null = null;
  cantidad = 1;
  motivo = 'venta';
  destinoOrigen = '';
  documento = '';
  comentario = '';
  cargando = true;
  guardando = false;
  error = '';
  ngOnInit(): void {
    forkJoin({ productos: this.service.obtenerProductos(), variantes: this.service.obtenerVariantes() }).subscribe({
      next: ({ productos, variantes }) => { this.variantes = variantes.map(v => ({ ...v, etiqueta: `${v.codigoUnico} · ${productos.find(p => p.id === v.productoId)?.nombreModelo ?? `Producto #${v.productoId}`} · ${v.talla}/${v.color} (stock: ${v.stockActual})` })); this.cargando = false; },
      error: () => { this.error = 'No se pudieron cargar las variantes.'; this.cargando = false; }
    });
  }
  get varianteSeleccionada(): OpcionVariante | undefined { return this.variantes.find(v => v.id === Number(this.varianteId)); }
  get stockProyectado(): number { return (this.varianteSeleccionada?.stockActual ?? 0) - (Number(this.cantidad) || 0); }
  get hayError(): boolean { return !!this.varianteSeleccionada && (this.cantidad < 1 || !Number.isInteger(Number(this.cantidad)) || this.stockProyectado < 0); }
  registrar(): void {
    this.error = '';
    if (!this.varianteId || !this.cantidad || this.cantidad < 1 || !Number.isInteger(Number(this.cantidad))) { this.error = 'Selecciona una variante e ingresa una cantidad entera mayor que cero.'; return; }
    if (this.hayError) { this.error = `Stock insuficiente. Disponible: ${this.varianteSeleccionada?.stockActual ?? 0} unidades.`; return; }
    this.guardando = true;
    this.service.registrarMovimiento({ varianteId: Number(this.varianteId), tipo: 'salida', motivo: this.motivo, cantidad: Number(this.cantidad), destinoOrigen: this.destinoOrigen, documento: this.documento, comentario: this.comentario }).subscribe({
      next: () => { this.guardando = false; void this.router.navigate(['/movimientos']); },
      error: e => { this.guardando = false; this.error = e?.error?.detail || 'No se pudo registrar la salida.'; }
    });
  }
}
