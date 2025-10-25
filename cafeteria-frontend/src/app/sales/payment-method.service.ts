import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentMethod } from './payment-method.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PaymentMethodService {
  private readonly BASE = `${environment.apiBaseUrl}/metodos-pago`;
  constructor(private readonly http: HttpClient) {}
  getMethods(): Observable<PaymentMethod[]> { return this.http.get<PaymentMethod[]>(this.BASE); }
}
