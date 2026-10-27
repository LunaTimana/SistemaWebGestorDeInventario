import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ProductoService, ReporteCategoria } from '../services/producto';

@Component({ selector: 'app-reportes', imports: [DecimalPipe], templateUrl: './reportes.html', styleUrl: './reportes.scss' })
export class Reportes implements OnInit {
  private readonly service = inject(ProductoService);
  private readonly cdr = inject(ChangeDetectorRef);
  cargando = true;
  error = '';
  exportando = '';
  reportes = [
    { id:'inventario', titulo:'Reporte de Inventario General', descripcion:'Existencias y valorización del catálogo en tiempo real.', icono:'bi-file-earmark-excel', boton:'Descargar Excel', principal:true },
    { id:'movimientos', titulo:'Reporte de Movimientos', descripcion:'Registro de entradas y salidas con responsable, motivo y documento.', icono:'bi-arrow-left-right', boton:'Descargar Excel', principal:false },
  ];
  categorias: (ReporteCategoria & { porcentaje:number; valorTexto:string; promedioTexto:string })[] = [];
  totalUnidades = 0;
  valorTotal = 0;
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando = true; this.error = '';
    this.service.obtenerReporteCategorias().subscribe({
      next: rows => {
        this.totalUnidades = rows.reduce((s,r)=>s+Number(r.unidades||0),0);
        this.valorTotal = rows.reduce((s,r)=>s+Number(r.valorVenta||0),0);
        this.categorias = rows.map(r=>({...r, porcentaje:this.totalUnidades ? Number(r.unidades)*100/this.totalUnidades : 0, valorTexto:this.moneda(Number(r.valorVenta)), promedioTexto:this.moneda(Number(r.precioPromedio))}));
        this.cargando = false;
        this.cdr.markForCheck();
      }, error: e => { this.error = e?.error?.detail || 'No se pudo consultar el reporte en el backend.'; this.cargando = false; this.cdr.markForCheck(); }
    });
  }
  generar(id:string): void {
    if (this.exportando) return;
    this.exportando = id; this.error = '';
    const solicitud = id === 'inventario' ? this.service.exportarInventarioExcel() : this.service.exportarMovimientosExcel();
    solicitud.subscribe({ next: blob => {
      const url = URL.createObjectURL(blob); const a = document.createElement('a');
      a.href=url; a.download=id === 'inventario' ? 'EMS-Store-Inventario.xlsx' : 'EMS-Store-Movimientos.xlsx';
      a.click(); setTimeout(() => URL.revokeObjectURL(url), 1000); this.exportando='';
    }, error: e => { this.error = e?.error?.detail || 'No se pudo generar el reporte.'; this.exportando=''; } });
  }
  colorPorcentaje(porcentaje:number): string {
    const acotado = Math.max(0, Math.min(100, porcentaje));
    return `hsl(${Math.round(acotado * 1.2)} 75% 45%)`;
  }
  private moneda(v:number):string { return new Intl.NumberFormat('es-PE',{style:'currency',currency:'PEN'}).format(v); }
}
