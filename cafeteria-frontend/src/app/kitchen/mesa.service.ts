import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Mesa } from './mesa.model';

@Injectable({ providedIn: 'root' })
export class MesaService {
  private readonly BASE = '/api/mesas';
  constructor(private readonly http: HttpClient) {}

  getMesas(): Observable<Mesa[]> {
    return this.http.get<Mesa[]>(this.BASE).pipe(
      map(mesas => mesas.map(mesa => {
        // Assign placeholder positions based on mesa name for now
        // In a real application, these would come from the backend or a more sophisticated layout system
        switch (mesa.nombre.toLowerCase()) {
          case 'barra 1': return { ...mesa, topPosition: 10, leftPosition: 20 };
          case 'mesa 2': return { ...mesa, topPosition: 35, leftPosition: 30 };
          case 'mesa 1': return { ...mesa, topPosition: 50, leftPosition: 10 };
          case 'mesa 3': return { ...mesa, topPosition: 65, leftPosition: 30 };
          case 'mesa 4': return { ...mesa, topPosition: 20, leftPosition: 60 };
          case 'mesa 5': return { ...mesa, topPosition: 45, leftPosition: 70 };
          case 'mesa 6': return { ...mesa, topPosition: 60, leftPosition: 85 };
          case 'barra 2': return { ...mesa, topPosition: 75, leftPosition: 65 };
          default: return { ...mesa, topPosition: Math.random() * 80, leftPosition: Math.random() * 80 }; // Random for others
        }
      }))
    );
  }
}

