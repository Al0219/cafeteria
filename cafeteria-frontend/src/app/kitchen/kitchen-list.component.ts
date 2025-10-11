import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from './order.service';
import { Pedido } from './order.model';
import { MesaService } from './mesa.service';
import { Mesa } from './mesa.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-kitchen-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe],
  templateUrl: './kitchen-list.component.html',
  styleUrls: ['./kitchen-list.component.scss']
})
export class KitchenListComponent implements OnInit {
  readonly loading = signal(true);
  readonly search = signal('');
  readonly estadoFilter = signal<string>('');
  readonly mesaFilter = signal<string>('');
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly orders = signal<Pedido[]>([]);
  readonly expanded = signal<Set<number>>(new Set());
  readonly progressingId = signal<number | null>(null);

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
      const ta = new Date(a.creado_en).getTime();
      const tb = new Date(b.creado_en).getTime();
      return dir === 'asc' ? ta - tb : tb - ta;
    });
  });

  constructor(
    private readonly orderService: OrderService,
    private readonly mesaService: MesaService
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
    // Abrir solo uno a la vez: si ya está abierto, cerrar; si no, abrir y cargar detalles si faltan
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

  avanzar(o: Pedido): void {
    // Confirmación antes de avanzar
    const estados = ['RECIBIDO', 'PREPARANDO', 'LISTO', 'ENTREGADO'] as const;
    const idx = estados.indexOf(o.estado as any);
    const next = estados[Math.min(idx + 1, estados.length - 1)];
    const ok = confirm(`¿Avanzar el pedido ${o.id} de ${o.estado} a ${next}?`);
    if (!ok) return;
    this.progressingId.set(o.id);
    this.orderService.avanzarEstado(o, 1).subscribe({
      next: updated => {
        this.orders.update(list => list.map(x => x.id === updated.id ? updated : x));
        this.progressingId.set(null);
      },
      error: err => {
        console.error('Error avanzando estado', err);
        this.progressingId.set(null);
        alert('No se pudo avanzar el estado');
      }
    });
  }

  onEstadoChange(valor: string): void {
    // Solo ajustar el filtro en cliente; no filtrar por defecto
    this.estadoFilter.set(valor);
  }
}
