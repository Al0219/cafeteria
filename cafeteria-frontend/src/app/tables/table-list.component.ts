import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MesaService } from '../kitchen/mesa.service'; // Assuming MesaService is in kitchen folder
import { Mesa } from '../kitchen/mesa.model'; // Assuming Mesa model is in kitchen folder
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
      tempMesas = tempMesas.filter(mesa => {
        // Assuming 'activo' property determines if a table is free or occupied
        if (this.estadoFilter === 'libre') return mesa.activo; 
        if (this.estadoFilter === 'ocupada') return !mesa.activo; 
        // 'reservada' state would require a specific property in Mesa model
        return true;
      });
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

  goToPedidos(): void {
    this.router.navigate(['/pedidos']);
  }

  goToControlPedidos(): void {
    this.router.navigate(['/cocina']); // Assuming 'cocina' is the control de pedidos route
  }

  goBack(): void {
    this.router.navigate(['/home']); // Or a more appropriate previous page
  }

  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/']); // Navigate to login page
  }
}
