import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginPayload { usernameOrEmail: string; password: string; }
export interface LoginResponse {
  userId: number; username: string; email: string; nombre: string;
  roles: string[]; token: string; expiresAt: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly KEY = 'auth.token';
  private readonly ROLES_KEY = 'auth.roles';
  readonly token = signal<string | null>(this.loadToken());
  readonly roles = signal<string[]>(this.loadRoles());
  readonly isLoggedIn = computed(() => !!this.token());

  constructor(private readonly http: HttpClient) {}

  login(body: LoginPayload): Observable<LoginResponse> {
    const url = `${environment.apiBaseUrl}/auth/login`;
    return this.http.post<LoginResponse>(url, body).pipe(
      tap(res => {
        if (res?.token) {
          localStorage.setItem(this.KEY, res.token);
          this.token.set(res.token);
        }
        if (Array.isArray(res?.roles)) {
          localStorage.setItem(this.ROLES_KEY, JSON.stringify(res.roles));
          this.roles.set(res.roles);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.KEY);
    localStorage.removeItem(this.ROLES_KEY);
    this.token.set(null);
    this.roles.set([]);
  }

  private loadToken(): string | null { return localStorage.getItem(this.KEY); }
  private loadRoles(): string[] {
    try { const raw = localStorage.getItem(this.ROLES_KEY); return raw ? JSON.parse(raw) : []; } catch { return []; }
  }

  has(role: string): boolean { return this.roles().includes(role); }
  hasAny(allowed: string[]): boolean {
    const set = new Set(this.roles());
    return allowed.some(r => set.has(r));
  }

  homeRoute(): string {
    const rs = new Set(this.roles());
    if (rs.has('ADMIN') || rs.has('GERENTE')) return '/pedidos';
    if (rs.has('CAJERO')) return '/pedidos2';
    if (rs.has('COCINA')) return '/cocina';
    if (rs.has('MESERO')) return '/mesas';
    return '/';
  }
}
