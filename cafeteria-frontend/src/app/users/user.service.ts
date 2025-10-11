import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { BackendUsuario, User } from './user.model';

const BASE = '/api/usuarios';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private readonly http: HttpClient) {}

  private adapt(u: BackendUsuario): User {
    return {
      id: u.id,
      dpi: u.dpi,
      nombre: u.nombre,
      usuario: u.usuario,
      contrasena: u.contrasenia ?? '',
      rol: u.rolCodigo,
      activo: typeof u.activo === 'boolean' ? u.activo : true,
      estado: typeof u.activo === 'boolean' ? (u.activo ? 'Activo' : 'Inactivo') : 'Activo',
      email: u.email,
      telefono: u.telefono,
      direccion: u.direccion,
    };
  }

  getUsers(filters?: { nombre?: string; rol?: string; estado?: string }): Observable<User[]> {
    let params = new HttpParams();
    if (filters?.nombre) params = params.set('nombre', filters.nombre);
    if (filters?.rol) params = params.set('rol', filters.rol);
    if (filters?.estado) params = params.set('estado', filters.estado);
    return this.http.get<unknown>(BASE, { params }).pipe(
      map((resp: unknown) => {
        // Soportar respuesta como array directo o como objeto paginado { content: [] }
        const anyResp: any = resp as any;
        const list: BackendUsuario[] = Array.isArray(anyResp)
          ? (anyResp as BackendUsuario[])
          : Array.isArray(anyResp?.content)
            ? (anyResp.content as BackendUsuario[])
            : Array.isArray(anyResp?.data)
              ? (anyResp.data as BackendUsuario[])
              : [];
        return list.map(u => this.adapt(u));
      })
    );
  }

  createUser(payload: BackendUsuario): Observable<User> {
    return this.http.post<BackendUsuario>(BASE, payload).pipe(map(u => this.adapt(u)));
  }

  updateUser(id: number | string, payload: BackendUsuario): Observable<User> {
    return this.http.put<BackendUsuario>(`${BASE}/${id}`, payload).pipe(map(u => this.adapt(u)));
  }

  getUser(id: number | string): Observable<User> {
    return this.http.get<BackendUsuario>(`${BASE}/${id}`).pipe(map(u => this.adapt(u)));
  }

  deleteUser(id: number | string): Observable<void> {
    return this.http.delete<void>(`${BASE}/${id}`);
  }
}
