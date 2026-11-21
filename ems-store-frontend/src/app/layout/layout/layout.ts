import { Component, computed, HostListener, inject, OnDestroy, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { MENU, SUBPANTALLAS } from '../../core/menu';
import { Rol } from '../../core/rol';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class Layout implements OnDestroy {
  private router = inject(Router);
  private authService = inject(AuthService);
  private readonly tiempoInactividadMs = 4 * 60 * 1000;
  private ultimaActividad = Date.now();
  private temporizadorInactividad?: ReturnType<typeof setTimeout>;

  constructor() {
    this.programarCierreSesion();
  }

  @HostListener('document:pointerdown')
  @HostListener('document:keydown')
  @HostListener('document:mousemove')
  @HostListener('document:scroll')
  @HostListener('document:touchstart')
  registrarActividad(): void {
    if (!sessionStorage.getItem('emsStoreToken')) return;
    this.ultimaActividad = Date.now();
    this.programarCierreSesion();
  }

  ngOnDestroy(): void {
    if (this.temporizadorInactividad) clearTimeout(this.temporizadorInactividad);
  }

  private programarCierreSesion(): void {
    if (this.temporizadorInactividad) clearTimeout(this.temporizadorInactividad);
    this.temporizadorInactividad = setTimeout(() => {
      const inactivoMs = Date.now() - this.ultimaActividad;
      if (inactivoMs < this.tiempoInactividadMs) {
        this.temporizadorInactividad = setTimeout(() => this.cerrarPorInactividad(), this.tiempoInactividadMs - inactivoMs);
        return;
      }
      this.cerrarPorInactividad();
    }, this.tiempoInactividadMs);
  }

  private cerrarPorInactividad(): void {
    if (!sessionStorage.getItem('emsStoreToken')) return;
    this.authService.cerrarSesion();
    void this.router.navigateByUrl('/login', { replaceUrl: true });
  }

  usuario = (() => {
    try {
      const user = JSON.parse(typeof sessionStorage === 'undefined' ? '{}' : sessionStorage.getItem('emsStoreUser') ?? '{}') as {
        nombre?: string;
        apellido?: string;
        rol?: string;
      };
      const nombre = [user.nombre, user.apellido].filter(Boolean).join(' ').trim() || 'Usuario';
      const iniciales = nombre.split(/\s+/).slice(0, 2).map(parte => parte[0]).join('').toUpperCase();
      return { nombre, rol: user.rol || 'Sin rol', iniciales: iniciales || 'U' };
    } catch {
      return { nombre: 'Usuario', rol: 'Sin rol', iniciales: 'U' };
    }
  })();

  menu = computed(() => {
    let role = 'VENTAS';
    try {
      const user = JSON.parse(sessionStorage.getItem('emsStoreUser') ?? '{}') as { rol?: string };
      role = user.rol === 'Administrador' ? 'ADMIN' : user.rol === 'Almacén' ? 'ALMACEN' : 'VENTAS';
    } catch { role = 'VENTAS'; }
    return MENU.filter(item => item.roles.includes(role as Rol));
  });
  sucursal = 'Sucursal Principal';
  menuAbierto = signal(false);

  private url = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  // Opción del menú que corresponde a la pantalla actual
  actual = computed(() => MENU.find((m) => this.url().startsWith('/' + m.ruta)));
  
  // Si la pantalla es una subpantalla (por ejemplo, el detalle de un producto)
  subpantalla = computed(() => SUBPANTALLAS.find((s) => this.url().startsWith(s.prefijo)));
  encabezado = computed(() => this.subpantalla()?.encabezado ?? this.actual()?.encabezado);
}
