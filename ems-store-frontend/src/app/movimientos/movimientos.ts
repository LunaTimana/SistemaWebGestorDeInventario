import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductoService, MovimientoInventario } from '../services/producto';

interface FilaMovimiento { id: number; fecha: string; tipo: string; clase: string; sku: string; producto: string; cantidad: string; responsable: string; origen: string; }
@Component({ selector: 'app-movimientos', imports: [RouterLink, FormsModule], templateUrl: './movimientos.html', styleUrl: './movimientos.scss' })
export class Movimientos implements OnInit {
  private readonly service = inject(ProductoService);
  private readonly cdr = inject(ChangeDetectorRef);
  cargando = true;
  error = '';
  busqueda = '';
  tipoFiltro = '';
  resumen = [
    { titulo: 'Total entradas', subtitulo: 'Ingresos registrados', insignia: '0 uds', valor: '0 uds', detalle: 'Sin movimientos de entrada.', icono: 'bi-plus-circle', tono: 'verde' },
    { titulo: 'Total salidas', subtitulo: 'Salidas registradas', insignia: '0 uds', valor: '0 uds', detalle: 'Sin movimientos de salida.', icono: 'bi-dash-circle', tono: 'rojo' },
    { titulo: 'Ajustes y pérdidas', subtitulo: 'Movimientos no comerciales', insignia: '0 uds', valor: '0 uds', detalle: 'Sin ajustes o pérdidas.', icono: 'bi-search', tono: 'naranja' },
  ];
  movimientos: FilaMovimiento[] = [];
  get movimientosFiltrados(): FilaMovimiento[] {
    const q = this.busqueda.trim().toLocaleLowerCase();
    return this.movimientos.filter(m => (!this.tipoFiltro || m.tipo.toLowerCase() === this.tipoFiltro) && (!q || `${m.sku} ${m.producto} ${m.responsable} ${m.origen}`.toLocaleLowerCase().includes(q)));
  }
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando = true; this.error = '';
    this.service.obtenerMovimientos().subscribe({
      next: rows => {
        this.movimientos = rows.map((m: MovimientoInventario) => ({
          id: m.id, fecha: new Date(m.fecha).toLocaleString('es-PE'), tipo: m.tipo === 'entrada' ? 'Ingreso' : (m.motivo === 'ajuste' || m.motivo === 'merma' ? 'Ajuste' : 'Salida'),
          clase: m.tipo === 'entrada' ? 'verde' : (m.motivo === 'ajuste' || m.motivo === 'merma' ? 'naranja' : 'rojo'), sku: m.sku,
          producto: m.producto, cantidad: `${m.tipo === 'entrada' ? '+' : '-'}${m.cantidad} ${m.cantidad === 1 ? 'ud' : 'uds'}`,
          responsable: m.responsable, origen: m.documento || m.destinoOrigen || m.motivo
        }));
        const entradas = rows.filter(m => m.tipo === 'entrada').reduce((sum, m) => sum + m.cantidad, 0);
        const salidas = rows.filter(m => m.tipo === 'salida').reduce((sum, m) => sum + m.cantidad, 0);
        const ajustes = rows.filter(m => ['ajuste', 'merma'].includes(m.motivo)).reduce((sum, m) => sum + m.cantidad, 0);
        this.resumen = [
          { titulo: 'Total entradas', subtitulo: 'Ingresos registrados', insignia: `+${entradas} uds`, valor: `+${entradas} uds`, detalle: `${rows.filter(m => m.tipo === 'entrada').length} movimientos de ingreso registrados.`, icono: 'bi-plus-circle', tono: 'verde' },
          { titulo: 'Total salidas', subtitulo: 'Salidas registradas', insignia: `-${salidas} uds`, valor: `-${salidas} uds`, detalle: `${rows.filter(m => m.tipo === 'salida').length} movimientos de salida registrados.`, icono: 'bi-dash-circle', tono: 'rojo' },
          { titulo: 'Ajustes y pérdidas', subtitulo: 'Movimientos no comerciales', insignia: `${ajustes} uds`, valor: `${ajustes} uds`, detalle: `${rows.filter(m => ['ajuste', 'merma'].includes(m.motivo)).length} ajustes o pérdidas registradas.`, icono: 'bi-search', tono: 'naranja' },
        ];
        this.cargando = false;
        this.cdr.markForCheck();
      }, error: e => { this.error = e?.error?.detail || 'No se pudo cargar el historial de movimientos.'; this.cargando = false; this.cdr.markForCheck(); }
    });
  }
}
