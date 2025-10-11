import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { OrderService } from '../kitchen/order.service';
import { Pedido, PedidoItem } from '../kitchen/order.model';

@Component({
  selector: 'app-orders-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './orders-list.component.html',
  styleUrls: ['./orders-list.component.scss']
})
export class OrdersListComponent implements OnInit {
  orders: Pedido[] = [];
  filteredOrders: Pedido[] = [];
  estadoFilter: string = 'todos';
  searchQuery: string = '';

  constructor(
    private readonly orderService: OrderService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.orderService.getOrders().subscribe({
      next: (data) => {
        this.orders = data;
        this.applyFilter();
      },
      error: (err) => {
        console.error('Error al cargar los pedidos', err);
      }
    });
  }

  applyFilter(): void {
    let tempOrders = this.orders;

    // Filter by state
    if (this.estadoFilter !== 'todos') {
      tempOrders = tempOrders.filter(order => order.estado.toLowerCase() === this.estadoFilter.toLowerCase());
    }

    // Filter by search query (product name)
    if (this.searchQuery) {
      tempOrders = tempOrders.filter(order =>
        order.items.some(item =>
          item.nombre.toLowerCase().includes(this.searchQuery.toLowerCase())
        )
      );
    }

    this.filteredOrders = tempOrders;
  }

  getProductInfo(items: PedidoItem[]): string {
    if (!items || items.length === 0) {
      return '';
    }
    return items.map(item => `${item.cantidad}x ${item.nombre}`).join(', ');
  }

  goBack(): void {
    this.router.navigate(['/home']); // Or a more appropriate previous page
  }

  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/']); // Navigate to login page
  }

  goToPedidos(): void {
    this.router.navigate(['/pedidos']);
  }

  goToMesas(): void {
    this.router.navigate(['/mesas']);
  }

  onEdit(order: Pedido): void {
    alert(`Editar pedido ${order.id} (funcionalidad no implementada)`);
    // Implement actual edit logic, e.g., navigate to an edit form
  }

  onDelete(order: Pedido): void {
    if (confirm(`¿Estás seguro de que quieres eliminar el pedido ${order.id}?`)) {
      alert(`Eliminar pedido ${order.id} (funcionalidad no implementada)`);
      // Implement actual delete logic via OrderService
    }
  }
}

