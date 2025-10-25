import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';

import { OrderService } from '../kitchen/order.service';
import { Pedido } from '../kitchen/order.model';
import { MesaService } from '../kitchen/mesa.service';
import { Mesa } from '../kitchen/mesa.model';

@Component({
  selector: 'app-orders-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe],
  templateUrl: './orders-list.component.html',
  styleUrls: ['./orders-list.component.scss']
})
export class OrdersListComponent implements OnInit {
  readonly loading = signal(true);
  readonly search = signal('');
  readonly estadoFilter = signal<string>('');
  readonly mesaFilter = signal<string>('');
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly orders = signal<Pedido[]>([]);
  readonly expanded = signal<Set<number>>(new Set());
  readonly showDelete = signal(false);
  private pendingToDelete: Pedido | null = null;

  readonly estados = computed(() => Array.from(new Set(this.orders().map(o => o.estado))));
  readonly mesasApi = signal<Mesa[]>([]);
  readonly mesas = computed(() => {
    const api = this.mesasApi();
    if (api.length) {
      const nombres = api.filter(m => m.activo !== false).map(m => m.nombre);
      return ['Para llevar', ...nombres];
    }
    return Array.from(new Set(
      this.orders().map(o => (o.mesaNumero == null ? 'Para llevar' : `Mesa ${o.mesaNumero}`))
    ));
  });

  readonly filtered = computed(() => {
    const termRaw = this.search().trim().toLowerCase();
    const term = termRaw.replace(/\s|-/g, '');
    const est = this.estadoFilter();
    const mesaSel = this.mesaFilter();
    const list = this.orders().filter(o => {
      const noPedido = String(o.id).includes(term);
      const mesaTxt = (o.mesaNumero == null) ? 'para llevar' : `mesa ${o.mesaNumero}`;
      const labelMesa = (o.mesaNumero == null) ? 'Para llevar' : `Mesa ${o.mesaNumero}`;
      const byMesa = !mesaSel || labelMesa === mesaSel;
      const byCli = (o.cliente || '').toLowerCase().includes(termRaw);
      const byQuery = !term || noPedido || mesaTxt.includes(term) || byCli;
      const byEstado = est ? (o.estado === est) : (o.estado !== 'ENTREGADO');
      return byQuery && byEstado && byMesa;
    });
    const dir = this.sortDir();
    return list.slice().sort((a, b) => {
      const ta = new Date(a.creado_en).getTime();
      const tb = new Date(b.creado_en).getTime();
      return dir === 'asc' ? ta - tb : tb - ta;
    });
  });

  constructor(
    private readonly orderService: OrderService,
    private readonly mesaService: MesaService,
    private readonly router: Router,
    public readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    this.orderService.getOrders().subscribe(list => {
      this.orders.set(list);
      this.loading.set(false);
    });
    this.mesaService.getMesas().subscribe({
      next: ms => this.mesasApi.set(ms),
      error: err => console.error('Error cargando mesas', err)
    });
  }

  toggleRow(id: number): void {
    const open = this.expanded();
    if (open.has(id)) {
      this.expanded.set(new Set());
      return;
    }
    this.expanded.set(new Set([id]));
    const target = this.orders().find(o => o.id === id);
    if (target && (!target.items || target.items.length === 0)) {
      this.orderService.getOrder(id).subscribe({
        next: det => {
          this.orders.update(list => list.map(x => x.id === id ? { ...x, items: det.items, notas: det.notas } : x));
        },
        error: err => console.error('Error cargando detalles del pedido', err)
      });
    }
  }

  toggleSort(): void {
    this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
  }

  onEstadoChange(valor: string): void { this.estadoFilter.set(valor); }

  onEdit(o: Pedido): void {
    if (o.estado !== 'RECIBIDO') return;
    const params: any = { pedidoId: o.id };
    if (o.mesaNumero != null) params.mesaId = o.mesaNumero;
    this.router.navigate(['/pedir'], { queryParams: params });
  }

  onDelete(o: Pedido): void {
    if (o.estado !== 'RECIBIDO') return;
    this.pendingToDelete = o;
    this.showDelete.set(true);
  }

  confirmDelete(): void {
    const o = this.pendingToDelete;
    if (!o) { this.showDelete.set(false); return; }
    this.orderService.deletePedido(o.id).subscribe({
      next: () => {
        this.orders.update(list => list.filter(p => p.id !== o.id));
        this.expanded.set(new Set());
        this.pendingToDelete = null;
        this.showDelete.set(false);
      },
      error: err => {
        console.error('Error eliminando pedido', err);
        this.showDelete.set(false);
        alert(err?.error?.message || err?.message || 'No se pudo eliminar el pedido');
      }
    });
  }

  cancelDelete(): void { this.pendingToDelete = null; this.showDelete.set(false); }

  onLogout(ev: Event): void {
    ev.preventDefault();
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
