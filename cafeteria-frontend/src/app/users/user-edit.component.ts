import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { UserService } from './user.service';
import { RoleService } from './role.service';
import { Role } from './role.model';
import { User } from './user.model';

@Component({
  selector: 'app-user-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent implements OnInit {
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMsg = signal<string | null>(null);
  readonly roles = signal<Role[]>([]);
  readonly user = signal<User | null>(null);

  readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    usuario: ['', Validators.required],
    dpi: ['', Validators.required],
    email: ['', Validators.email],
    telefono: [''],
    direccion: [''],
    rolCodigo: ['', Validators.required],
    contrasenia: [''] // opcional al editar
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly userService: UserService,
    private readonly roleService: RoleService,
    public readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.loading.set(true);
    this.errorMsg.set(null);

    this.roleService.getRoles().subscribe({
      next: rs => this.roles.set(rs),
      error: err => console.error('Error roles', err)
    });

    this.userService.getUser(id).subscribe({
      next: u => {
        this.user.set(u);
        this.form.patchValue({
          nombre: u.nombre,
          usuario: u.usuario,
          dpi: u.dpi,
          email: u.email ?? '',
          telefono: u.telefono ?? '',
          direccion: u.direccion ?? '',
          rolCodigo: u.rol || ''
        });
        this.loading.set(false);
      },
      error: err => {
        console.error('Error cargando usuario', err);
        this.errorMsg.set('No se pudo cargar el usuario.');
        this.loading.set(false);
      }
    });
  }

  back(): void { this.router.navigate(['/usuarios']); }

  onLogout(ev: Event): void {
    ev.preventDefault();
    this.auth.logout();
    this.router.navigate(['/']);
  }

  save(): void {
    if (!this.user() || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const id = this.user()!.id!;
    const v = this.form.getRawValue();
    this.saving.set(true);
    this.userService.updateUser(id, {
      nombre: v.nombre,
      usuario: v.usuario,
      dpi: v.dpi,
      email: v.email || undefined,
      telefono: v.telefono || undefined,
      direccion: v.direccion || undefined,
      rolCodigo: v.rolCodigo,
      contrasenia: v.contrasenia || ''
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.router.navigate(['/usuarios']);
      },
      error: err => {
        console.error('Error actualizando usuario', err);
        this.saving.set(false);
        this.errorMsg.set('No se pudo actualizar el usuario');
      }
    });
  }
}
