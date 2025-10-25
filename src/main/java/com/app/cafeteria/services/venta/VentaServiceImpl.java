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

        VentaResponse r = toResponse(venta, detalles, pago);
        String html = "" +
                "<!DOCTYPE html>" +
                "<html lang=\"es\">" +
                "<head>" +
                "<meta charset=\"utf-8\"/>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>" +
                "<title>Ticket " + r.id() + "</title>" +
                "<style>" +
                "body{font-family: -apple-system, Segoe UI, Roboto, Arial, sans-serif; margin:0; padding:0; background:#fff;}" +
                ".ticket{width:300px; margin:0 auto; padding:12px 10px; }" +
                ".actions{ text-align:right; margin-bottom:8px; }" +
                ".print{ background:#111; color:#fff; border:0; padding:6px 10px; border-radius:4px; cursor:pointer; }" +
                ".print:active{ transform: translateY(1px); }" +
                ".header{ text-align:center; }" +
                ".header h1{ font-size:16px; margin:0 0 4px;}" +
                ".header .sub{ font-size:12px; color:#555; }" +
                ".line{ border-top:1px dashed #999; margin:8px 0; }" +
                ".row{ display:flex; font-size:13px; }" +
                ".row .label{ flex:1; color:#444;}" +
                ".row .value{ text-align:right; min-width:80px;}" +
                ".items{ width:100%; border-collapse:collapse; font-size:13px;}" +
                ".items th, .items td{ padding:4px 0;}" +
                ".items th{ text-align:left; border-bottom:1px solid #ddd; font-weight:600;}" +
                ".totals .row{ font-weight:600;}" +
                ".footer{ text-align:center; font-size:12px; color:#555; margin-top:10px;}" +
                "@media print{ .ticket{ width:72mm; } .actions{ display:none; } }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"ticket\">" +
                "<div class=\"actions\"><button class=\"print\" onclick=\"window.print()\">Imprimir</button></div>" +
                "<div class=\"header\">" +
                "<h1>Cafetería</h1>" +
                "<div class=\"sub\">Ticket #" + r.id() + "</div>" +
                "</div>" +
                "<div class=\"line\"></div>" +
                "<div class=\"row\"><div class=\"label\">Fecha</div><div class=\"value\">" + r.fecha() + "</div></div>" +
                "<div class=\"row\"><div class=\"label\">Cajero</div><div class=\"value\">" + (r.cajeroNombre() != null ? r.cajeroNombre() : "-") + "</div></div>" +
                "<div class=\"line\"></div>" +
                "<table class=\"items\">" +
                "<thead><tr><th>Producto</th><th style=\"text-align:right\">Cant</th><th style=\"text-align:right\">Precio</th></tr></thead>" +
                "<tbody>" +
                r.detalles().stream().map(d ->
                        "<tr>" +
                        "<td>" + (d.nombreProducto() != null ? d.nombreProducto() : "") + "</td>" +
                        "<td style=\\\"text-align:right\\\">" + d.cantidad() + "</td>" +
                        "<td style=\\\"text-align:right\\\">Q " + d.precioUnitario() + "</td>" +
                        "</tr>")
                        .collect(Collectors.joining()) +
                "</tbody></table>" +
                "<div class=\"line\"></div>" +
                "<div class=\"totals\">" +
                "<div class=\"row\"><div class=\"label\">Subtotal</div><div class=\"value\">Q " + r.subtotal() + "</div></div>" +
                "<div class=\"row\"><div class=\"label\">Descuento</div><div class=\"value\">- Q " + r.descuento() + "</div></div>" +
                "<div class=\"row\"><div class=\"label\">Propina</div><div class=\"value\">Q " + r.propina() + "</div></div>" +
                "<div class=\"row\"><div class=\"label\">Total</div><div class=\"value\">Q " + r.total() + "</div></div>" +
                "</div>" +
                (r.pago() != null ? "<div class=\\\"row\\\"><div class=\\\"label\\\">Pago (" + r.pago().metodoPagoNombre() + ")</div><div class=\\\"value\\\">Q " + r.pago().monto() + "</div></div>" : "") +
                "<div class=\"row\"><div class=\"label\">Impuesto</div><div class=\"value\">Q " + r.impuesto() + "</div></div>" +
                "<div class=\"line\"></div>" +
                "<div class=\"footer\">Gracias por su compra</div>" +
                "</div>" +
                "</body></html>";

        return html.getBytes();
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


