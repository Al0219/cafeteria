import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { UserListComponent } from './users/user-list.component';
import { UserEditComponent } from './users/user-edit.component';
import { MenuListComponent } from './menu/menu-list.component';
import { KitchenListComponent } from './kitchen/kitchen-list.component';

export const routes: Routes = [
  { path: '', component: LoginComponent, title: 'Iniciar sesión | Yumil Kool' },
  { path: 'usuarios', component: UserListComponent, title: 'Control de usuarios' },
  { path: 'menu', component: MenuListComponent, title: 'Control del menú' },
  { path: 'cocina', component: KitchenListComponent, title: 'Control de pedidos en cocina' },
  { path: 'usuarios/editar/:id', component: UserEditComponent, title: 'Editar usuario' },
  { path: '**', redirectTo: '' }
];
