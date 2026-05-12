package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CancelacionOrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CancelarOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CrearOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarEstadoDomicilioRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AsignarRepartidorRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.DomicilioOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.DomicilioOrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.HistorialVentaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenActivaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemModRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemModResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.PagoRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.PagoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.RepartidorOptionResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.TicketResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.VentasDiaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.LineaOrden;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.LineaOrdenMod;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.MovimientoCaja;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Orden;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.OrdenDomicilio;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Pago;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaProducto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.SesionCaja;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Insumo;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.LineaOrdenRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.LineaOrdenModRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ModificadorRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.MovimientoCajaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.OrdenRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.OrdenDomicilioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.PagoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ProductoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.SesionCajaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.InsumoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;

@Service
public class VentaService {

    private final OrdenRepository ordenRepository;
    private final OrdenDomicilioRepository ordenDomicilioRepository;
    private final LineaOrdenRepository lineaOrdenRepository;
    private final LineaOrdenModRepository lineaOrdenModRepository;
    private final PagoRepository pagoRepository;
    private final ProductoRepository productoRepository;
    private final ModificadorRepository modificadorRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final InsumoRepository insumoRepository;
    private final UsuarioRepository usuarioRepository;

    public VentaService(
            OrdenRepository ordenRepository,
            OrdenDomicilioRepository ordenDomicilioRepository,
            LineaOrdenRepository lineaOrdenRepository,
            LineaOrdenModRepository lineaOrdenModRepository,
            PagoRepository pagoRepository,
            ProductoRepository productoRepository,
            ModificadorRepository modificadorRepository,
            SesionCajaRepository sesionCajaRepository,
            MovimientoCajaRepository movimientoCajaRepository,
            InsumoRepository insumoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.ordenRepository = ordenRepository;
        this.ordenDomicilioRepository = ordenDomicilioRepository;
        this.lineaOrdenRepository = lineaOrdenRepository;
        this.lineaOrdenModRepository = lineaOrdenModRepository;
        this.pagoRepository = pagoRepository;
        this.productoRepository = productoRepository;
        this.modificadorRepository = modificadorRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.insumoRepository = insumoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public OrdenResponse crearOrden(CrearOrdenRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("La orden debe incluir al menos un producto");
        }

        SesionCaja sesionCaja = sesionCajaRepository
                .findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(request.idUsuario(), "ABIERTA")
                .orElseThrow(() -> new IllegalArgumentException("El usuario no tiene una sesion de caja abierta"));

        LocalDate fechaOperacion = LocalDate.now();
        int folioDia = (int) ordenRepository.countBySesionCajaUsuarioNegocioIdNegocioAndFechaOperacion(
                sesionCaja.getUsuario().getNegocio().getIdNegocio(),
                fechaOperacion
        ) + 1;
        String tipoOrden = normalizarTipoOrden(request.tipoOrden());

        Orden orden = new Orden();
        orden.setSesionCaja(sesionCaja);
        orden.setFecha(LocalDateTime.now());
        orden.setFechaOperacion(fechaOperacion);
        orden.setFolioDia(folioDia);
        orden.setTipoOrden(tipoOrden);
        orden.setEstado("PENDIENTE_PAGO");
        orden.setTotal(BigDecimal.ZERO);
        orden = ordenRepository.save(orden);

        BigDecimal total = BigDecimal.ZERO;
        List<OrdenItemResponse> items = new ArrayList<>();

        for (OrdenItemRequest itemRequest : request.items()) {
            if (itemRequest.cantidad() == null || itemRequest.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a cero");
            }

            Producto producto = productoRepository.findById(itemRequest.idProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + itemRequest.idProducto()));

            BigDecimal totalModificadores = BigDecimal.ZERO;
            List<OrdenItemModResponse> modificadoresResponse = new ArrayList<>();
            List<OrdenItemModRequest> modificadoresRequest = itemRequest.modificadores() == null
                    ? List.of()
                    : itemRequest.modificadores();

            for (OrdenItemModRequest modificadorRequest : modificadoresRequest) {
                if (modificadorRequest == null || modificadorRequest.idModificador() == null) {
                    continue;
                }

                Modificador modificador = modificadorRepository.findById(modificadorRequest.idModificador())
                        .orElseThrow(() -> new IllegalArgumentException("Modificador no encontrado: " + modificadorRequest.idModificador()));

                if (!Objects.equals(modificador.getProducto().getIdProducto(), producto.getIdProducto())) {
                    throw new IllegalArgumentException("El modificador no pertenece al producto seleccionado");
                }

                totalModificadores = totalModificadores.add(modificador.getPrecioExtra());
                modificadoresResponse.add(new OrdenItemModResponse(
                        modificador.getIdModificador(),
                        modificador.getNombre(),
                        modificador.getPrecioExtra()
                ));
            }

            BigDecimal subtotal = producto.getPrecio()
                    .add(totalModificadores)
                    .multiply(BigDecimal.valueOf(itemRequest.cantidad()));
            total = total.add(subtotal);

            LineaOrden lineaOrden = new LineaOrden();
            lineaOrden.setOrden(orden);
            lineaOrden.setProducto(producto);
            lineaOrden.setCantidad(itemRequest.cantidad());
            lineaOrden.setNombreSnapshot(producto.getNombre());
            lineaOrden.setPrecioUnitarioSnapshot(producto.getPrecio());
            lineaOrden = lineaOrdenRepository.save(lineaOrden);

            for (OrdenItemModRequest modificadorRequest : modificadoresRequest) {
                if (modificadorRequest == null || modificadorRequest.idModificador() == null) {
                    continue;
                }

                Modificador modificador = modificadorRepository.findById(modificadorRequest.idModificador())
                        .orElseThrow(() -> new IllegalArgumentException("Modificador no encontrado: " + modificadorRequest.idModificador()));

                LineaOrdenMod lineaOrdenMod = new LineaOrdenMod();
                lineaOrdenMod.setLineaOrden(lineaOrden);
                lineaOrdenMod.setModificador(modificador);
                lineaOrdenMod.setNombreSnapshot(modificador.getNombre());
                lineaOrdenMod.setPrecioExtraSnapshot(modificador.getPrecioExtra());
                lineaOrdenModRepository.save(lineaOrdenMod);
            }

            items.add(new OrdenItemResponse(
                    producto.getIdProducto(),
                    producto.getNombre(),
                    itemRequest.cantidad(),
                    producto.getPrecio(),
                    subtotal,
                    modificadoresResponse
            ));
        }

        orden.setTotal(total);
        ordenRepository.save(orden);
        DomicilioOrdenResponse domicilioResponse = null;

        if ("DOMICILIO".equals(tipoOrden)) {
            domicilioResponse = guardarDomicilio(orden, request.domicilio());
        }

        return new OrdenResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getTipoOrden(),
                orden.getEstado(),
                orden.getTotal(),
                orden.getFecha(),
                domicilioResponse,
                items
        );
    }

    @Transactional
    public PagoResponse registrarPago(Long idOrden, PagoRequest request) {
        Orden orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if ("PAGADA".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("La orden ya fue pagada");
        }

        String metodo = request.metodo() == null ? "" : request.metodo().trim().toUpperCase();
        if (metodo.isBlank()) {
            throw new IllegalArgumentException("El metodo de pago es obligatorio");
        }

        BigDecimal efectivoRecibido = request.efectivoRecibido();
        BigDecimal cambio = BigDecimal.ZERO;

        if ("EFECTIVO".equals(metodo)) {
            if (efectivoRecibido == null) {
                throw new IllegalArgumentException("Debes indicar el efectivo recibido");
            }
            if (efectivoRecibido.compareTo(orden.getTotal()) < 0) {
                throw new IllegalArgumentException("El efectivo recibido es menor al total de la orden");
            }
            cambio = efectivoRecibido.subtract(orden.getTotal());
        }

        descontarInventario(orden);

        Pago pago = new Pago();
        pago.setOrden(orden);
        pago.setMetodo(metodo);
        pago.setMonto(orden.getTotal());
        pago.setEfectivoRecibido(efectivoRecibido);
        pago.setCambio(cambio);
        pago = pagoRepository.save(pago);

        MovimientoCaja movimientoCaja = new MovimientoCaja();
        movimientoCaja.setSesionCaja(orden.getSesionCaja());
        movimientoCaja.setTipo("INGRESO_VENTA");
        movimientoCaja.setMonto(orden.getTotal());
        movimientoCaja.setMotivo("Pago de orden " + orden.getIdOrden());
        movimientoCajaRepository.save(movimientoCaja);

        orden.setEstado("PAGADA");
        ordenRepository.save(orden);

        return new PagoResponse(
                pago.getIdPago(),
                orden.getIdOrden(),
                pago.getMetodo(),
                pago.getMonto(),
                pago.getEfectivoRecibido(),
                pago.getCambio(),
                orden.getEstado()
        );
    }

    @Transactional(readOnly = true)
    public TicketResponse obtenerTicket(Long idOrden) {
        Orden orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        Pago pago = pagoRepository.findTopByOrdenIdOrdenOrderByIdPagoDesc(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("La orden no tiene pago registrado"));

        List<OrdenItemResponse> items = orden.getLineasOrden().stream()
                .map(lineaOrden -> {
                    List<OrdenItemModResponse> modificadores = lineaOrden.getModificadores().stream()
                            .map(lineaOrdenMod -> new OrdenItemModResponse(
                                    lineaOrdenMod.getModificador().getIdModificador(),
                                    lineaOrdenMod.getNombreSnapshot(),
                                    lineaOrdenMod.getPrecioExtraSnapshot()
                            ))
                            .toList();

                    BigDecimal precioConExtras = lineaOrden.getPrecioUnitarioSnapshot().add(
                            lineaOrden.getModificadores().stream()
                                    .map(LineaOrdenMod::getPrecioExtraSnapshot)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    );

                    return new OrdenItemResponse(
                            lineaOrden.getProducto().getIdProducto(),
                            lineaOrden.getNombreSnapshot(),
                            lineaOrden.getCantidad(),
                            precioConExtras,
                            precioConExtras.multiply(BigDecimal.valueOf(lineaOrden.getCantidad())),
                            modificadores
                    );
                })
                .toList();

        return new TicketResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getTipoOrden(),
                orden.getSesionCaja().getUsuario().getNegocio().getNombre(),
                orden.getSesionCaja().getUsuario().getNombre(),
                orden.getFecha(),
                pago.getMetodo(),
                orden.getTotal(),
                pago.getEfectivoRecibido(),
                pago.getCambio(),
                mapDomicilio(orden.getOrdenDomicilio()),
                items
        );
    }

    @Transactional(readOnly = true)
    public List<HistorialVentaResponse> listarVentasPagadasPorUsuario(Long idUsuario) {
        return ordenRepository.findBySesionCajaUsuarioIdUsuarioAndEstadoOrderByFechaDesc(idUsuario, "PAGADA")
                .stream()
                .map(orden -> {
                    Pago pago = pagoRepository.findTopByOrdenIdOrdenOrderByIdPagoDesc(orden.getIdOrden()).orElse(null);
                    return new HistorialVentaResponse(
                            orden.getIdOrden(),
                            construirFolio(orden),
                            orden.getFecha(),
                            pago != null ? pago.getMetodo() : "N/A",
                            orden.getTotal(),
                            orden.getEstado()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenActivaResponse> listarOrdenesActivasPorUsuario(Long idUsuario) {
        return ordenRepository.findBySesionCajaUsuarioIdUsuarioAndEstadoInOrderByFechaDesc(idUsuario, List.of("PENDIENTE_PAGO"))
                .stream()
                .map(orden -> new OrdenActivaResponse(
                        orden.getIdOrden(),
                        construirFolio(orden),
                        orden.getFecha(),
                        orden.getTipoOrden(),
                        orden.getTotal(),
                        orden.getLineasOrden().stream().map(LineaOrden::getCantidad).reduce(0, Integer::sum),
                        orden.getEstado(),
                        mapDomicilio(orden.getOrdenDomicilio())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public VentasDiaResponse obtenerVentasDelDia(Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        List<Orden> ordenesHoy = ordenRepository.findBySesionCajaUsuarioIdUsuarioAndFechaOperacionOrderByFechaDesc(idUsuario, hoy);

        List<HistorialVentaResponse> ventasPagadas = ordenesHoy.stream()
                .filter(orden -> "PAGADA".equalsIgnoreCase(orden.getEstado()))
                .map(orden -> {
                    Pago pago = pagoRepository.findTopByOrdenIdOrdenOrderByIdPagoDesc(orden.getIdOrden()).orElse(null);
                    return new HistorialVentaResponse(
                            orden.getIdOrden(),
                            construirFolio(orden),
                            orden.getFecha(),
                            pago != null ? pago.getMetodo() : "N/A",
                            orden.getTotal(),
                            orden.getEstado()
                    );
                })
                .toList();

        BigDecimal totalVentas = ventasPagadas.stream()
                .map(HistorialVentaResponse::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int cantidadCanceladas = (int) ordenesHoy.stream()
                .filter(orden -> "CANCELADA".equalsIgnoreCase(orden.getEstado()))
                .count();
        int cantidadActivas = (int) ordenesHoy.stream()
                .filter(orden -> "PENDIENTE_PAGO".equalsIgnoreCase(orden.getEstado()))
                .count();

        return new VentasDiaResponse(
                hoy,
                totalVentas,
                ventasPagadas.size(),
                cantidadCanceladas,
                cantidadActivas,
                ventasPagadas
        );
    }

    @Transactional
    public CancelacionOrdenResponse cancelarOrden(Long idOrden, CancelarOrdenRequest request) {
        Orden orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if ("PAGADA".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("No puedes cancelar una orden ya pagada");
        }
        if ("CANCELADA".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("La orden ya fue cancelada");
        }

        String motivo = request == null || request.motivo() == null ? "" : request.motivo().trim();
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el motivo de cancelacion");
        }

        orden.setEstado("CANCELADA");
        orden.setMotivoCancelacion(motivo);
        orden.setFechaCancelacion(LocalDateTime.now());
        ordenRepository.save(orden);

        return new CancelacionOrdenResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getEstado(),
                orden.getMotivoCancelacion(),
                orden.getFechaCancelacion()
        );
    }

    @Transactional
    public OrdenActivaResponse actualizarEstadoDomicilio(Long idOrden, ActualizarEstadoDomicilioRequest request) {
        Orden orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!"DOMICILIO".equalsIgnoreCase(orden.getTipoOrden())) {
            throw new IllegalArgumentException("La orden no es de domicilio");
        }

        OrdenDomicilio ordenDomicilio = ordenDomicilioRepository.findByOrdenIdOrden(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Datos de domicilio no encontrados"));

        String estadoEntrega = normalizarEstadoDomicilio(request == null ? null : request.estadoEntrega());
        ordenDomicilio.setEstadoEntrega(estadoEntrega);
        ordenDomicilioRepository.save(ordenDomicilio);

        return mapOrdenActiva(orden, ordenDomicilio);
    }

    @Transactional
    public OrdenActivaResponse asignarRepartidor(Long idOrden, AsignarRepartidorRequest request) {
        Orden orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!"DOMICILIO".equalsIgnoreCase(orden.getTipoOrden())) {
            throw new IllegalArgumentException("La orden no es de domicilio");
        }

        OrdenDomicilio ordenDomicilio = ordenDomicilioRepository.findByOrdenIdOrden(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Datos de domicilio no encontrados"));

        if (request == null || request.idRepartidor() == null) {
            ordenDomicilio.setRepartidor(null);
            ordenDomicilio.setFechaAsignacion(null);
            ordenDomicilioRepository.save(ordenDomicilio);
            return mapOrdenActiva(orden, ordenDomicilio);
        }

        Usuario repartidor = usuarioRepository.findById(request.idRepartidor())
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado"));

        if (!orden.getSesionCaja().getUsuario().getNegocio().getIdNegocio().equals(repartidor.getNegocio().getIdNegocio())) {
            throw new IllegalArgumentException("El repartidor no pertenece al negocio");
        }
        if (!RolSistema.REPARTIDOR.name().equalsIgnoreCase(repartidor.getRol())) {
            throw new IllegalArgumentException("El usuario seleccionado no es repartidor");
        }
        if (!Boolean.TRUE.equals(repartidor.getActivo())) {
            throw new IllegalArgumentException("El repartidor está inactivo");
        }

        ordenDomicilio.setRepartidor(repartidor);
        ordenDomicilio.setFechaAsignacion(LocalDateTime.now());
        ordenDomicilioRepository.save(ordenDomicilio);

        return mapOrdenActiva(orden, ordenDomicilio);
    }

    @Transactional(readOnly = true)
    public List<OrdenActivaResponse> listarColaDomiciliosPorNegocio(Long idNegocio) {
        return ordenDomicilioRepository.findByOrdenSesionCajaUsuarioNegocioIdNegocioAndOrdenEstadoInOrderByOrdenFechaDesc(
                        idNegocio,
                        List.of("PENDIENTE_PAGO", "PAGADA")
                )
                .stream()
                .map(ordenDomicilio -> mapOrdenActiva(ordenDomicilio.getOrden(), ordenDomicilio))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenActivaResponse> listarColaDomiciliosPorRepartidor(Long idRepartidor) {
        return ordenDomicilioRepository.findByRepartidorIdUsuarioAndOrdenEstadoInOrderByOrdenFechaDesc(
                        idRepartidor,
                        List.of("PENDIENTE_PAGO", "PAGADA")
                )
                .stream()
                .map(ordenDomicilio -> mapOrdenActiva(ordenDomicilio.getOrden(), ordenDomicilio))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RepartidorOptionResponse> listarRepartidoresPorNegocio(Long idNegocio) {
        return usuarioRepository.findByNegocioIdNegocioAndRolIgnoreCaseAndActivoTrueOrderByNombreAsc(idNegocio, RolSistema.REPARTIDOR.name())
                .stream()
                .map(usuario -> new RepartidorOptionResponse(usuario.getIdUsuario(), usuario.getNombre()))
                .toList();
    }

    private void descontarInventario(Orden orden) {
        java.util.Map<Long, BigDecimal> requeridosPorInsumo = new java.util.HashMap<>();

        for (LineaOrden lineaOrden : orden.getLineasOrden()) {
            for (RecetaProducto recetaProducto : lineaOrden.getProducto().getReceta()) {
                BigDecimal requerido = recetaProducto.getCantidad()
                        .multiply(BigDecimal.valueOf(lineaOrden.getCantidad()));
                Long idInsumo = recetaProducto.getInsumo().getIdInsumo();
                requeridosPorInsumo.merge(idInsumo, requerido, BigDecimal::add);
            }
        }

        for (java.util.Map.Entry<Long, BigDecimal> entry : requeridosPorInsumo.entrySet()) {
            Insumo insumo = insumoRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado: " + entry.getKey()));

            if (insumo.getStockActual().compareTo(entry.getValue()) < 0) {
                throw new IllegalArgumentException("Stock insuficiente para " + insumo.getNombre());
            }
        }

        for (java.util.Map.Entry<Long, BigDecimal> entry : requeridosPorInsumo.entrySet()) {
            Insumo insumo = insumoRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado: " + entry.getKey()));
            insumo.setStockActual(insumo.getStockActual().subtract(entry.getValue()));
            insumoRepository.save(insumo);
        }
    }

    private String construirFolio(Orden orden) {
        return orden.getFechaOperacion().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + String.format("%03d", orden.getFolioDia());
    }

    private String normalizarTipoOrden(String tipoOrden) {
        String tipo = tipoOrden == null || tipoOrden.trim().isBlank()
                ? "MOSTRADOR"
                : tipoOrden.trim().toUpperCase();
        if (!"MOSTRADOR".equals(tipo) && !"DOMICILIO".equals(tipo)) {
            throw new IllegalArgumentException("Tipo de orden invalido");
        }
        return tipo;
    }

    private String normalizarEstadoDomicilio(String estadoEntrega) {
        String estado = estadoEntrega == null || estadoEntrega.trim().isBlank()
                ? "PENDIENTE"
                : estadoEntrega.trim().toUpperCase();
        if (!List.of("PENDIENTE", "EN_RUTA", "ENTREGADO").contains(estado)) {
            throw new IllegalArgumentException("Estado de domicilio invalido");
        }
        return estado;
    }

    private DomicilioOrdenResponse guardarDomicilio(Orden orden, DomicilioOrdenRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Debes capturar los datos del domicilio");
        }

        String nombreCliente = validarTexto(request.nombreCliente(), "El nombre del cliente es obligatorio");
        String direccion = validarTexto(request.direccion(), "La direccion del domicilio es obligatoria");
        String telefono = validarTexto(request.telefono(), "El telefono del cliente es obligatorio");

        OrdenDomicilio ordenDomicilio = new OrdenDomicilio();
        ordenDomicilio.setOrden(orden);
        ordenDomicilio.setNombreCliente(nombreCliente);
        ordenDomicilio.setDireccion(direccion);
        ordenDomicilio.setTelefono(telefono);
        ordenDomicilio.setEstadoEntrega("PENDIENTE");
        ordenDomicilioRepository.save(ordenDomicilio);

        return mapDomicilio(ordenDomicilio);
    }

    private DomicilioOrdenResponse mapDomicilio(OrdenDomicilio ordenDomicilio) {
        if (ordenDomicilio == null) {
            return null;
        }
        return new DomicilioOrdenResponse(
                ordenDomicilio.getNombreCliente(),
                ordenDomicilio.getDireccion(),
                ordenDomicilio.getTelefono(),
                ordenDomicilio.getEstadoEntrega(),
                ordenDomicilio.getRepartidor() != null ? ordenDomicilio.getRepartidor().getIdUsuario() : null,
                ordenDomicilio.getRepartidor() != null ? ordenDomicilio.getRepartidor().getNombre() : null
        );
    }

    private OrdenActivaResponse mapOrdenActiva(Orden orden, OrdenDomicilio ordenDomicilio) {
        return new OrdenActivaResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getFecha(),
                orden.getTipoOrden(),
                orden.getTotal(),
                orden.getLineasOrden().stream().map(LineaOrden::getCantidad).reduce(0, Integer::sum),
                orden.getEstado(),
                mapDomicilio(ordenDomicilio)
        );
    }

    private String validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
