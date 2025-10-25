import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category } from './category.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly BASE = `${environment.apiBaseUrl}/categorias`;
  constructor(private readonly http: HttpClient) {}
  getCategories(): Observable<Category[]> { return this.http.get<Category[]>(this.BASE); }
}
