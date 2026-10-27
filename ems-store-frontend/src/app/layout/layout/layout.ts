import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { MENU, SUBPANTALLAS } from '../../core/menu';
import { Rol } from '../../core/rol';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class Layout {
  private router = inject(Router);

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