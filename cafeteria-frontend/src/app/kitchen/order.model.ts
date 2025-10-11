export type PedidoEstado = 'RECIBIDO' | 'PREPARANDO' | 'LISTO' | 'ENTREGADO';

export interface PedidoItem {
  cantidad: number;
  nombre: string;
  nota?: string;
}

export interface Pedido {
  id: number;
  creado_en: string; // ISO string o HH:mm
  tipo: 1 | 2; // 1 = en mesa, 2 = para llevar
  mesaNumero?: number | null;
  cliente: string;
  estado: PedidoEstado;
  items: PedidoItem[];
  notas?: string;
}

