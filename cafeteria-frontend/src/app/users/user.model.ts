export interface User {
  id?: number;
  dpi: string;
  nombre: string;
  usuario: string;
  contrasena: string; // UI label
  rol: string;        // rolCodigo
  estado?: string;    // 'Activo' | 'Inactivo'
  activo?: boolean;   // fuente booleana desde backend
  email?: string;
  telefono?: string;
  direccion?: string;
}

// Backend payload shape
export interface BackendUsuario {
  id?: number;
  nombre: string;
  usuario: string;
  dpi: string;
  email?: string;
  telefono?: string;
  direccion?: string;
  rolCodigo: string;
  contrasenia?: string;
  activo?: boolean;
  rolNombre?: string;
}
