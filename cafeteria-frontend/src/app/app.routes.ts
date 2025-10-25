import { Routes } from '@angular/router';
import { roleGuard } from './auth/role.guard';
import { LoginComponent } from './login/login.component';
import { UserListComponent } from './users/user-list.component';
import { UserEditComponent } from './users/user-edit.component';
import { MenuListComponent } from './menu/menu-list.component';
import { OrdersListComponent } from './orders/orders-list.component';
import { OrdersCashierListComponent } from './orders2/orders-cashier-list.component';
import { SaleFormComponent } from './sales/sale-form.component';
import { OrderTakeComponent } from './order-take/order-take.component';
import { MesasComponent } from './mesas/mesas.component';
import { KitchenListComponent } from './kitchen/kitchen-list.component';

export const routes: Routes = [
  { path: '', component: LoginComponent, title: 'Iniciar sesion | Yumil Kool' },
  { path: 'usuarios', component: UserListComponent, title: 'Control de usuarios', canMatch: [roleGuard(['ADMIN','GERENTE'])] },
  { path: 'menu', component: MenuListComponent, title: 'Control del menu', canMatch: [roleGuard(['ADMIN','GERENTE'])] },
  { path: 'cocina', component: KitchenListComponent, title: 'Control de pedidos en cocina', canMatch: [roleGuard(['COCINA','ADMIN','GERENTE'])] },
  { path: 'usuarios/editar/:id', component: UserEditComponent, title: 'Editar usuario', canMatch: [roleGuard(['ADMIN','GERENTE'])] },
  { path: 'pedidos', component: OrdersListComponent, title: 'Control de pedidos', canMatch: [roleGuard(['MESERO','ADMIN','GERENTE'])] },
  { path: 'pedidos2', component: OrdersCashierListComponent, title: 'Control de pedidos (Caja)', canMatch: [roleGuard(['CAJERO','ADMIN','GERENTE'])] },
  { path: 'ventas/nueva/:pedidoId', component: SaleFormComponent, title: 'Registrar venta', canMatch: [roleGuard(['CAJERO','ADMIN','GERENTE'])] },
  { path: 'pedir', component: OrderTakeComponent, title: 'Tomar pedido', canMatch: [roleGuard(['MESERO','ADMIN','GERENTE'])] },
  { path: 'mesas', component: MesasComponent, title: 'Mesas', canMatch: [roleGuard(['MESERO','ADMIN','GERENTE'])] },
  { path: '**', redirectTo: '' }
];


