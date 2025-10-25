import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Product } from './product.model';
import { environment } from '../../environments/environment';

interface BackendProducto {
  id?: number;
  nombre: string;
  categoriaId: number;
  categoriaNombre?: string;
  precio: number;
  stock: number;
  imagenUrl?: string;
  descripcion?: string;
  activo?: boolean;
}

const BASE = `${environment.apiBaseUrl}/productos`;

@Injectable({ providedIn: 'root' })
export class ProductService {
  constructor(private readonly http: HttpClient) {}

  private adapt(p: BackendProducto): Product {
    return {
      id: p.id,
      nombre: p.nombre,
      categoria: p.categoriaNombre ?? String(p.categoriaId ?? ''),
      categoriaId: p.categoriaId,
      precio: p.precio,
      stock: p.stock,
      imagenUrl: p.imagenUrl,
      estado: p.activo === false ? 'Inactivo' : 'Activo',
      descripcion: p.descripcion
    };
  }

  getProducts(filters?: { categoriaId?: number; estado?: 'Activo' | 'Inactivo'; nombre?: string }): Observable<Product[]> {
    let params = new HttpParams();
    if (filters?.categoriaId != null) params = params.set('categoriaId', String(filters.categoriaId));
    if (filters?.estado) params = params.set('estado', filters.estado);
    if (filters?.nombre) params = params.set('nombre', filters.nombre);

    return this.http.get<unknown>(BASE, { params }).pipe(
      map((resp: any) => {
        const list: BackendProducto[] = Array.isArray(resp)
          ? resp
          : Array.isArray(resp?.content)
            ? resp.content
            : Array.isArray(resp?.data)
              ? resp.data
              : [];
        return list.map(p => this.adapt(p));
      })
    );
  }

  createProduct(payload: BackendProducto): Observable<Product> {
    return this.http.post<BackendProducto>(BASE, payload).pipe(map(p => this.adapt(p)));
  }

  updateProduct(id: number | string, payload: BackendProducto): Observable<Product> {
    return this.http.put<BackendProducto>(`${BASE}/${id}`, payload).pipe(map(p => this.adapt(p)));
  }
}
