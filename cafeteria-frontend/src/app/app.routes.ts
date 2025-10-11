import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { UserListComponent } from './users/user-list.component';
import { UserEditComponent } from './users/user-edit.component';

export const routes: Routes = [
  { path: '', component: LoginComponent, title: 'Iniciar sesión | Yumil Kool' },
  { path: 'usuarios', component: UserListComponent, title: 'Control de usuarios' },
  { path: 'usuarios/editar/:id', component: UserEditComponent, title: 'Editar usuario' },
  { path: '**', redirectTo: '' }
];
