import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../kitchen/order.service';
import { Pedido } from '../kitchen/order.model';
import { PaymentMethodService } from './payment-method.service';
import { PaymentMethod } from './payment-method.model';
import { SaleService } from './sale.service';

@Component({
  selector: 'app-sale-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './sale-form.component.html',
  styleUrls: ['./sale-form.component.scss']
})
export class SaleFormComponent implements OnInit {
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly pedido = signal<Pedido | null>(null);
  readonly methods = signal<PaymentMethod[]>([]);

  readonly subtotal = computed(() => {
    const p = this.pedido();
    if (!p) return 0;
    return (p.items || []).reduce((acc, it) => acc + (Number(it.precioUnitario ?? 0) * Number(it.cantidad ?? 0)), 0);
  });

  readonly form = this.fb.nonNullable.group({
    descuento: [0 as number, [Validators.min(0)]],
    propina: [0 as number, [Validators.min(0)]],
    metodoPagoId: [null as number | null, Validators.required],
    monto: [0 as number, [Validators.required, Validators.min(0)]]
  });

  readonly total = computed(() => {
    const s = this.subtotal();
    const d = Number(this.form.controls.descuento.value || 0);
    const pr = Number(this.form.controls.propina.value || 0);
    return Math.max(0, Number((s - d + pr).toFixed(2)));
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly orderService: OrderService,
    private readonly paymentService: PaymentMethodService,
    private readonly saleService: SaleService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('pedidoId'));
    this.orderService.getOrder(id).subscribe({
      next: p => {
        if (p.vendido) {
          alert('El pedido ya fue vendido.');
          this.router.navigate(['/pedidos2']);
          return;
        }
        this.pedido.set(p);
        // monto por defecto = total
        this.form.patchValue({ monto: this.total() });
        this.loading.set(false);
      },
      error: err => {
        console.error('Error cargando pedido', err);
        alert('No se puede cargar el pedido. Puede que ya haya sido vendido.');
        this.router.navigate(['/pedidos2']);
      }
    });
    this.paymentService.getMethods().subscribe({
      next: m => this.methods.set(m),
      error: err => console.error('Error cargando métodos de pago', err)
    });
  }

  onSubmit(): void {
    if (!this.pedido() || this.form.invalid) { this.form.markAllAsTouched(); return; }
    const total = this.total();
    const monto = Number(this.form.controls.monto.value || 0);
    if (monto !== total) {
      alert(`El monto del pago debe ser exactamente Q ${total.toFixed(2)} (por validación del backend).`);
      return;
    }

    this.submitting.set(true);
    const body = {
      pedidoId: this.pedido()!.id,
      cajeroId: 1, // TODO: tomar de sesión cuando exista
      descuento: Number(this.form.controls.descuento.value || 0),
      propina: Number(this.form.controls.propina.value || 0),
      pago: {
        metodoPagoId: this.form.controls.metodoPagoId.value!,
        monto
      }
    };
    this.saleService.crearVenta(body).subscribe({
      next: (resp: any) => {
        this.submitting.set(false);
        const id = resp?.id ?? null;
        if (id) {
          // Descargar ticket opcionalmente
          this.saleService.getTicket(id).subscribe({
            next: blob => {
              const url = URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url; a.download = `ticket-${id}.txt`; a.click(); URL.revokeObjectURL(url);
            }
          });
        }
        alert('Venta registrada');
        this.router.navigate(['/pedidos2']);
      },
      error: err => {
        this.submitting.set(false);
        console.error('Error registrando venta', err);
        alert('No se pudo registrar la venta');
      }
    });
  }
}
