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
        const newMesa: Mesa = { ...mesa };
        // Assign initial state based on 'activo' property
        newMesa.estado = mesa.activo ? 'libre' : 'ocupada';

        // Assign placeholder positions based on mesa name for now
        switch (mesa.nombre.toLowerCase()) {
          case 'barra 1': newMesa.topPosition = 10; newMesa.leftPosition = 20; break;
          case 'mesa 2': newMesa.topPosition = 35; newMesa.leftPosition = 30; break;
          case 'mesa 1': newMesa.topPosition = 50; newMesa.leftPosition = 10; break;
          case 'mesa 3': newMesa.topPosition = 65; newMesa.leftPosition = 30; break;
          case 'mesa 4': newMesa.topPosition = 20; newMesa.leftPosition = 60; break;
          case 'mesa 5': newMesa.topPosition = 45; newMesa.leftPosition = 70; break;
          case 'mesa 6': newMesa.topPosition = 60; newMesa.leftPosition = 85; break;
          case 'barra 2': newMesa.topPosition = 75; newMesa.leftPosition = 65; break;
          default: newMesa.topPosition = Math.random() * 80; newMesa.leftPosition = Math.random() * 80; break; // Random for others
        }
        return newMesa;
      }))
    );
  }
}

