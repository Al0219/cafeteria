import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';

import { OrderService } from '../kitchen/order.service';
import { Pedido } from '../kitchen/order.model';
import { MesaService } from '../kitchen/mesa.service';
import { Mesa } from '../kitchen/mesa.model';
import { SaleService } from '../sales/sale.service';

@Component({
  selector: 'app-orders-cashier-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe],
  templateUrl: './orders-cashier-list.component.html',
  styleUrls: ['./orders-cashier-list.component.scss']
})
export class OrdersCashierListComponent implements OnInit {
  readonly loading = signal(true);
  readonly search = signal('');
  readonly estadoFilter = signal<string>('ENTREGADO');
  readonly mesaFilter = signal<string>('');
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly orders = signal<Pedido[]>([]);
  readonly expanded = signal<Set<number>>(new Set());

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
      const byEstado = !est || o.estado === est;
      return byQuery && byEstado && byMesa;
    });
    const dir = this.sortDir();
    return list.slice().sort((a, b) => {
      // Primero: los no vendidos antes que los vendidos
      const av = a.vendido ? 1 : 0;
      const bv = b.vendido ? 1 : 0;
      if (av !== bv) return av - bv;
      // Segundo: ordenar por hora segun sortDir
      const ta = new Date(a.creado_en).getTime();
      const tb = new Date(b.creado_en).getTime();
      return dir === 'asc' ? ta - tb : tb - ta;
    });
  });

  constructor(
    private readonly orderService: OrderService,
    private readonly mesaService: MesaService,
    private readonly saleService: SaleService,
    public readonly auth: AuthService,
    private readonly router: Router
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
    if (open.has(id)) { this.expanded.set(new Set()); return; }
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

  toggleSort(): void { this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc'); }
  onEstadoChange(valor: string): void { this.estadoFilter.set(valor); }

  onSell(o: Pedido): void {
    if (o.estado !== 'ENTREGADO') return;
    // Navegar al formulario de venta
    window.location.href = `/ventas/nueva/${o.id}`;
  }

  onTicket(o: Pedido): void {
    if (!o.ventaId) { alert('No se encontró el identificador de la venta.'); return; }
    this.saleService.getTicket(o.ventaId).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      error: err => {
        console.error('Error obteniendo ticket', err);
        alert('No se pudo descargar el ticket');
      }
    });
  }

  onLogout(ev: Event): void {
    ev.preventDefault();
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
