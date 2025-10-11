export interface Product {
  id?: number;
  nombre: string;
  categoria: string;
  categoriaId?: number;
  precio: number;
  stock: number;
  estado: 'Activo' | 'Inactivo';
  descripcion?: string;
}
