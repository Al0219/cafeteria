export interface Mesa {
  id: number;
  nombre: string;
  tipoId?: number;
  tipoNombre?: string;
  activo?: boolean;
  topPosition?: number; // Percentage for CSS top property
  leftPosition?: number; // Percentage for CSS left property
}

