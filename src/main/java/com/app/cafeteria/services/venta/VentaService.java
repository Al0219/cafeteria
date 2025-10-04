package com.app.cafeteria.services.venta;

import com.app.cafeteria.dtos.venta.VentaCreateRequest;
import com.app.cafeteria.dtos.venta.VentaResponse;

public interface VentaService {

    VentaResponse registrarVenta(VentaCreateRequest request);

    byte[] generarTicket(Integer ventaId);
}
