import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProductoService } from '../../services/producto';
import { AuthService } from '../../auth/auth.service';

@Component({ selector: 'app-configuracion-cuenta', imports: [RouterLink, FormsModule], templateUrl: './configuracion-cuenta.html', styleUrl: './configuracion-cuenta.scss' })
export class ConfiguracionCuenta implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly service = inject(ProductoService);
  private readonly router = inject(Router);
  usuario = { id:0,nombre:'',apellido:'',correo:'',rol:'',debe_cambiar_contrasena:false,iniciales:'--',sucursal:'Sucursal Principal' };
  formulario = { nombre:'',apellido:'',correo:'' };
  claves = { actual:'',nueva:'',confirmar:'' };
  mostrar = { actual:false,nueva:false,confirmar:false };
  cargando = true; guardando = false; error = ''; mensaje = '';
  ngOnInit():void { this.cargar(); }
  cargar():void {
    this.cargando=true;this.error='';
    this.service.obtenerMiCuenta().subscribe({next:u=>{
      this.usuario={...u,iniciales:`${u.nombre?.[0]??''}${u.apellido?.[0]??''}`.toUpperCase(),sucursal:'Sucursal Principal'};
      this.formulario={nombre:u.nombre,apellido:u.apellido,correo:u.correo};
      const guardado=sessionStorage.getItem('emsStoreUser');
      if(guardado){try{sessionStorage.setItem('emsStoreUser',JSON.stringify({...JSON.parse(guardado),nombre:u.nombre,apellido:u.apellido,correo:u.correo,rol:u.rol,debeCambiarContrasena:u.debe_cambiar_contrasena}));}catch{}}
      this.cargando=false;
      this.cdr.markForCheck();
    },error:e=>{this.error=e?.error?.detail||'No se pudo cargar la cuenta desde el backend.';this.cargando=false;this.cdr.markForCheck();}});
  }
  guardarDatos():void {
    this.error='';this.mensaje='';
    if(!this.formulario.nombre.trim()||!this.formulario.apellido.trim()||!this.formulario.correo.includes('@')){this.error='Completa nombre, apellido y un correo válido.';return;}
    this.guardando=true;
    this.service.actualizarMiCuenta({nombre:this.formulario.nombre.trim(),apellido:this.formulario.apellido.trim(),correo:this.formulario.correo.trim()}).subscribe({next:()=>{this.mensaje='Datos personales actualizados.';this.guardando=false;this.cargar();},error:e=>{this.error=e?.error?.detail||'No se pudieron guardar los datos.';this.guardando=false;}});
  }
  cambiarContrasena():void {
    this.error='';this.mensaje='';
    if(!this.claves.actual||!this.claves.nueva||this.claves.nueva!==this.claves.confirmar){this.error='Completa las tres contraseñas y confirma que coincidan.';return;}
    if(!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/.test(this.claves.nueva)){this.error='Usa al menos 8 caracteres, una mayúscula, una minúscula y un número.';return;}
    this.guardando=true;
    this.service.cambiarMiContrasena(this.claves.actual,this.claves.nueva).subscribe({next:r=>{this.mensaje=r.mensaje;this.claves={actual:'',nueva:'',confirmar:''};this.guardando=false;this.cargar();},error:e=>{this.error=e?.error?.detail||'No se pudo cambiar la contraseña.';this.guardando=false;}});
  }
  cerrarSesion():void { this.authService.cerrarSesion();void this.router.navigateByUrl('/login'); }
}
