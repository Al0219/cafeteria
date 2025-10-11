import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Role } from './role.model';

@Injectable({ providedIn: 'root' })
export class RoleService {
  private readonly BASE = '/api/roles';
  constructor(private readonly http: HttpClient) {}
  getRoles(): Observable<Role[]> { return this.http.get<Role[]>(this.BASE); }
}

