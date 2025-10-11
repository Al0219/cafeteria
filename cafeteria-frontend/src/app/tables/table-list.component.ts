import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MesaService } from '../kitchen/mesa.service';
import { Mesa } from '../kitchen/mesa.model';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-table-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './table-list.component.html',
  styleUrls: ['./table-list.component.scss']
})
export class TableListComponent implements OnInit {
  mesas: Mesa[] = [];
  filteredMesas: Mesa[] = [];
  estadoFilter: string = 'todas';
  searchQuery: string = '';

  constructor(private mesaService: MesaService, private router: Router) { }

  ngOnInit(): void {
    this.mesaService.getMesas().subscribe({
      next: (data) => {
        this.mesas = data;
        this.applyFilter();
      },
      error: (err) => {
        console.error('Error al cargar las mesas', err);
      }
    });
  }

  applyFilter(): void {
    let tempMesas = this.mesas;

    // Filter by state
    if (this.estadoFilter !== 'todas') {
      tempMesas = tempMesas.filter(mesa => mesa.estado === this.estadoFilter);
    }

    // Filter by search query (e.g., table name)
    if (this.searchQuery) {
      tempMesas = tempMesas.filter(mesa =>
        mesa.nombre.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    }

    this.filteredMesas = tempMesas;
  }

  onEstadoChange(event: Event): void {
    this.estadoFilter = (event.target as HTMLSelectElement).value;
    this.applyFilter();
  }

  onSearchChange(event: Event): void {
    this.searchQuery = (event.target as HTMLInputElement).value;
    this.applyFilter();
  }

  onTableClick(mesa: Mesa): void {
    let currentState = mesa.estado;
    let newState: 'libre' | 'reservada' | 'ocupada';

    if (currentState === 'libre') {
      newState = 'reservada';
    } else if (currentState === 'reservada') {
      newState = 'ocupada';
    } else if (currentState === 'ocupada') {
      newState = 'libre';
    } else {
      newState = 'libre'; // Default state if somehow undefined
    }
    mesa.estado = newState;
    // Optionally, update the backend with the new state here
    this.applyFilter(); // Re-apply filter to update display if needed
  }

  goToPedidos(): void {
    this.router.navigate(['/pedidos']);
  }

  goToControlPedidos(): void {
    this.router.navigate(['/cocina']);
  }

  goBack(): void {
    this.router.navigate(['/home']);
  }

  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/']);
  }
}
