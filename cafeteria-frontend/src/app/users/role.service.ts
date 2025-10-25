import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Role } from './role.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RoleService {
  private readonly BASE = `${environment.apiBaseUrl}/roles`;
  constructor(private readonly http: HttpClient) {}
  getRoles(): Observable<Role[]> { return this.http.get<Role[]>(this.BASE); }
}
