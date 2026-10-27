import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductoService, ResumenAlertas } from '../services/producto';

@Component({ selector: 'app-alertas', imports: [RouterLink], templateUrl: './alertas.html', styleUrl: './alertas.scss' })
export class Alertas implements OnInit {
  private readonly service = inject(ProductoService);
  private readonly cdr = inject(ChangeDetectorRef);
  cargando = true;
  error = '';
  filtro: 'todos'|'criticos'|'riesgo' = 'todos';
  resumen = [
    { titulo:'Productos críticos', valor:'0', tono:'rojo' },
    { titulo:'Productos en riesgo', valor:'0', tono:'naranja' },
    { titulo:'Variantes monitoreadas', valor:'0', tono:'neutro' },
  ];
  alertas: { varianteId:number; sku:string; producto:string; talla:string; stock:number; minimo:number; deficit:number; estado:string; ultimoIngreso:string|null; clase:string }[] = [];
  get alertasFiltradas() { return this.alertas.filter(a => this.filtro === 'todos' || (this.filtro === 'criticos' ? a.estado === 'CRÍTICO' : a.estado !== 'CRÍTICO')); }
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando = true; this.error = '';
    this.service.obtenerAlertas().subscribe({
      next: (d: ResumenAlertas) => {
        this.resumen = [
          { titulo:'Productos críticos', valor:String(d.criticos), tono:'rojo' },
          { titulo:'Productos en riesgo', valor:String(d.riesgo), tono:'naranja' },
          { titulo:'Variantes monitoreadas', valor:String(d.total), tono:'neutro' },
        ];
        this.alertas = d.alertas.map(a => ({ ...a, clase:a.estado === 'CRÍTICO' ? 'rojo' : 'naranja' }));
        this.cargando = false;
        this.cdr.markForCheck();
      }, error: e => { this.error = e?.error?.detail || 'No se pudieron cargar las alertas desde la base de datos.'; this.cargando = false; this.cdr.markForCheck(); }
    });
  }
  fecha(fecha:string|null): string { return fecha ? new Date(fecha).toLocaleDateString('es-PE') : 'Sin ingresos registrados'; }
}
