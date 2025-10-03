package com.app.cafeteria.services.venta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.cafeteria.dtos.venta.PagoRequest;
import com.app.cafeteria.dtos.venta.PagoResponse;
import com.app.cafeteria.dtos.venta.VentaCreateRequest;
import com.app.cafeteria.dtos.venta.VentaDetalleRequest;
import com.app.cafeteria.dtos.venta.VentaDetalleResponse;
import com.app.cafeteria.dtos.venta.VentaResponse;
import com.app.cafeteria.entities.MetodoPago;
import com.app.cafeteria.entities.Pago;
import com.app.cafeteria.entities.Pedido;
import com.app.cafeteria.entities.PedidoDetalle;
import com.app.cafeteria.entities.Usuario;
import com.app.cafeteria.entities.Venta;
import com.app.cafeteria.entities.VentaDetalle;
import com.app.cafeteria.entities.enums.EstadoPedido;
import com.app.cafeteria.repositories.MetodoPagoRepository;
import com.app.cafeteria.repositories.PagoRepository;
import com.app.cafeteria.repositories.PedidoDetalleRepository;
import com.app.cafeteria.repositories.PedidoRepository;
import com.app.cafeteria.repositories.UsuarioRepository;
import com.app.cafeteria.repositories.VentaDetalleRepository;
import com.app.cafeteria.repositories.VentaRepository;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            VentaDetalleRepository ventaDetalleRepository,
                            PagoRepository pagoRepository,
                            PedidoRepository pedidoRepository,
                            PedidoDetalleRepository pedidoDetalleRepository,
                            UsuarioRepository usuarioRepository,
                            MetodoPagoRepository metodoPagoRepository) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
        this.usuarioRepository = usuarioRepository;
        this.metodoPagoRepository = metodoPagoRepository;
    }

    @Override
    @Transactional
    public VentaResponse registrarVenta(VentaCreateRequest request) {
        Pedido pedido = pedidoRepository.findById(request.pedidoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido no encontrado"));
        if (Boolean.TRUE.equals(pedido.getVendido())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido ya cuenta con una venta");
        }
        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe estar ENTREGADO antes de facturarse");
        }

        Usuario cajero = usuarioRepository.findById(request.cajeroId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cajero no encontrado"));

        List<PedidoDetalle> detallesPedido = pedidoDetalleRepository.findByPedidoId(pedido.getId());
        if (detallesPedido.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido no posee detalles");
        }

        List<VentaDetalleRequest> detallesRequest = request.detalles();
        if (detallesRequest != null && !detallesRequest.isEmpty()) {
            validarDetalles(detallesRequest, detallesPedido);
        }

        BigDecimal subtotal = detallesPedido.stream()
                .map(det -> det.getPrecioUnitario().multiply(BigDecimal.valueOf(det.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuento = request.descuento();
        BigDecimal propina = request.propina();
        if (descuento.compareTo(subtotal) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El descuento no puede ser mayor al subtotal");
        }
        if (propina.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La propina no puede ser negativa");
        }

        BigDecimal total = subtotal.subtract(descuento).add(propina);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El total debe ser mayor a cero");
        }

        BigDecimal impuesto = total.multiply(BigDecimal.valueOf(12))
                .divide(BigDecimal.valueOf(112), 2, RoundingMode.HALF_UP);

        OffsetDateTime fecha = OffsetDateTime.now(ZoneOffset.UTC);

        Venta venta = Venta.builder()
                .pedido(pedido)
                .cajero(cajero)
                .fecha(fecha)
                .subtotal(subtotal)
                .descuento(descuento)
                .propina(propina)
                .impuesto(impuesto)
                .total(total)
                .build();
        Venta guardada = ventaRepository.save(venta);

        List<VentaDetalle> ventaDetalles = detallesPedido.stream()
                .map(det -> VentaDetalle.builder()
                        .venta(guardada)
                        .producto(det.getProducto())
                        .nombreProducto(det.getNombreProducto())
                        .precioUnitario(det.getPrecioUnitario())
                        .cantidad(det.getCantidad())
                        .build())
                .collect(Collectors.toList());
        ventaDetalleRepository.saveAll(ventaDetalles);

        PagoRequest pagoRequest = request.pago();
        MetodoPago metodo = metodoPagoRepository.findById(pagoRequest.metodoPagoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo de pago no encontrado"));

        if (pagoRequest.monto().compareTo(total) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto del pago debe coincidir con el total");
        }

        Pago pago = Pago.builder()
                .venta(guardada)
                .metodo(metodo)
                .monto(pagoRequest.monto())
                .build();
        pagoRepository.save(pago);

        pedido.setVendido(true);
        pedidoRepository.save(pedido);

        return toResponse(guardada, ventaDetalles, pago);
    }

    private void validarDetalles(List<VentaDetalleRequest> detallesRequest, List<PedidoDetalle> detallesPedido) {
        Map<Integer, PedidoDetalle> mapaPedido = new HashMap<>();
        for (PedidoDetalle det : detallesPedido) {
            if (det.getProducto() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido tiene un detalle sin producto asociado");
            }
            mapaPedido.put(det.getProducto().getId(), det);
        }
        for (VentaDetalleRequest detReq : detallesRequest) {
            if (detReq.productoId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Detalle sin producto");
            }
            PedidoDetalle detPedido = mapaPedido.get(detReq.productoId());
            if (detPedido == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto del detalle no pertenece al pedido");
            }
            if (detReq.cantidad() != null && !detPedido.getCantidad().equals(detReq.cantidad())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad no coincide con el pedido");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarTicket(Integer ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        List<VentaDetalle> detalles = ventaDetalleRepository.findByVentaId(ventaId);
        Pago pago = pagoRepository.findByVentaId(ventaId)
                .orElse(null);

        VentaResponse response = toResponse(venta, detalles, pago);
        StringBuilder builder = new StringBuilder();
        builder.append("Venta ").append(response.id()).append("\n");
        builder.append("Fecha: ").append(response.fecha()).append("\n");
        builder.append("Cajero: ").append(response.cajeroNombre()).append("\n");
        builder.append("Subtotal: ").append(response.subtotal()).append("\n");
        builder.append("Descuento: ").append(response.descuento()).append("\n");
        builder.append("Propina: ").append(response.propina()).append("\n");
        builder.append("Impuesto: ").append(response.impuesto()).append("\n");
        builder.append("Total: ").append(response.total()).append("\n\n");
        builder.append("Detalle:\n");
        for (VentaDetalleResponse det : response.detalles()) {
            builder.append(" - ")
                    .append(det.nombreProducto())
                    .append(" x")
                    .append(det.cantidad())
                    .append(" @")
                    .append(det.precioUnitario())
                    .append("\n");
        }
        if (response.pago() != null) {
            builder.append("\nPago: ").append(response.pago().metodoPagoNombre())
                    .append(" -> ").append(response.pago().monto());
        }
        return builder.toString().getBytes();
    }

    private VentaResponse toResponse(Venta venta, List<VentaDetalle> detalles, Pago pago) {
        List<VentaDetalleResponse> detalleResponses = detalles.stream()
                .map(det -> new VentaDetalleResponse(
                        det.getId(),
                        det.getProducto() != null ? det.getProducto().getId() : null,
                        det.getNombreProducto(),
                        det.getPrecioUnitario(),
                        det.getCantidad()
                ))
                .collect(Collectors.toList());

        PagoResponse pagoResponse = pago != null ? new PagoResponse(
                pago.getId(),
                pago.getMetodo() != null ? pago.getMetodo().getId() : null,
                pago.getMetodo() != null ? pago.getMetodo().getNombre() : null,
                pago.getMonto()
        ) : null;

        return new VentaResponse(
                venta.getId(),
                venta.getPedido() != null ? venta.getPedido().getId() : null,
                venta.getCajero() != null ? venta.getCajero().getId() : null,
                venta.getCajero() != null ? venta.getCajero().getNombre() : null,
                venta.getFecha(),
                venta.getSubtotal(),
                venta.getDescuento(),
                venta.getPropina(),
                venta.getImpuesto(),
                venta.getTotal(),
                detalleResponses,
                pagoResponse
        );
    }
}


