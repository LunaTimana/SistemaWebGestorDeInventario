import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProductoService, UsuarioSistema } from '../services/producto';

@Component({ selector: 'app-usuarios', imports: [FormsModule], templateUrl: './usuarios.html', styleUrl: './usuarios.scss' })
export class Usuarios implements OnInit {
  private readonly service = inject(ProductoService);
  private readonly cdr = inject(ChangeDetectorRef);
  usuarios: UsuarioSistema[] = [];
  seleccionado: UsuarioSistema | null = null;
  rolesDisponibles = ['Administrador','Almacén','Ventas'];
  nuevoRol = 'Almacén';
  nombreCompleto = '';
  correo = '';
  rolNuevo = 'Almacén';
  cargando = true;
  guardando = false;
  error = '';
  mensaje = '';
  contrasenaTemporal = '';
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando = true; this.error = '';
    this.service.obtenerUsuarios().subscribe({ next: rows => { this.usuarios=rows; if (this.seleccionado) this.seleccionado=rows.find(u=>u.id===this.seleccionado?.id) ?? null; if (!this.seleccionado) this.seleccionado=rows[0] ?? null; this.nuevoRol=this.seleccionado?.rol ?? 'Almacén'; this.cargando=false; this.cdr.markForCheck(); }, error:e=>{this.error=e?.error?.detail || 'No se pudieron cargar los usuarios de MySQL.';this.cargando=false;this.cdr.markForCheck();} });
  }
  seleccionar(u:UsuarioSistema):void { this.seleccionado=u; this.nuevoRol=u.rol; this.mensaje=''; }
  claseRol(rol:string):string { return rol==='Administrador'?'admin':rol==='Almacén'?'almacen':'ventas'; }
  claseEstado(estado:string):string { return estado==='activo'?'activo':'inactivo'; }
  fecha(valor:string|null):string { return valor ? new Date(valor).toLocaleString('es-PE') : 'Sin conexión registrada'; }
  guardarRol():void {
    if(!this.seleccionado || this.guardando) return;
    this.guardando=true;this.error='';this.mensaje='';
    this.service.cambiarRolUsuario(this.seleccionado.id,this.nuevoRol).subscribe({next:r=>{this.mensaje=r.mensaje;this.guardando=false;this.cargar();},error:e=>{this.error=e?.error?.detail || 'No se pudo actualizar el rol.';this.guardando=false;}});
  }
  alternarEstado(u:UsuarioSistema):void {
    const nuevo = u.estado==='activo'?'inactivo':'activo';
    if(!confirm(`¿${nuevo==='activo'?'Activar':'Desactivar'} a ${u.nombre} ${u.apellido}?`))return;
    this.error='';this.mensaje='';
    this.service.cambiarEstadoUsuario(u.id,nuevo).subscribe({next:r=>{this.mensaje=r.mensaje;this.cargar();},error:e=>this.error=e?.error?.detail || 'No se pudo cambiar el estado del usuario.'});
  }
  crearUsuario():void {
    this.error='';this.mensaje='';this.contrasenaTemporal='';
    const partes=this.nombreCompleto.trim().split(/\s+/);
    if(partes.length<2 || !this.correo.trim() || !this.correo.includes('@')){this.error='Ingresa nombre y apellido, además de un correo válido.';return;}
    this.guardando=true;
    this.service.crearUsuario({nombre:partes[0],apellido:partes.slice(1).join(' '),correo:this.correo.trim(),rol:this.rolNuevo}).subscribe({next:r=>{this.mensaje=r.mensaje;this.contrasenaTemporal=r.contrasenaTemporal;this.nombreCompleto='';this.correo='';this.guardando=false;this.cargar();},error:e=>{this.error=e?.error?.detail || 'No se pudo crear el usuario.';this.guardando=false;}});
  }
}
