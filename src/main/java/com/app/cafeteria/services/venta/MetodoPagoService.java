package com.app.cafeteria.services.venta;

import java.util.List;
import com.app.cafeteria.dtos.venta.MetodoPagoResponse;

public interface MetodoPagoService {
    List<MetodoPagoResponse> listar();
}

