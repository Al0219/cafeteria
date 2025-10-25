import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SaleRequest {
  pedidoId: number;
  cajeroId: number;
  descuento: number; // 0 si no aplica
  propina: number;   // 0 si no aplica
  pago: { metodoPagoId: number; monto: number };
}

export interface SaleResponse { id: number; total: number; }

@Injectable({ providedIn: 'root' })
export class SaleService {
  private readonly BASE = `${environment.apiBaseUrl}/ventas`;
  constructor(private readonly http: HttpClient) {}
  crearVenta(body: SaleRequest): Observable<SaleResponse> {
    return this.http.post<SaleResponse>(this.BASE, body);
  }
  getTicket(ventaId: number): Observable<Blob> {
    return this.http.get(`${this.BASE}/${ventaId}/ticket`, { responseType: 'blob' });
  }
}
