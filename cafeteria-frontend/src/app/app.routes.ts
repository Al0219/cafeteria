import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { UserListComponent } from './users/user-list.component';
import { UserEditComponent } from './users/user-edit.component';
import { MenuListComponent } from './menu/menu-list.component';
import { OrdersListComponent } from './orders/orders-list.component';
import { OrdersCashierListComponent } from './orders2/orders-cashier-list.component';
import { SaleFormComponent } from './sales/sale-form.component';
import { KitchenListComponent } from './kitchen/kitchen-list.component';

export const routes: Routes = [
  { path: '', component: LoginComponent, title: 'Iniciar sesión | Yumil Kool' },
  { path: 'usuarios', component: UserListComponent, title: 'Control de usuarios' },
  { path: 'menu', component: MenuListComponent, title: 'Control del menú' },
  { path: 'cocina', component: KitchenListComponent, title: 'Control de pedidos en cocina' },
  { path: 'usuarios/editar/:id', component: UserEditComponent, title: 'Editar usuario' },
  { path: 'pedidos', component: OrdersListComponent, title: 'Control de pedidos' },
  { path: 'pedidos2', component: OrdersCashierListComponent, title: 'Control de pedidos (Caja)' },
  { path: 'ventas/nueva/:pedidoId', component: SaleFormComponent, title: 'Registrar venta' },
  { path: '**', redirectTo: '' }
];
