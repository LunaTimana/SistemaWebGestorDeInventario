import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductoService, DashboardResumen } from '../../services/producto';

@Component({ selector: 'app-dashboard', imports: [RouterLink], templateUrl: './dashboard.html', styleUrl: './dashboard.scss' })
export class Dashboard implements OnInit {
  private readonly service = inject(ProductoService);
  private readonly cdr = inject(ChangeDetectorRef);
  cargando = true;
  error = '';
  kpis = [
    { titulo: 'Productos registrados', valor: '—', etiqueta: 'Base de datos', detalle: 'productos', icono: 'bi-bag-check', tono: 'verde', tonoEtiqueta: 'verde' },
    { titulo: 'Valor comercial del stock', valor: '—', etiqueta: 'Actual', detalle: 'según precio de venta', icono: 'bi-cash-stack', tono: 'verde', tonoEtiqueta: 'verde' },
    { titulo: 'Alertas de stock bajo', valor: '—', etiqueta: 'Revisar', detalle: 'variantes bajo mínimo', icono: 'bi-exclamation-triangle', tono: 'rojo', tonoEtiqueta: 'rojo' },
    { titulo: 'Movimientos de hoy', valor: '—', etiqueta: 'Hoy', detalle: 'registros en MySQL', icono: 'bi-arrow-left-right', tono: 'naranja', tonoEtiqueta: 'verde' },
  ];
  categorias: { nombre:string; unidades:number; porcentaje:number; color:string }[] = [];
  alertas: { producto:string; sku:string; talla:string; stock:string; minimo:number }[] = [];
  transacciones: { titulo:string; detalle:string; color:string }[] = [];

  get puedeRegistrar(): boolean { try { return ['Administrador', 'Almacén'].includes(JSON.parse(sessionStorage.getItem('emsStoreUser') ?? '{}').rol ?? ''); } catch { return false; } }
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando = true; this.error = '';
    this.service.obtenerDashboard().subscribe({
      next: (d: DashboardResumen) => {
        this.kpis = [
          { ...this.kpis[0], valor: `${Number(d.totalProductos || 0).toLocaleString('es-PE')}`, detalle: 'productos registrados' },
          { ...this.kpis[1], valor: this.moneda(Number(d.valorComercial || 0)), detalle: `${Number(d.totalUnidades || 0).toLocaleString('es-PE')} unidades disponibles` },
          { ...this.kpis[2], valor: `${Number(d.alertasStockBajo || 0)}`, detalle: 'variantes bajo su mínimo' },
          { ...this.kpis[3], valor: `${Number(d.movimientosHoy || 0)}`, detalle: 'movimientos registrados hoy' },
        ];
        const colores = ['#b4ff00','#6b7280','#16a34a','#f59e0b','#38bdf8'];
        this.categorias = (d.categorias || []).map((c,i) => ({ ...c, color: colores[i % colores.length] }));
        this.alertas = (d.alertas || []).map(a => ({ producto:a.producto, sku:a.sku, talla:a.talla, stock:`${a.stock} uds`, minimo:a.minimo }));
        this.transacciones = (d.movimientos || []).map(m => ({ titulo:`${m.tipo === 'entrada' ? 'Entrada' : 'Salida'} · ${m.producto}`, detalle:`${m.tipo === 'entrada' ? '+' : '-'}${m.cantidad} uds · ${m.responsable} · ${m.documento || m.motivo}`, color:m.tipo === 'entrada' ? '#16c26b' : '#f59e0b' }));
        this.cargando = false;
        this.cdr.markForCheck();
      },
      error: e => { this.error = e?.error?.detail || 'No se pudo cargar el dashboard desde el backend.'; this.cargando = false; this.cdr.markForCheck(); }
    });
  }
  private moneda(v:number): string { return new Intl.NumberFormat('es-PE',{style:'currency',currency:'PEN',maximumFractionDigits:2}).format(v); }
}
