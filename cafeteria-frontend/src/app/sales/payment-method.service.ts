import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentMethod } from './payment-method.model';

@Injectable({ providedIn: 'root' })
export class PaymentMethodService {
  private readonly BASE = '/api/metodos-pago';
  constructor(private readonly http: HttpClient) {}
  getMethods(): Observable<PaymentMethod[]> { return this.http.get<PaymentMethod[]>(this.BASE); }
}

