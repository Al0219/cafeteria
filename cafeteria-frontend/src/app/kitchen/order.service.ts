import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay, map } from 'rxjs/operators';
import { Pedido, PedidoEstado } from './order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private readonly http: HttpClient) {}
  private readonly LIST_BASE = '/api/pedidos';
  private readonly KITCHEN_BASE = '/api/pedidos/cocina';
  private data: Pedido[] = [
    {
      id: 10254,
      creado_en: new Date().toISOString(),
      tipo: 1,
      mesaNumero: 1,
      cliente: 'Roberto López',
      estado: 'RECIBIDO',
      items: [
        { cantidad: 2, nombre: 'Hamburguesa Clásica', nota: 'sin cebolla' },
        { cantidad: 1, nombre: 'Coca-Cola 350ml' }
      ],
      notas: 'Cliente pidió sin mayonesa'
    },
    {
      id: 10255,
      creado_en: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
      tipo: 1,
      mesaNumero: 3,
      cliente: 'Ana Pérez',
      estado: 'PREPARANDO',
      items: [
        { cantidad: 1, nombre: 'Capuchino' },
        { cantidad: 1, nombre: 'Croissant' }
      ]
    },
    {
      id: 10256,
      creado_en: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
      tipo: 2,
      mesaNumero: null,
      cliente: 'Luis Márquez',
      estado: 'LISTO',
      items: [ { cantidad: 1, nombre: 'Sandwich Club' } ]
    }
  ];

  getOrders(estado?: 'RECIBIDO' | 'PREPARANDO' | 'LISTO'): Observable<Pedido[]> {
    // Llamada real al backend (lista completa). Si falla, degradar a mock local.
    const url = `${this.LIST_BASE}`;
    return this.http.get<any>(url).pipe(
      map((resp: any) => {
        const list: any[] = Array.isArray(resp)
          ? resp
          : Array.isArray(resp?.content)
            ? resp.content
            : Array.isArray(resp?.data)
              ? resp.data
              : [];

        const toPedido = (r: any): Pedido => ({
          id: r.id ?? r.pedidoId ?? r.noPedido,
          creado_en: r.creado_en ?? r.creadoEn ?? r.creado ?? r.fecha ?? new Date().toISOString(),
          tipo: (r.tipo ?? r.tipoPedido ?? 1) as 1 | 2,
          mesaNumero: r.mesaNumero ?? r.mesa ?? null,
          cliente: r.cliente ?? r.clienteNombre ?? r.nombreCliente ?? r.cliente_nombre ?? '—',
          estado: (r.estado ?? 'RECIBIDO') as any,
          items: this.mapDetalles(r),
          notas: r.notas ?? r.observaciones ?? r.nota ?? '',
          vendido: r.vendido === true,
          ventaId: r.ventaId ?? r.idVenta ?? r.venta?.id
        });

        let mapped = list.map(toPedido);
        if (estado) {
          mapped = mapped.filter(p => p.estado === estado);
        }
        // Mantener un snapshot local (opcional) para avance instantáneo
        this.data = structuredClone(mapped);
        return mapped;
      })
    );
  }

  private mapDetalles(r: any) {
    const fuentes = [
      r.items,
      r.detalles,
      r.detalle,
      r.detallesProducto,
      r.detalleProductos,
      r.pedidoDetalles,
      r.productos,
      r.productosDetalle
    ].filter(Boolean) as any[][];

    const arr = (fuentes[0] || []) as any[];
    return arr.map((it: any) => ({
      cantidad: it.cantidad ?? it.qty ?? 1,
      nombre: it.nombre ?? it.nombreProducto ?? it.productoNombre ?? it.producto ?? it.descripcion ?? 'Producto',
      nota: it.nota ?? it.observacion ?? it.observaciones ?? it.notas ?? '',
      productoId: it.productoId ?? it.idProducto ?? it.id,
      precioUnitario: Number(it.precioUnitario ?? it.precio ?? it.unitPrice ?? 0)
    }));
  }

  getOrder(id: number): Observable<Pedido> {
    return this.http.get<any>(`${this.LIST_BASE}/${id}`).pipe(
      map((r: any) => ({
        id: r.id ?? r.pedidoId ?? r.noPedido,
        creado_en: r.creado_en ?? r.creadoEn ?? r.creado ?? r.fecha ?? new Date().toISOString(),
        tipo: (r.tipo ?? r.tipoPedido ?? 1) as 1 | 2,
        mesaNumero: r.mesaNumero ?? r.mesa ?? null,
        cliente: r.cliente ?? r.clienteNombre ?? r.nombreCliente ?? r.cliente_nombre ?? '—',
        estado: (r.estado ?? 'RECIBIDO') as any,
        items: this.mapDetalles(r),
        notas: r.notas ?? r.observaciones ?? r.nota ?? '',
        vendido: r.vendido === true,
        ventaId: r.ventaId ?? r.idVenta ?? r.venta?.id
      }))
    );
  }

  avanzarEstadoLocal(id: number): Pedido {
    const idx = this.data.findIndex(p => p.id === id);
    if (idx < 0) throw new Error('Pedido no encontrado');
    const estados: PedidoEstado[] = ['RECIBIDO', 'PREPARANDO', 'LISTO', 'ENTREGADO'];
    const actual = this.data[idx];
    const nextIndex = Math.min(estados.indexOf(actual.estado) + 1, estados.length - 1);
    this.data[idx] = { ...actual, estado: estados[nextIndex] };
    return structuredClone(this.data[idx]);
  }

  // Conectar a backend: PUT /api/pedidos/cocina/{id} { usuarioId, nuevoEstado }
  avanzarEstado(order: Pedido, usuarioId = 1): Observable<Pedido> {
    const estados: PedidoEstado[] = ['RECIBIDO', 'PREPARANDO', 'LISTO', 'ENTREGADO'];
    const idx = Math.min(estados.indexOf(order.estado) + 1, estados.length - 1);
    const nuevoEstado = estados[idx];
    // Llamada al backend. Si falla o no retorna el objeto, degradar al cambio local para mantener la UI reactiva.
    return this.http
      .put<any>(`${this.KITCHEN_BASE}/${order.id}`, { usuarioId, nuevoEstado })
      .pipe(
        map(resp => {
          // Si el backend devuelve el pedido actualizado úsalo; si no, aplica cambio local
          if (resp && typeof resp === 'object' && 'estado' in resp) {
            // Actualizar mock local para consistencia (opcional)
            const updated = this.avanzarEstadoLocal(order.id);
            updated.estado = resp.estado as PedidoEstado;
            return updated;
          }
          return this.avanzarEstadoLocal(order.id);
        })
      );
  }
}
