import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mesa } from './mesa.model';

@Injectable({ providedIn: 'root' })
export class MesaService {
  private readonly BASE = '/api/mesas';
  constructor(private readonly http: HttpClient) {}
  getMesas(): Observable<Mesa[]> { return this.http.get<Mesa[]>(this.BASE); }
}

