import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';

import { ProductService } from '../menu/product.service';
import { Product } from '../menu/product.model';
import { CategoryService } from '../menu/category.service';
import { OrderService } from '../kitchen/order.service';

interface CartLine {
  id: number;
  nombre: string;
  precio: number;
  imagenUrl?: string;
  cantidad: number;
}

@Component({
  selector: 'app-order-take',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './order-take.component.html',
  styleUrls: ['./order-take.component.scss']
})
export class OrderTakeComponent implements OnInit {
  readonly showToast = signal(false);
  readonly toastMsg = signal('');

  readonly loading = signal(true);
  readonly products = signal<Product[]>([]);
  readonly categories = signal<string[]>([]);
  readonly search = signal('');
  readonly category = signal('');
  readonly isCarry = signal(true); // Para llevar
  readonly mesaNumero = signal<string>('');
  readonly clienteNombre = signal<string>('');
  readonly notas = signal<string>('');
  readonly showConfirm = signal(false);
  private lastCreateBody: any | null = null;


  // carrito como mapa id -> línea
  readonly cart = signal(new Map<number, CartLine>());

  readonly filtered = computed(() => {
    const term = this.search().trim().toLowerCase();
    const cat = this.category();
    return this.products().filter(p => {
      const byCat = !cat || p.categoria === cat;
      const byQuery = !term || p.nombre.toLowerCase().includes(term) || (p.descripcion || '').toLowerCase().includes(term);
      return byCat && byQuery;
    });
  });

  readonly subtotal = computed(() => {
    let s = 0;
    this.cart().forEach(l => s += l.precio * l.cantidad);
    return Number(s.toFixed(2));
  });

  constructor(
    private readonly productService: ProductService,
    private readonly categoryService: CategoryService,
    private readonly orderService: OrderService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    public readonly auth: AuthService
  ) {}

  onLogout(ev: Event): void {
    ev.preventDefault();
    this.auth.logout();
    this.router.navigate(['/']);
  }

  ngOnInit(): void {
    // Escuchar cambios de query params para soportar navegación desde la lista
    this.route.queryParamMap.subscribe(params => {
      const mesaParam = params.get('mesaId');
      if (mesaParam) { this.isCarry.set(false); this.mesaNumero.set(String(mesaParam)); }
      const pedidoParam = params.get('pedidoId');
      if (pedidoParam) {
        const id = Number(pedidoParam);
        this.orderService.getOrder(id).subscribe({
          next: p => {
            if (p.mesaNumero != null) { this.isCarry.set(false); this.mesaNumero.set(String(p.mesaNumero)); }
            else { this.isCarry.set(true); }
            if ((p as any).cliente) { this.clienteNombre.set((p as any).cliente); }
            if ((p as any).notas) { this.notas.set((p as any).notas); }
            const map = new Map<number, CartLine>();
            for (const it of (p.items || [])) {
              const pid = (it as any).productoId ?? (it as any).producto_id;
              if (!pid) continue;
              const line: CartLine = {
                id: Number(pid),
                nombre: it.nombre,
                precio: Number((it as any).precioUnitario ?? (it as any).precio_unitario ?? 0),
                imagenUrl: undefined,
                cantidad: Number(it.cantidad || 1)
              };
              map.set(line.id, line);
            }
            if (map.size) this.cart.set(map);
          },
          error: err => console.error('No se pudo cargar el pedido para edición', err)
        });
      }
    });
    this.productService.getProducts().subscribe(list => {
      this.products.set(list);
      // derivar categorías por nombre si la API aún no está
      const set = new Set(list.map(p => p.categoria));
      this.categories.set(Array.from(set));
      this.loading.set(false);
    });
    this.categoryService.getCategories().subscribe({
      next: cats => this.categories.set(cats.map(c => c.nombre)),
      error: () => {}
    });
  }

  addToCart(p: Product): void {
    const map = new Map(this.cart());
    const current = map.get(p.id!);
    if (current) {
      current.cantidad += 1;
    } else {
      map.set(p.id!, {
        id: p.id!,
        nombre: p.nombre,
        precio: p.precio,
        imagenUrl: (p as any).imagenUrl,
        cantidad: 1
      });
    }
    this.cart.set(map);
  }

  inc(line: CartLine): void { this.updateQty(line, line.cantidad + 1); }
  dec(line: CartLine): void { this.updateQty(line, Math.max(1, line.cantidad - 1)); }
  onQtyChange(line: CartLine, v: string): void {
    const num = Math.max(1, Number(v || 1));
    this.updateQty(line, num);
  }
  remove(line: CartLine): void {
    const map = new Map(this.cart());
    map.delete(line.id);
    this.cart.set(map);
  }
  clear(): void { this.cart.set(new Map()); }

  private updateQty(line: CartLine, qty: number): void {
    const map = new Map(this.cart());
    const l = map.get(line.id);
    if (l) { l.cantidad = qty; this.cart.set(map); }
  }

  finalize(): void {
    if (this.cart().size === 0) { alert('El carrito está vacío'); return; }
    const detalles = Array.from(this.cart().values()).map(l => ({ productoId: l.id, cantidad: l.cantidad, observaciones: null }));
    const base: any = {
      usuarioId: 1,
      clienteNombre: this.clienteNombre() || null,
      notas: this.notas() || null,
      detalles
    };
    const pedidoParam = this.route.snapshot.queryParamMap.get('pedidoId');
    if (pedidoParam) {
      // Actualizar pedido existente
      const id = Number(pedidoParam);
      const upd = {
        ...base,
        mesaId: this.isCarry() ? null : (this.mesaNumero() ? Number(this.mesaNumero()) : null),
        
        nuevoEstado: null
      };
      this.orderService.updatePedido(id, upd).subscribe({
        next: () => { this.pushToast('Pedido actualizado'); this.clear(); this.router.navigate(['/pedidos']); },
        error: err => {
          console.error('Error actualizando pedido', err);
          this.pushToast(this.friendlyError(err));
        }
      });
    } else {
      // Crear un nuevo pedido
      const body = {
        ...base,
        tipoServicioId: this.isCarry() ? 2 : 1,
        mesaId: this.isCarry() ? null : (this.mesaNumero() ? Number(this.mesaNumero()) : null)
      };
      this.lastCreateBody = body;
      this.orderService.createPedido(body).subscribe({
        next: () => { this.pushToast('Pedido enviado. Estado: RECIBIDO'); this.clear(); this.router.navigate(['/pedidos']); },
        error: err => {
          const msg = (err?.error?.message || err?.message || '').toLowerCase();
          const ocupado = err?.status === 409 || msg.includes('mesa ya tiene un pedido');
          if (ocupado) {
            this.showConfirm.set(true);
            return;
          }
          console.error('Error creando pedido', err);
          this.pushToast(this.friendlyError(err));
        }
      });
    }
  }

  confirmCreateAnother(): void {
    if (!this.lastCreateBody) { this.showConfirm.set(false); return; }
    this.orderService.createPedido(this.lastCreateBody, true).subscribe({
      next: () => {
        this.showConfirm.set(false);
        this.lastCreateBody = null;
        alert('Pedido enviado. Estado: RECIBIDO');
        this.clear();
        this.router.navigate(['/pedidos']);
      },
      error: e2 => {
        console.error('Error creando pedido (confirm)', e2);
        this.showConfirm.set(false);
        alert(e2?.error?.message || e2?.message || 'No se pudo crear el pedido');
      }
    });
  }

  cancelConflict(): void {
    this.showConfirm.set(false);
    this.lastCreateBody = null;
  }
  private friendlyError(err: any): string {
    const raw = (err?.error?.message || err?.message || '').toString();
    const m = raw.toLowerCase();
    const list = (err?.error?.errors || err?.error?.details || err?.error?.bindingErrors || []) as any[];
    if (Array.isArray(list)) {
      for (const e of list) {
        const field = (e?.field || e?.fieldName || e?.objectName || '').toString().toLowerCase();
        const msg = (e?.defaultMessage || e?.message || '').toString().toLowerCase();
        if (field.includes('cliente') || field.includes('clientenombre') || msg.includes('not blank') || msg.includes('no puede estar vac')) {
          return 'Debes ingresar el nombre del cliente.';
        }
      }
    }
    if (err?.status === 400 && (m.includes('validation failed') || m.includes("error count"))) {
      return 'Debes ingresar el nombre del cliente.';
    }
    return raw || 'No se pudo crear el pedido';
  }
  private pushToast(msg: string, ms = 2500): void {
    this.toastMsg.set(msg);
    this.showToast.set(true);
    setTimeout(() => this.showToast.set(false), ms);
  }
}
