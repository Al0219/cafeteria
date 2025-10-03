package com.app.cafeteria.services.pedido;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.cafeteria.dtos.pedido.PedidoCerrarRequest;
import com.app.cafeteria.dtos.pedido.PedidoCreateRequest;
import com.app.cafeteria.dtos.pedido.PedidoDetalleRequest;
import com.app.cafeteria.dtos.pedido.PedidoDetalleResponse;
import com.app.cafeteria.dtos.pedido.PedidoResponse;
import com.app.cafeteria.dtos.pedido.PedidoUpdateRequest;
import com.app.cafeteria.entities.Mesa;
import com.app.cafeteria.entities.Pedido;
import com.app.cafeteria.entities.PedidoDetalle;
import com.app.cafeteria.entities.PedidoEstadoLog;
import com.app.cafeteria.entities.Producto;
import com.app.cafeteria.entities.TipoServicio;
import com.app.cafeteria.entities.Usuario;
import com.app.cafeteria.entities.enums.EstadoPedido;
import com.app.cafeteria.repositories.MesaRepository;
import com.app.cafeteria.repositories.PedidoDetalleRepository;
import com.app.cafeteria.repositories.PedidoEstadoLogRepository;
import com.app.cafeteria.repositories.PedidoRepository;
import com.app.cafeteria.repositories.ProductoRepository;
import com.app.cafeteria.repositories.TipoServicioRepository;
import com.app.cafeteria.repositories.UsuarioRepository;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Map<EstadoPedido, EstadoPedido> SIGUIENTE_ESTADO = new EnumMap<>(EstadoPedido.class);
    static {
        SIGUIENTE_ESTADO.put(EstadoPedido.RECIBIDO, EstadoPedido.PREPARANDO);
        SIGUIENTE_ESTADO.put(EstadoPedido.PREPARANDO, EstadoPedido.LISTO);
        SIGUIENTE_ESTADO.put(EstadoPedido.LISTO, EstadoPedido.ENTREGADO);
    }

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final PedidoEstadoLogRepository pedidoEstadoLogRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;
    private final TipoServicioRepository tipoServicioRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                              PedidoDetalleRepository pedidoDetalleRepository,
                              PedidoEstadoLogRepository pedidoEstadoLogRepository,
                              ProductoRepository productoRepository,
                              UsuarioRepository usuarioRepository,
                              MesaRepository mesaRepository,
                              TipoServicioRepository tipoServicioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
        this.pedidoEstadoLogRepository = pedidoEstadoLogRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.mesaRepository = mesaRepository;
        this.tipoServicioRepository = tipoServicioRepository;
    }

    @Override
    @Transactional
    public PedidoResponse crearPedido(PedidoCreateRequest request) {
        Usuario usuario = obtenerUsuario(request.usuarioId());
        TipoServicio tipoServicio = obtenerTipoServicio(request.tipoServicioId());
        Mesa mesa = null;
        if (request.mesaId() != null) {
            mesa = obtenerMesaDisponible(request.mesaId(), null);
        }

        String cliente = request.clienteNombre() == null || request.clienteNombre().isBlank()
                ? "CF"
                : request.clienteNombre();

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        Pedido pedido = Pedido.builder()
                .tipo(tipoServicio)
                .mesa(mesa)
                .clienteNombre(cliente)
                .estado(EstadoPedido.RECIBIDO)
                .vendido(Boolean.FALSE)
                .notas(request.notas())
                .creadoEn(ahora)
                .actualizadoEn(ahora)
                .creadoPor(usuario)
                .build();

        Pedido guardado = pedidoRepository.save(pedido);
        procesarDetallesNuevaLista(guardado, request.detalles());
        registrarLog(guardado, EstadoPedido.RECIBIDO, usuario, ahora);

        List<PedidoDetalle> detalles = pedidoDetalleRepository.findByPedidoId(guardado.getId());
        return toResponse(guardado, detalles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll().stream()
                .map(pedido -> toResponse(pedido, pedidoDetalleRepository.findByPedidoId(pedido.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PedidoResponse actualizarPedido(Integer id, PedidoUpdateRequest request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        Usuario usuario = obtenerUsuario(request.usuarioId());
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        boolean requiereModoEdicion = request.detalles() != null
                || request.notas() != null
                || request.mesaId() != null
                || request.clienteNombre() != null;
        if (requiereModoEdicion && pedido.getEstado() != EstadoPedido.RECIBIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido ya no puede modificarse en el estado actual");
        }

        if (request.mesaId() != null) {
            Mesa mesa = obtenerMesaDisponible(request.mesaId(), pedido);
            if (pedido.getMesa() == null || !Objects.equals(pedido.getMesa().getId(), mesa.getId())) {
                pedido.setMesa(mesa);
            }
        }

        if (request.clienteNombre() != null) {
            String cliente = request.clienteNombre().isBlank() ? "CF" : request.clienteNombre();
            pedido.setClienteNombre(cliente);
        }

        if (request.notas() != null) {
            pedido.setNotas(request.notas());
        }

        if (request.detalles() != null) {
            rehacerDetalles(pedido, request.detalles());
        }

        if (request.nuevoEstado() != null) {
            manejarCambioEstado(pedido, request.nuevoEstado(), usuario, ahora);
        } else {
            pedido.setActualizadoEn(ahora);
        }

        Pedido actualizado = pedidoRepository.save(pedido);
        List<PedidoDetalle> detalles = pedidoDetalleRepository.findByPedidoId(actualizado.getId());
        return toResponse(actualizado, detalles);
    }

    @Override
    @Transactional
    public PedidoResponse cerrarPedido(Integer id, PedidoCerrarRequest request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        if (pedido.getEstado() != EstadoPedido.LISTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden cerrar pedidos en estado LISTO");
        }
        if (Boolean.TRUE.equals(pedido.getVendido())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido ya fue cerrado");
        }

        Usuario usuario = obtenerUsuario(request.usuarioId());
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setVendido(true);
        pedido.setActualizadoEn(ahora);
        Pedido actualizado = pedidoRepository.save(pedido);
        registrarLog(actualizado, EstadoPedido.ENTREGADO, usuario, ahora);

        List<PedidoDetalle> detalles = pedidoDetalleRepository.findByPedidoId(actualizado.getId());
        return toResponse(actualizado, detalles);
    }

    private void rehacerDetalles(Pedido pedido, List<PedidoDetalleRequest> nuevosDetalles) {
        List<PedidoDetalle> actuales = pedidoDetalleRepository.findByPedidoId(pedido.getId());
        restituirStock(actuales);
        pedidoDetalleRepository.deleteAll(actuales);
        procesarDetallesNuevaLista(pedido, nuevosDetalles);
    }

    private void procesarDetallesNuevaLista(Pedido pedido, List<PedidoDetalleRequest> detalleRequests) {
        List<PedidoDetalle> nuevos = new ArrayList<>();
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        for (PedidoDetalleRequest detalleRequest : detalleRequests) {
            Producto producto = productoRepository.findById(detalleRequest.productoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no encontrado"));
            if (Boolean.FALSE.equals(producto.getActivo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inactivo");
            }
            if (producto.getStock() < detalleRequest.cantidad()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - detalleRequest.cantidad());
            productoRepository.save(producto);

            PedidoDetalle detalle = PedidoDetalle.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .nombreProducto(producto.getNombre())
                    .precioUnitario(producto.getPrecio())
                    .cantidad(detalleRequest.cantidad())
                    .observaciones(detalleRequest.observaciones())
                    .build();
            nuevos.add(detalle);
        }

        pedidoDetalleRepository.saveAll(nuevos);
        pedido.setActualizadoEn(ahora);
    }

    private void restituirStock(List<PedidoDetalle> detalles) {
        for (PedidoDetalle detalle : detalles) {
            Producto producto = detalle.getProducto();
            if (producto != null) {
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }
    }

    private void manejarCambioEstado(Pedido pedido, EstadoPedido nuevoEstado, Usuario usuario, OffsetDateTime ahora) {
        EstadoPedido estadoActual = pedido.getEstado();
        if (nuevoEstado == estadoActual) {
            pedido.setActualizadoEn(ahora);
            return;
        }
        EstadoPedido siguiente = SIGUIENTE_ESTADO.get(estadoActual);
        if (siguiente == null || siguiente != nuevoEstado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transicion de estado no permitida");
        }
        if (nuevoEstado == EstadoPedido.ENTREGADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use el endpoint de cierre para marcar como entregado");
        }
        pedido.setEstado(nuevoEstado);
        pedido.setActualizadoEn(ahora);
        registrarLog(pedido, nuevoEstado, usuario, ahora);
    }

    private void registrarLog(Pedido pedido, EstadoPedido estado, Usuario usuario, OffsetDateTime cuando) {
        PedidoEstadoLog log = PedidoEstadoLog.builder()
                .pedido(pedido)
                .estado(estado)
                .cambiadoPor(usuario)
                .cambiadoEn(cuando)
                .build();
        pedidoEstadoLogRepository.save(log);
    }

    private Usuario obtenerUsuario(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));
    }

    private TipoServicio obtenerTipoServicio(Integer tipoServicioId) {
        return tipoServicioRepository.findById(tipoServicioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de servicio no encontrado"));
    }

    private Mesa obtenerMesaDisponible(Integer mesaId, Pedido pedidoActual) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesa no encontrada"));
        if (Boolean.FALSE.equals(mesa.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mesa esta inactiva");
        }
        boolean esMismaMesa = pedidoActual != null && pedidoActual.getMesa() != null
                && Objects.equals(pedidoActual.getMesa().getId(), mesaId);
        if (!esMismaMesa && pedidoRepository.existsByMesaIdAndVendidoFalseAndEstadoNot(mesaId, EstadoPedido.ENTREGADO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La mesa ya tiene un pedido en curso");
        }
        return mesa;
    }

    private PedidoResponse toResponse(Pedido pedido, List<PedidoDetalle> detalles) {
        List<PedidoDetalleResponse> detalleResponses = detalles.stream()
                .map(detalle -> new PedidoDetalleResponse(
                        detalle.getId(),
                        detalle.getProducto() != null ? detalle.getProducto().getId() : null,
                        detalle.getNombreProducto(),
                        detalle.getPrecioUnitario(),
                        detalle.getCantidad(),
                        detalle.getObservaciones()
                ))
                .collect(Collectors.toList());

        return new PedidoResponse(
                pedido.getId(),
                pedido.getTipo() != null ? pedido.getTipo().getId() : null,
                pedido.getTipo() != null ? pedido.getTipo().getNombre() : null,
                pedido.getMesa() != null ? pedido.getMesa().getId() : null,
                pedido.getMesa() != null ? pedido.getMesa().getNombre() : null,
                pedido.getClienteNombre(),
                pedido.getEstado(),
                pedido.getVendido(),
                pedido.getNotas(),
                pedido.getCreadoEn(),
                pedido.getActualizadoEn(),
                pedido.getCreadoPor() != null ? pedido.getCreadoPor().getId() : null,
                pedido.getCreadoPor() != null ? pedido.getCreadoPor().getNombre() : null,
                detalleResponses
        );
    }
}
