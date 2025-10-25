import { Component, OnInit, signal, computed, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { UserService } from './user.service';
import { RoleService } from './role.service';
import { Role } from './role.model';
import { User } from './user.model';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit, AfterViewInit {
  @ViewChild('logoutChip') logoutChip?: ElementRef<HTMLAnchorElement>;
  @ViewChild('hotbar') hotbar?: ElementRef<HTMLElement>;
  readonly loading = signal(true);
  readonly search = signal('');
  readonly roleFilter = signal<string>(''); // vacío = todos
  readonly estadoFilter = signal<string>(''); // vacío = todos
  readonly users = signal<User[]>([]);
  readonly errorMsg = signal<string | null>(null);
  readonly creating = signal(false);
  readonly isSaving = signal(false);
  readonly editing = signal<User | null>(null);
  readonly isUpdating = signal(false);
  readonly deletingId = signal<number | null>(null);
  readonly activatingId = signal<number | null>(null);

  // Formulario de creación
  readonly createForm = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    usuario: ['', Validators.required],
    dpi: ['', Validators.required],
    email: ['', Validators.email],
    telefono: [''],
    direccion: [''],
    rolCodigo: ['', Validators.required],
    contrasenia: ['', [Validators.required, Validators.minLength(6)]]
  });

  // Formulario de edición (contraseña opcional)
  readonly editForm = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    usuario: ['', Validators.required],
    dpi: ['', Validators.required],
    email: ['', Validators.email],
    telefono: [''],
    direccion: [''],
    rolCodigo: ['', Validators.required],
    contrasenia: ['']
  });

  readonly roles = signal<Role[]>([]);

  readonly estados = computed(() => {
    const set = new Set(this.users().map(u => u.estado));
    return Array.from(set.values());
  });

  readonly filtered = computed(() => {
    const raw = this.search().trim().toLowerCase();
    const term = raw.replace(/\s|-/g, '');
    const role = this.roleFilter();
    const estado = this.estadoFilter();
    return this.users().filter(u => {
      const nombreOk = !term || (u.nombre || '').toLowerCase().includes(raw);
      const dpiOk = !term || (u.dpi || '').toLowerCase().replace(/\s|-/g, '').includes(term);
      const byQuery = nombreOk || dpiOk;
      const byRole = !role || u.rol === role;
      const byEstado = !estado || u.estado === estado;
      return byQuery && byRole && byEstado;
    });
  });

  constructor(
    private readonly userService: UserService,
    private readonly roleService: RoleService,
    private readonly fb: FormBuilder,
    private readonly router: Router,
    public readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    this.refresh();
    this.roleService.getRoles().subscribe({
      next: rs => this.roles.set(rs),
      error: err => console.error('Error cargando roles', err)
    });
  }

  refresh(): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.userService.getUsers().subscribe({
      next: users => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set('No se pudieron cargar los usuarios.');
        // opcional: console error para depuración
        console.error('Error cargando usuarios', err);
      }
    });
  }

  ngAfterViewInit(): void {
    // Ajustar el ancho uniforme de los chips al ancho real del botón "Cerrar sesión"
    queueMicrotask(() => {
      const logoutEl = this.logoutChip?.nativeElement;
      const hotbarEl = this.hotbar?.nativeElement;
      if (logoutEl && hotbarEl) {
        const width = Math.ceil(logoutEl.getBoundingClientRect().width);
        hotbarEl.style.setProperty('--chip-w', `${width}px`);
      }
    });
  }

  onCreate(): void {
    this.creating.set(true);
  }

  onEdit(user: User): void {
    this.editing.set(user);
    this.editForm.reset();
    this.editForm.patchValue({
      nombre: user.nombre,
      usuario: user.usuario,
      dpi: user.dpi,
      email: user.email ?? '',
      telefono: user.telefono ?? '',
      direccion: user.direccion ?? '',
      rolCodigo: user.rol || ''
    });
  }

  onDelete(user: User): void {
    if (!user.id) return;
    const ok = confirm(`¿Desactivar a ${user.nombre}?`);
    if (!ok) return;
    this.deletingId.set(user.id);
    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        // Reflejar de inmediato en la tabla sin recargar toda la lista
        this.users.update(list => list.map(u => u.id === user.id ? { ...u, activo: false, estado: 'Inactivo' } : u));
        this.deletingId.set(null);
        // Forzar sincronización con backend para evitar estados inconsistentes
        this.refresh();
      },
      error: (err) => {
        console.error('Error desactivando usuario', err);
        this.deletingId.set(null);
        alert('No se pudo desactivar el usuario');
      }
    });
  }

  onLogout(ev: Event): void {
    ev.preventDefault();
    this.auth.logout();
    this.router.navigate(['/']);
  }

  onActivate(user: User): void {
    if (!user.id) return;
    const ok = confirm(`¿Activar a ${user.nombre}?`);
    if (!ok) return;
    this.activatingId.set(user.id);
    // Enviar PUT con los datos actuales y activo=true (sin cambiar contraseña)
    this.userService.updateUser(user.id, {
      nombre: user.nombre,
      usuario: user.usuario,
      dpi: user.dpi,
      email: user.email || undefined,
      telefono: user.telefono || undefined,
      direccion: user.direccion || undefined,
      rolCodigo: user.rol,
      // contrasenia omitida para no modificar
      activo: true
    }).subscribe({
      next: () => {
        this.users.update(list => list.map(u => u.id === user.id ? { ...u, activo: true, estado: 'Activo' } : u));
        this.activatingId.set(null);
        this.refresh();
      },
      error: (err) => {
        console.error('Error activando usuario', err);
        this.activatingId.set(null);
        alert('No se pudo activar el usuario');
      }
    });
  }

  cancelCreate(): void {
    this.creating.set(false);
    this.createForm.reset();
  }

  cancelEdit(): void {
    this.editing.set(null);
    this.editForm.reset();
  }

  saveCreate(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    this.isSaving.set(true);
    const v = this.createForm.getRawValue();
    this.userService.createUser({
      nombre: v.nombre,
      usuario: v.usuario,
      dpi: v.dpi,
      email: v.email || undefined,
      telefono: v.telefono || undefined,
      direccion: v.direccion || undefined,
      rolCodigo: v.rolCodigo,
      contrasenia: v.contrasenia
    }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.creating.set(false);
        this.createForm.reset();
        this.refresh();
      },
      error: (err) => {
        console.error('Error creando usuario', err);
        this.isSaving.set(false);
        alert('No se pudo crear el usuario');
      }
    });
  }

  saveEdit(): void {
    const current = this.editing();
    if (!current) return;
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }
    this.isUpdating.set(true);
    const v = this.editForm.getRawValue();
    const payload: any = {
      nombre: v.nombre,
      usuario: v.usuario,
      dpi: v.dpi,
      email: v.email || undefined,
      telefono: v.telefono || undefined,
      direccion: v.direccion || undefined,
      rolCodigo: v.rolCodigo
    };
    if (v.contrasenia && v.contrasenia.trim().length > 0) {
      payload.contrasenia = v.contrasenia;
    }
    this.userService.updateUser(current.id!, payload).subscribe({
      next: () => {
        this.isUpdating.set(false);
        this.editing.set(null);
        this.refresh();
      },
      error: (err) => {
        console.error('Error actualizando usuario', err);
        this.isUpdating.set(false);
        alert('No se pudo actualizar el usuario');
      }
    });
  }
}
