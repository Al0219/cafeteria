import { Component, computed, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { MesaService } from '../kitchen/mesa.service';
import { Mesa } from '../kitchen/mesa.model';
import { OrderService } from '../kitchen/order.service';
import { Pedido } from '../kitchen/order.model';

type Side = 'left' | 'right';

interface MesaBox {
  id: number;
  nombre: string;
  side: Side; // área izquierda/derecha
  top: number; // porcentaje
  left: number; // porcentaje
}

@Component({
  selector: 'app-mesas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './mesas.component.html',
  styleUrls: ['./mesas.component.scss']
})
export class MesasComponent {
  readonly search = signal('');
  readonly categoria = signal('');

  readonly mesasApi = signal<Mesa[]>([]);
  readonly editing = signal(false);

  // posiciones persistidas { [id]: { side, top, left } }
  private readonly positions = signal<Record<number, { side: Side; top: number; left: number }>>(
    (() => { try { return JSON.parse(localStorage.getItem('mesas-layout-v1') || '{}') as any; } catch { return {}; } })()
  );

  @ViewChild('leftZone') leftZone?: ElementRef<HTMLDivElement>;
  @ViewChild('rightZone') rightZone?: ElementRef<HTMLDivElement>;

  constructor(
    private readonly mesaService: MesaService,
    private readonly router: Router,
    private readonly orderService: OrderService,
    public readonly auth: AuthService
  ) {
    this.mesaService.getMesas().subscribe({
      next: ms => this.mesasApi.set(ms),
      error: err => console.error('Error cargando mesas', err)
    });
    this.orderService.getOrders().subscribe({
      next: list => this.orders.set(list),
      error: err => console.error('Error cargando pedidos', err)
    });
  }

  // Distribución automática: ubica las mesas en dos zonas con una cuadrícula suave
  readonly mesas = computed<MesaBox[]>(() => {
    const term = this.search().trim().toLowerCase();
    const cat = this.categoria();
    const data = this.mesasApi().filter(m => {
      const byCat = !cat || m.tipoNombre === cat || (cat === 'Barra' && (m.nombre ?? '').toUpperCase().includes('BARRA')) || (cat === 'Mesa' && !(m.nombre ?? '').toUpperCase().includes('BARRA'));
      const byQuery = !term || (m.nombre || '').toLowerCase().includes(term) || (m.tipoNombre || '').toLowerCase().includes(term);
      return byCat && byQuery;
    });

    // Si hay posiciones guardadas, utilízalas
    const saved = this.positions() || {};
    const placed: MesaBox[] = [];
    data.forEach(m => {
      const s = saved[m.id];
      if (s) placed.push({ id: m.id, nombre: m.nombre, side: s.side, top: s.top, left: s.left });
    });

    const remaining = data.filter(m => !saved[m.id]);
    const leftArr: Mesa[] = [];
    const rightArr: Mesa[] = [];
    remaining.forEach((m, i) => (i % 2 === 0 ? leftArr : rightArr).push(m));

    const placeAuto = (arr: Mesa[], side: Side): MesaBox[] => {
      const rows = Math.max(1, Math.ceil(arr.length / 3));
      return arr.map((m, idx) => {
        const col = idx % 3; // 0..2
        const row = Math.floor(idx / 3); // 0..rows
        const top = 15 + (row * (70 / Math.max(1, rows - 1 || 1)));
        const left = 10 + col * 20 + (col === 1 ? 5 : 0); // espaciado
        return { id: m.id, nombre: m.nombre, side, top: Math.min(90, top), left: Math.min(90, left + (side === 'left' ? 0 : 10)) } as MesaBox;
      }).map(b => ({ id: b.id, nombre: b.nombre, side, top: b.top, left: b.left }));
    };

    return [...placed, ...placeAuto(leftArr, 'left'), ...placeAuto(rightArr, 'right')];
  });

  // Pedidos en curso para determinar ocupación
  readonly orders = signal<Pedido[]>([]);
  readonly occupiedMap = computed<Record<number, boolean>>(() => {
    const map: Record<number, boolean> = {};
    for (const o of this.orders()) {
      const numero = o.mesaNumero ?? null;
      if (numero != null) {
        const busy = o.estado !== 'ENTREGADO' && !o.vendido;
        map[numero] = map[numero] || busy;
      }
    }
    return map;
  });

  // UI actions
  goCarry(): void { this.router.navigate(['/pedir'], { queryParams: { carry: 1 } }); }
  toggleEdit(): void { this.editing.set(!this.editing()); }

  onLogout(ev: Event): void {
    ev.preventDefault();
    this.auth.logout();
    this.router.navigate(['/']);
  }

  onDragStart(m: MesaBox, ev: DragEvent): void {
    if (!this.editing()) { ev.preventDefault(); return; }
    ev.dataTransfer?.setData('mesaId', String(m.id));
    ev.dataTransfer?.setData('side', m.side);
  }
  onDrop(side: Side, ev: DragEvent): void {
    if (!this.editing()) return;
    ev.preventDefault();
    const idStr = ev.dataTransfer?.getData('mesaId');
    if (!idStr) return;
    const id = Number(idStr);
    const zone = side === 'left' ? this.leftZone?.nativeElement : this.rightZone?.nativeElement;
    if (!zone) return;
    const rect = zone.getBoundingClientRect();
    const x = (ev.clientX - rect.left) / rect.width * 100;
    const y = (ev.clientY - rect.top) / rect.height * 100;
    const pos = { side, top: Math.max(5, Math.min(95, y)), left: Math.max(5, Math.min(95, x)) };
    const next = { ...(this.positions() || {}) } as any;
    next[id] = pos;
    this.positions.set(next);
    localStorage.setItem('mesas-layout-v1', JSON.stringify(next));
  }
}
