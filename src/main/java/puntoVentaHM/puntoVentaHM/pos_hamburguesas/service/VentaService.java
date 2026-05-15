package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CancelacionOrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CancelarOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CotizacionOrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CrearOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarEstadoDomicilioRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarEstadoOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AgregarItemsOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AsignarRepartidorRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.DomicilioOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.DomicilioOrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.HistorialVentaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenActivaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenCocinaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemModRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemModResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemPromocionResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenSeguimientoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.PagoRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.PagoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.RepartidorOptionResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ReembolsoRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.TicketResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.VentasDiaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.LineaOrden;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.LineaOrdenMod;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.LineaOrdenPromocion;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Mesa;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.MovimientoCaja;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Orden;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.OrdenDomicilio;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Pago;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaModificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaProducto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.SesionCaja;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Insumo;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.LineaOrdenRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.LineaOrdenPromocionRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.LineaOrdenModRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.MesaRepository;
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

    private static final List<String> ESTADOS_ACTIVOS = List.of("POR_ACEPTAR", "PREPARANDO", "LISTO", "EN_RUTA");
    private static final List<String> ESTADOS_PEDIDO = List.of("POR_ACEPTAR", "PREPARANDO", "LISTO", "EN_RUTA", "ENTREGADO", "CANCELADO", "REEMBOLSADO");

    private final OrdenRepository ordenRepository;
    private final OrdenDomicilioRepository ordenDomicilioRepository;
    private final LineaOrdenRepository lineaOrdenRepository;
    private final LineaOrdenModRepository lineaOrdenModRepository;
    private final LineaOrdenPromocionRepository lineaOrdenPromocionRepository;
    private final PagoRepository pagoRepository;
    private final ProductoRepository productoRepository;
    private final ModificadorRepository modificadorRepository;
    private final MesaRepository mesaRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final InsumoRepository insumoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PromocionService promocionService;
    private final InventarioService inventarioService;
    private final PasswordEncoder passwordEncoder;

    public VentaService(
            OrdenRepository ordenRepository,
            OrdenDomicilioRepository ordenDomicilioRepository,
            LineaOrdenRepository lineaOrdenRepository,
            LineaOrdenModRepository lineaOrdenModRepository,
            LineaOrdenPromocionRepository lineaOrdenPromocionRepository,
            PagoRepository pagoRepository,
            ProductoRepository productoRepository,
            ModificadorRepository modificadorRepository,
            MesaRepository mesaRepository,
            SesionCajaRepository sesionCajaRepository,
            MovimientoCajaRepository movimientoCajaRepository,
            InsumoRepository insumoRepository,
            UsuarioRepository usuarioRepository,
            PromocionService promocionService,
            InventarioService inventarioService,
            PasswordEncoder passwordEncoder
    ) {
        this.ordenRepository = ordenRepository;
        this.ordenDomicilioRepository = ordenDomicilioRepository;
        this.lineaOrdenRepository = lineaOrdenRepository;
        this.lineaOrdenModRepository = lineaOrdenModRepository;
        this.lineaOrdenPromocionRepository = lineaOrdenPromocionRepository;
        this.pagoRepository = pagoRepository;
        this.productoRepository = productoRepository;
        this.modificadorRepository = modificadorRepository;
        this.mesaRepository = mesaRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.insumoRepository = insumoRepository;
        this.usuarioRepository = usuarioRepository;
        this.promocionService = promocionService;
        this.inventarioService = inventarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public CotizacionOrdenResponse cotizarOrden(CrearOrdenRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("La orden debe incluir al menos un producto");
        }
        SesionCaja sesionCaja = sesionCajaRepository
                .findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(request.idUsuario(), "ABIERTA")
                .orElseThrow(() -> new IllegalArgumentException("El usuario no tiene una sesion de caja abierta"));

        PromocionService.ResultadoPromocion resultado = promocionService.cotizar(
                sesionCaja.getUsuario().getNegocio().getIdNegocio(),
                request.items()
        );
        BigDecimal costoEnvio = "DOMICILIO".equals(normalizarTipoOrden(request.tipoOrden()))
                ? normalizarCostoEnvio(request.domicilio() == null ? null : request.domicilio().costoEnvio())
                : BigDecimal.ZERO;

        return new CotizacionOrdenResponse(
                resultado.subtotalLista(),
                costoEnvio,
                resultado.totalDescuento(),
                resultado.subtotalLista().subtract(resultado.totalDescuento()).add(costoEnvio),
                resultado.getLineas().stream().map(this::mapLineaCotizada).toList()
        );
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
        orden.setEstado("POR_ACEPTAR");
        orden.setTotal(BigDecimal.ZERO);
        orden.setMesa(resolverMesaParaOrden(request.idMesa(), tipoOrden, sesionCaja.getUsuario().getNegocio().getIdNegocio()));
        orden = ordenRepository.save(orden);

        PromocionService.ResultadoPromocion cotizacion = promocionService.cotizar(
                sesionCaja.getUsuario().getNegocio().getIdNegocio(),
                request.items()
        );
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalDescuentoPromocional = cotizacion.totalDescuento();
        List<OrdenItemResponse> items = new ArrayList<>();
        java.util.Map<Long, BigDecimal> requeridosPorInsumo = new java.util.HashMap<>();
        Map<String, PromocionService.LineaCotizacion> cotizacionPorProducto = cotizacion.getLineas().stream()
                .collect(java.util.stream.Collectors.toMap(PromocionService.LineaCotizacion::getClave, linea -> linea, (a, b) -> a));

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

            String claveCotizacion = producto.getIdProducto() + ":" + modificadoresRequest.stream()
                    .filter(Objects::nonNull)
                    .map(OrdenItemModRequest::idModificador)
                    .filter(Objects::nonNull)
                    .sorted()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","));
            PromocionService.LineaCotizacion lineaCotizada = cotizacionPorProducto.get(claveCotizacion);
            BigDecimal precioListaUnitario = producto.getPrecio().add(totalModificadores);
            BigDecimal descuentoPromocional = lineaCotizada == null ? BigDecimal.ZERO : lineaCotizada.descuentoTotal();
            BigDecimal subtotal = lineaCotizada == null
                    ? precioListaUnitario.multiply(BigDecimal.valueOf(itemRequest.cantidad()))
                    : lineaCotizada.subtotalFinal();
            total = total.add(subtotal);

            LineaOrden lineaOrden = new LineaOrden();
            lineaOrden.setOrden(orden);
            lineaOrden.setProducto(producto);
            lineaOrden.setCantidad(itemRequest.cantidad());
            lineaOrden.setNombreSnapshot(producto.getNombre());
            lineaOrden.setPrecioUnitarioSnapshot(precioListaUnitario);
            lineaOrden.setDescuentoPromocionalSnapshot(descuentoPromocional);
            lineaOrden = lineaOrdenRepository.save(lineaOrden);

            for (RecetaProducto recetaProducto : producto.getReceta()) {
                BigDecimal requerido = recetaProducto.getCantidad()
                        .multiply(BigDecimal.valueOf(itemRequest.cantidad()));
                requeridosPorInsumo.merge(recetaProducto.getInsumo().getIdInsumo(), requerido, BigDecimal::add);
            }

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

                for (RecetaModificador recetaModificador : modificador.getReceta()) {
                    BigDecimal requerido = recetaModificador.getCantidad()
                            .multiply(BigDecimal.valueOf(itemRequest.cantidad()));
                    requeridosPorInsumo.merge(recetaModificador.getInsumo().getIdInsumo(), requerido, BigDecimal::add);
                }
            }

            if (lineaCotizada != null) {
                for (OrdenItemPromocionResponse promocion : lineaCotizada.getPromociones()) {
                    LineaOrdenPromocion snapshotPromocion = new LineaOrdenPromocion();
                    snapshotPromocion.setLineaOrden(lineaOrden);
                    snapshotPromocion.setPromocionNombre(promocion.nombre());
                    snapshotPromocion.setTipoRegla(promocion.tipoRegla());
                    snapshotPromocion.setDescuento(promocion.descuento());
                    snapshotPromocion.setDescripcion(promocion.descripcion());
                    lineaOrdenPromocionRepository.save(snapshotPromocion);
                }
            }

            items.add(new OrdenItemResponse(
                    producto.getIdProducto(),
                    producto.getNombre(),
                    itemRequest.cantidad(),
                    precioListaUnitario,
                    precioListaUnitario.subtract(itemRequest.cantidad() > 0 ? descuentoPromocional.divide(BigDecimal.valueOf(itemRequest.cantidad()), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO),
                    descuentoPromocional,
                    subtotal,
                    modificadoresResponse,
                    lineaCotizada == null ? List.of() : lineaCotizada.getPromociones()
            ));
        }

        DomicilioOrdenResponse domicilioResponse = null;

        if ("DOMICILIO".equals(tipoOrden)) {
            domicilioResponse = guardarDomicilio(orden, request.domicilio());
            total = total.add(domicilioResponse.costoEnvio());
        }

        orden.setTotal(total);
        ordenRepository.save(orden);
        ocuparMesaSiCorresponde(orden);
        aplicarDescuentoInventario(requeridosPorInsumo, "Orden " + construirFolio(orden));

        return new OrdenResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getTipoOrden(),
                orden.getEstado(),
                false,
                totalDescuentoPromocional,
                orden.getTotal(),
                orden.getFecha(),
                domicilioResponse,
                items
        );
    }

    @Transactional
    public OrdenResponse agregarItemsAOrden(Long idOrden, AgregarItemsOrdenRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Debes incluir al menos un producto");
        }

        Orden orden = ordenRepository.findByIdConLock(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        List<String> estadosPermitidos = List.of("POR_ACEPTAR", "PREPARANDO", "LISTO");
        if (!estadosPermitidos.contains(orden.getEstado())) {
            throw new IllegalArgumentException("Solo puedes agregar productos a órdenes en preparación o listas");
        }
        if (tienePagoRegistrado(idOrden)) {
            throw new IllegalArgumentException("No puedes agregar productos a una orden ya pagada");
        }

        BigDecimal totalAgregado = BigDecimal.ZERO;
        java.util.Map<Long, BigDecimal> requeridosPorInsumo = new java.util.HashMap<>();

        for (OrdenItemRequest itemRequest : request.items()) {
            if (itemRequest.cantidad() == null || itemRequest.cantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a cero");
            }

            Producto producto = productoRepository.findById(itemRequest.idProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + itemRequest.idProducto()));

            BigDecimal totalModificadores = BigDecimal.ZERO;
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
            }

            BigDecimal precioListaUnitario = producto.getPrecio().add(totalModificadores);
            BigDecimal subtotal = precioListaUnitario.multiply(BigDecimal.valueOf(itemRequest.cantidad()));
            totalAgregado = totalAgregado.add(subtotal);

            LineaOrden lineaOrden = new LineaOrden();
            lineaOrden.setOrden(orden);
            lineaOrden.setProducto(producto);
            lineaOrden.setCantidad(itemRequest.cantidad());
            lineaOrden.setNombreSnapshot(producto.getNombre());
            lineaOrden.setPrecioUnitarioSnapshot(precioListaUnitario);
            lineaOrden.setDescuentoPromocionalSnapshot(BigDecimal.ZERO);
            lineaOrden = lineaOrdenRepository.save(lineaOrden);

            for (RecetaProducto recetaProducto : producto.getReceta()) {
                BigDecimal requerido = recetaProducto.getCantidad()
                        .multiply(BigDecimal.valueOf(itemRequest.cantidad()));
                requeridosPorInsumo.merge(recetaProducto.getInsumo().getIdInsumo(), requerido, BigDecimal::add);
            }

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

                for (RecetaModificador recetaModificador : modificador.getReceta()) {
                    BigDecimal requerido = recetaModificador.getCantidad()
                            .multiply(BigDecimal.valueOf(itemRequest.cantidad()));
                    requeridosPorInsumo.merge(recetaModificador.getInsumo().getIdInsumo(), requerido, BigDecimal::add);
                }
            }
        }

        aplicarDescuentoInventario(requeridosPorInsumo, "Agregado a orden " + construirFolio(orden));
        orden.setTotal(orden.getTotal().add(totalAgregado));
        ordenRepository.save(orden);

        return new OrdenResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getTipoOrden(),
                orden.getEstado(),
                tienePagoRegistrado(orden.getIdOrden()),
                BigDecimal.ZERO,
                orden.getTotal(),
                orden.getFecha(),
                mapDomicilio(orden.getOrdenDomicilio()),
                List.of()
        );
    }

    @Transactional
    public PagoResponse registrarPago(Long idOrden, PagoRequest request) {
        Orden orden = ordenRepository.findByIdConLock(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if ("CANCELADO".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("No puedes cobrar una orden cancelada");
        }
        if ("REEMBOLSADO".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("No puedes cobrar una orden reembolsada");
        }
        if (tienePagoRegistrado(idOrden)) {
            throw new IllegalArgumentException("La orden ya fue pagada");
        }
        if (!ESTADOS_PEDIDO.contains(orden.getEstado().toUpperCase())) {
            throw new IllegalArgumentException("La orden no tiene un estado válido para cobro");
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
            if (cambio.compareTo(calcularEfectivoDisponible(orden.getSesionCaja())) > 0) {
                throw new IllegalArgumentException("La caja no tiene suficiente efectivo disponible para dar cambio");
            }
        }

        Pago pago = new Pago();
        pago.setOrden(orden);
        pago.setMetodo(metodo);
        pago.setMonto(orden.getTotal());
        pago.setEfectivoRecibido(efectivoRecibido);
        pago.setCambio(cambio);
        pago = pagoRepository.save(pago);

        MovimientoCaja movimientoCaja = new MovimientoCaja();
        movimientoCaja.setSesionCaja(orden.getSesionCaja());
        movimientoCaja.setTipo(switch (metodo) {
            case "EFECTIVO" -> "INGRESO_VENTA_EFECTIVO";
            case "TRANSFERENCIA" -> "INGRESO_VENTA_TRANSFERENCIA";
            default -> "INGRESO_VENTA_TARJETA";
        });
        movimientoCaja.setMonto(orden.getTotal());
        movimientoCaja.setFecha(LocalDateTime.now());
        movimientoCaja.setMotivo("Pago de orden " + orden.getIdOrden());
        movimientoCajaRepository.save(movimientoCaja);

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

    @Transactional
    public OrdenSeguimientoResponse reembolsarOrden(Long idOrden, ReembolsoRequest request, Long idUsuarioOperacion) {
        Orden orden = ordenRepository.findByIdConLock(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if ("CANCELADO".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("No puedes reembolsar una orden cancelada");
        }
        if ("REEMBOLSADO".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("La orden ya fue reembolsada");
        }

        Pago pago = pagoRepository.findTopByOrdenIdOrdenOrderByIdPagoDesc(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Solo puedes reembolsar una orden pagada"));

        String motivo = request == null || request.motivo() == null ? "" : request.motivo().trim();
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el motivo del reembolso");
        }

        String pinGerente = request == null || request.pinGerente() == null ? "" : request.pinGerente().trim();
        if (pinGerente.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el PIN de autorizacion");
        }

        Long idNegocio = orden.getSesionCaja().getUsuario().getNegocio().getIdNegocio();
        Usuario autorizador = usuarioRepository.findByNegocioIdNegocioAndRolInAndActivoTrue(
                        idNegocio,
                        List.of(RolSistema.ADMIN.name(), RolSistema.GERENTE.name())
                ).stream()
                .filter(u -> passwordEncoder.matches(pinGerente, u.getPinAcceso()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("PIN de autorizacion invalido"));

        SesionCaja sesionActual = sesionCajaRepository
                .findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(idUsuarioOperacion, "ABIERTA")
                .orElseThrow(() -> new IllegalArgumentException("No hay una sesion de caja abierta para registrar el reembolso"));

        if (!Objects.equals(sesionActual.getUsuario().getNegocio().getIdNegocio(), idNegocio)) {
            throw new IllegalArgumentException("La sesion de caja no pertenece al negocio de la orden");
        }

        orden.setEstado("REEMBOLSADO");
        orden.setMotivoCancelacion(motivo);
        orden.setFechaCancelacion(LocalDateTime.now());
        ordenRepository.save(orden);
        liberarMesaSiCorresponde(orden);

        MovimientoCaja movimientoCaja = new MovimientoCaja();
        movimientoCaja.setSesionCaja(sesionActual);
        movimientoCaja.setTipo("EGRESO");
        movimientoCaja.setMonto(pago.getMonto());
        movimientoCaja.setFecha(LocalDateTime.now());
        movimientoCaja.setMotivo("Reembolso orden " + construirFolio(orden) + " autorizado por " + autorizador.getNombre() + ": " + motivo);
        movimientoCajaRepository.save(movimientoCaja);

        return mapOrdenSeguimiento(orden);
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

                    BigDecimal precioConExtras = lineaOrden.getPrecioUnitarioSnapshot();
                    BigDecimal descuentoPromocional = lineaOrden.getDescuentoPromocionalSnapshot() == null
                            ? BigDecimal.ZERO
                            : lineaOrden.getDescuentoPromocionalSnapshot();
                    BigDecimal subtotal = precioConExtras
                            .multiply(BigDecimal.valueOf(lineaOrden.getCantidad()))
                            .subtract(descuentoPromocional);

                    return new OrdenItemResponse(
                            lineaOrden.getProducto().getIdProducto(),
                            lineaOrden.getNombreSnapshot(),
                            lineaOrden.getCantidad(),
                            lineaOrden.getPrecioUnitarioSnapshot(),
                            precioConExtras,
                            descuentoPromocional,
                            subtotal,
                            modificadores,
                            lineaOrden.getPromociones().stream()
                                    .map(promocion -> new OrdenItemPromocionResponse(
                                            promocion.getPromocionNombre(),
                                            promocion.getTipoRegla(),
                                            promocion.getDescuento(),
                                            promocion.getDescripcion()
                                    ))
                                    .toList()
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
                orden.getLineasOrden().stream()
                        .map(linea -> linea.getDescuentoPromocionalSnapshot() == null ? BigDecimal.ZERO : linea.getDescuentoPromocionalSnapshot())
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                orden.getTotal(),
                pago.getEfectivoRecibido(),
                pago.getCambio(),
                mapDomicilio(orden.getOrdenDomicilio()),
                items
        );
    }

    @Transactional(readOnly = true)
    public List<HistorialVentaResponse> listarVentasPagadasPorUsuario(Long idUsuario) {
        return ordenRepository.findBySesionCajaUsuarioIdUsuarioOrderByFechaDesc(idUsuario)
                .stream()
                .filter(orden -> Boolean.TRUE.equals(tienePagoRegistrado(orden.getIdOrden())))
                .map(this::mapHistorialVenta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialVentaResponse> listarHistorialVentas(Long idNegocio, LocalDate fechaInicio, LocalDate fechaFin, String estado, String metodoPago) {
        List<Orden> ordenes = obtenerOrdenesParaHistorial(idNegocio, fechaInicio, fechaFin);
        String estadoNormalizado = normalizarFiltro(estado);
        String metodoNormalizado = normalizarFiltro(metodoPago);

        return ordenes.stream()
                .map(this::mapHistorialVenta)
                .filter(item -> filtrarPorEstado(item, estadoNormalizado))
                .filter(item -> filtrarPorMetodo(item, metodoNormalizado))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenActivaResponse> listarOrdenesActivasPorUsuario(Long idUsuario) {
        return ordenRepository.findBySesionCajaUsuarioIdUsuarioAndEstadoInOrderByFechaDesc(idUsuario, ESTADOS_ACTIVOS)
                .stream()
                .map(orden -> mapOrdenActiva(orden, orden.getOrdenDomicilio()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenCocinaResponse> listarColaCocinaPorNegocio(Long idNegocio) {
        List<String> estadosCocina = List.of("POR_ACEPTAR", "PREPARANDO");
        return ordenRepository.findBySesionCajaUsuarioNegocioIdNegocioOrderByFechaDesc(idNegocio)
                .stream()
                .filter(orden -> estadosCocina.contains(orden.getEstado()))
                .map(this::mapOrdenCocina)
                .toList();
    }

    private OrdenCocinaResponse mapOrdenCocina(Orden orden) {
        List<OrdenCocinaResponse.OrdenCocinaItem> items = orden.getLineasOrden().stream()
                .map(linea -> new OrdenCocinaResponse.OrdenCocinaItem(
                        linea.getNombreSnapshot(),
                        linea.getCantidad(),
                        linea.getModificadores().stream()
                                .map(LineaOrdenMod::getNombreSnapshot)
                                .toList()
                ))
                .toList();

        return new OrdenCocinaResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getFecha(),
                orden.getTipoOrden(),
                orden.getLineasOrden().stream().map(LineaOrden::getCantidad).reduce(0, Integer::sum),
                orden.getEstado(),
                orden.getMesa() != null ? orden.getMesa().getNumero() : null,
                orden.getOrdenDomicilio() != null ? orden.getOrdenDomicilio().getNombreCliente() : null,
                items
        );
    }

    @Transactional(readOnly = true)
    public List<OrdenSeguimientoResponse> listarOrdenesPorNegocio(Long idNegocio, String estado, String tipoOrden, Boolean pagada) {
        String estadoNormalizado = normalizarFiltro(estado);
        String tipoNormalizado = normalizarFiltro(tipoOrden);

        return ordenRepository.findBySesionCajaUsuarioNegocioIdNegocioOrderByFechaDesc(idNegocio)
                .stream()
                .map(this::mapOrdenSeguimiento)
                .filter(item -> estadoNormalizado == null || estadoNormalizado.equalsIgnoreCase(item.estado()))
                .filter(item -> tipoNormalizado == null || tipoNormalizado.equalsIgnoreCase(item.tipoOrden()))
                .filter(item -> pagada == null || pagada.equals(item.pagada()))
                .toList();
    }

    @Transactional(readOnly = true)
    public VentasDiaResponse obtenerVentasDelDia(Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        List<Orden> ordenesHoy = ordenRepository.findBySesionCajaUsuarioIdUsuarioAndFechaOperacionOrderByFechaDesc(idUsuario, hoy);

        List<HistorialVentaResponse> ventasPagadas = ordenesHoy.stream()
                .filter(orden -> tienePagoRegistrado(orden.getIdOrden()))
                .map(this::mapHistorialVenta)
                .toList();

        BigDecimal totalVentas = ventasPagadas.stream()
                .map(HistorialVentaResponse::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int cantidadCanceladas = (int) ordenesHoy.stream()
                .filter(orden -> "CANCELADO".equalsIgnoreCase(orden.getEstado()))
                .count();
        int cantidadActivas = (int) ordenesHoy.stream()
                .filter(orden -> ESTADOS_ACTIVOS.contains(orden.getEstado()))
                .count();

        return new VentasDiaResponse(
                hoy,
                totalVentas,
                ordenesHoy.size(),
                ventasPagadas.size(),
                cantidadCanceladas,
                cantidadActivas,
                ventasPagadas
        );
    }

    @Transactional
    public CancelacionOrdenResponse cancelarOrden(Long idOrden, CancelarOrdenRequest request) {
        Orden orden = ordenRepository.findByIdConLock(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if ("CANCELADO".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("La orden ya fue cancelada");
        }
        if (tienePagoRegistrado(idOrden)) {
            throw new IllegalArgumentException("No puedes cancelar una orden ya pagada");
        }
        if (!"POR_ACEPTAR".equalsIgnoreCase(orden.getEstado())) {
            throw new IllegalArgumentException("Solo puedes cancelar una orden antes de que la cocina la acepte");
        }

        String motivo = request == null || request.motivo() == null ? "" : request.motivo().trim();
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el motivo de cancelacion");
        }

        revertirInventarioPorCancelacion(orden);

        orden.setEstado("CANCELADO");
        orden.setMotivoCancelacion(motivo);
        orden.setFechaCancelacion(LocalDateTime.now());
        ordenRepository.save(orden);
        liberarMesaSiCorresponde(orden);

        return new CancelacionOrdenResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getEstado(),
                orden.getMotivoCancelacion(),
                orden.getFechaCancelacion()
        );
    }

    @Transactional
    public OrdenActivaResponse actualizarEstadoOrden(Long idOrden, ActualizarEstadoOrdenRequest request) {
        Orden orden = ordenRepository.findByIdConLock(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        String nuevoEstado = normalizarEstadoPedido(request == null ? null : request.estado());
        validarCambioEstado(orden, nuevoEstado);

        orden.setEstado(nuevoEstado);
        ordenRepository.save(orden);
        liberarMesaSiCorrespondeSiAplicaPorEstado(orden, nuevoEstado);

        OrdenDomicilio ordenDomicilio = orden.getOrdenDomicilio();
        if (ordenDomicilio != null) {
            ordenDomicilio.setEstadoEntrega(normalizarEstadoEntregaDesdePedido(orden, nuevoEstado, ordenDomicilio.getEstadoEntrega()));
            ordenDomicilioRepository.save(ordenDomicilio);
        }

        return mapOrdenActiva(orden, ordenDomicilio);
    }

    @Transactional
    public OrdenActivaResponse actualizarEstadoDomicilio(Long idOrden, ActualizarEstadoDomicilioRequest request) {
        Orden orden = ordenRepository.findByIdConLock(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!"DOMICILIO".equalsIgnoreCase(orden.getTipoOrden())) {
            throw new IllegalArgumentException("La orden no es de domicilio");
        }

        OrdenDomicilio ordenDomicilio = ordenDomicilioRepository.findByOrdenIdOrden(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Datos de domicilio no encontrados"));

        String estadoEntrega = normalizarEstadoDomicilio(request == null ? null : request.estadoEntrega());
        validarCambioEstado(orden, estadoEntrega);

        orden.setEstado(estadoEntrega);
        ordenRepository.save(orden);
        ordenDomicilio.setEstadoEntrega(estadoEntrega);
        ordenDomicilioRepository.save(ordenDomicilio);

        return mapOrdenActiva(orden, ordenDomicilio);
    }

    @Transactional
    public OrdenActivaResponse asignarRepartidor(Long idOrden, AsignarRepartidorRequest request) {
        Orden orden = ordenRepository.findByIdConLock(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!"DOMICILIO".equalsIgnoreCase(orden.getTipoOrden())) {
            throw new IllegalArgumentException("La orden no es de domicilio");
        }

        OrdenDomicilio ordenDomicilio = ordenDomicilioRepository.findByOrdenIdOrden(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Datos de domicilio no encontrados"));

        if (request == null || request.idRepartidor() == null) {
            ordenDomicilio.setRepartidor(null);
            ordenDomicilio.setFechaAsignacion(null);
            if ("EN_RUTA".equalsIgnoreCase(orden.getEstado())) {
                orden.setEstado("LISTO");
                ordenRepository.save(orden);
                ordenDomicilio.setEstadoEntrega("LISTO");
            }
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
                        ESTADOS_ACTIVOS
                )
                .stream()
                .map(ordenDomicilio -> mapOrdenActiva(ordenDomicilio.getOrden(), ordenDomicilio))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenActivaResponse> listarColaDomiciliosPorRepartidor(Long idRepartidor) {
        return ordenDomicilioRepository.findByRepartidorIdUsuarioAndOrdenEstadoInOrderByOrdenFechaDesc(
                        idRepartidor,
                        ESTADOS_ACTIVOS
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

    private void aplicarDescuentoInventario(java.util.Map<Long, BigDecimal> requeridosPorInsumo, String referencia) {
        // Iterar por id ascendente para mantener orden de lock estable entre transacciones concurrentes.
        java.util.List<Long> ids = requeridosPorInsumo.keySet().stream().sorted().toList();

        for (Long idInsumo : ids) {
            BigDecimal requerido = requeridosPorInsumo.get(idInsumo);
            Insumo insumo = insumoRepository.findByIdConLock(idInsumo)
                    .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado: " + idInsumo));
            if (insumo.getStockActual().compareTo(requerido) < 0) {
                throw new IllegalArgumentException("Stock insuficiente para " + insumo.getNombre());
            }
            inventarioService.registrarSalidaVenta(insumo, requerido, referencia);
        }
    }

    private void revertirInventarioPorCancelacion(Orden orden) {
        java.util.Map<Long, BigDecimal> requeridosPorInsumo = new java.util.HashMap<>();

        for (LineaOrden lineaOrden : orden.getLineasOrden()) {
            for (RecetaProducto recetaProducto : lineaOrden.getProducto().getReceta()) {
                BigDecimal requerido = recetaProducto.getCantidad()
                        .multiply(BigDecimal.valueOf(lineaOrden.getCantidad()));
                requeridosPorInsumo.merge(recetaProducto.getInsumo().getIdInsumo(), requerido, BigDecimal::add);
            }
            for (LineaOrdenMod lineaOrdenMod : lineaOrden.getModificadores()) {
                for (RecetaModificador recetaModificador : lineaOrdenMod.getModificador().getReceta()) {
                    BigDecimal requerido = recetaModificador.getCantidad()
                            .multiply(BigDecimal.valueOf(lineaOrden.getCantidad()));
                    requeridosPorInsumo.merge(recetaModificador.getInsumo().getIdInsumo(), requerido, BigDecimal::add);
                }
            }
        }

        String referencia = "Cancelación orden " + construirFolio(orden);
        java.util.List<Long> ids = requeridosPorInsumo.keySet().stream().sorted().toList();
        for (Long idInsumo : ids) {
            Insumo insumo = insumoRepository.findByIdConLock(idInsumo)
                    .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado: " + idInsumo));
            inventarioService.registrarReversionCancelacion(insumo, requeridosPorInsumo.get(idInsumo), referencia);
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
                ? "POR_ACEPTAR"
                : estadoEntrega.trim().toUpperCase();
        if (!List.of("POR_ACEPTAR", "PREPARANDO", "LISTO", "EN_RUTA", "ENTREGADO").contains(estado)) {
            throw new IllegalArgumentException("Estado de domicilio invalido");
        }
        return estado;
    }

    private String normalizarEstadoPedido(String estadoPedido) {
        String estado = estadoPedido == null || estadoPedido.trim().isBlank()
                ? "POR_ACEPTAR"
                : estadoPedido.trim().toUpperCase();
        if (!ESTADOS_PEDIDO.contains(estado)) {
            throw new IllegalArgumentException("Estado de pedido invalido");
        }
        return estado;
    }

    private DomicilioOrdenResponse guardarDomicilio(Orden orden, DomicilioOrdenRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Debes capturar los datos del domicilio");
        }

        String nombreCliente = validarTexto(request.nombreCliente(), "El nombre del cliente es obligatorio");
        String direccion = validarTexto(request.direccion(), "La direccion del domicilio es obligatoria");
        String telefono = validarTelefono(request.telefono());
        BigDecimal costoEnvio = normalizarCostoEnvio(request.costoEnvio());

        OrdenDomicilio ordenDomicilio = new OrdenDomicilio();
        ordenDomicilio.setOrden(orden);
        ordenDomicilio.setNombreCliente(nombreCliente);
        ordenDomicilio.setDireccion(direccion);
        ordenDomicilio.setTelefono(telefono);
        ordenDomicilio.setCostoEnvio(costoEnvio);
        ordenDomicilio.setEstadoEntrega("POR_ACEPTAR");
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
                ordenDomicilio.getCostoEnvio(),
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
                tienePagoRegistrado(orden.getIdOrden()),
                mapDomicilio(ordenDomicilio)
        );
    }

    private OrdenSeguimientoResponse mapOrdenSeguimiento(Orden orden) {
        return new OrdenSeguimientoResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getFecha(),
                orden.getSesionCaja().getUsuario().getNombre(),
                orden.getTipoOrden(),
                orden.getTotal(),
                orden.getLineasOrden().stream().map(LineaOrden::getCantidad).reduce(0, Integer::sum),
                orden.getEstado(),
                tienePagoRegistrado(orden.getIdOrden()),
                orden.getMotivoCancelacion(),
                orden.getMesa() != null ? orden.getMesa().getNumero() : null,
                mapDomicilio(orden.getOrdenDomicilio())
        );
    }

    private OrdenItemResponse mapLineaCotizada(PromocionService.LineaCotizacion lineaCotizada) {
        List<OrdenItemModResponse> modificadores = lineaCotizada.getModificadores().stream()
                .map(modificador -> new OrdenItemModResponse(
                        modificador.getIdModificador(),
                        modificador.getNombre(),
                        modificador.getPrecioExtra()
                ))
                .toList();

        BigDecimal precioListaUnitario = lineaCotizada.precioListaUnitario();
        BigDecimal descuentoUnitario = lineaCotizada.getCantidad() > 0
                ? lineaCotizada.descuentoTotal().divide(BigDecimal.valueOf(lineaCotizada.getCantidad()), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new OrdenItemResponse(
                lineaCotizada.getProducto().getIdProducto(),
                lineaCotizada.getProducto().getNombre(),
                lineaCotizada.getCantidad(),
                precioListaUnitario,
                precioListaUnitario.subtract(descuentoUnitario),
                lineaCotizada.descuentoTotal(),
                lineaCotizada.subtotalFinal(),
                modificadores,
                lineaCotizada.getPromociones()
        );
    }

    private BigDecimal normalizarCostoEnvio(BigDecimal costoEnvio) {
        if (costoEnvio == null) {
            return BigDecimal.ZERO;
        }
        if (costoEnvio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo de envio no puede ser negativo");
        }
        return costoEnvio;
    }

    private HistorialVentaResponse mapHistorialVenta(Orden orden) {
        Pago pago = pagoRepository.findTopByOrdenIdOrdenOrderByIdPagoDesc(orden.getIdOrden()).orElse(null);
        return new HistorialVentaResponse(
                orden.getIdOrden(),
                construirFolio(orden),
                orden.getFecha(),
                orden.getSesionCaja().getUsuario().getNombre(),
                orden.getTipoOrden(),
                pago != null ? pago.getMetodo() : "SIN_PAGO",
                orden.getTotal(),
                orden.getEstado(),
                pago != null,
                orden.getMotivoCancelacion()
        );
    }

    private List<Orden> obtenerOrdenesParaHistorial(Long idNegocio, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null && fechaFin == null) {
            return ordenRepository.findBySesionCajaUsuarioNegocioIdNegocioOrderByFechaDesc(idNegocio);
        }

        LocalDate inicio = fechaInicio == null ? LocalDate.of(2000, 1, 1) : fechaInicio;
        LocalDate fin = fechaFin == null ? LocalDate.now() : fechaFin;
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El rango de fechas del historial es inválido");
        }
        return ordenRepository.findBySesionCajaUsuarioNegocioIdNegocioAndFechaBetweenOrderByFechaDesc(
                idNegocio,
                inicio.atStartOfDay(),
                fin.atTime(LocalTime.MAX)
        );
    }

    private String normalizarFiltro(String valor) {
        if (valor == null || valor.trim().isBlank() || "TODOS".equalsIgnoreCase(valor.trim())) {
            return null;
        }
        return valor.trim().toUpperCase();
    }

    private boolean filtrarPorEstado(HistorialVentaResponse item, String estado) {
        return estado == null || estado.equalsIgnoreCase(item.estado());
    }

    private boolean filtrarPorMetodo(HistorialVentaResponse item, String metodoPago) {
        return metodoPago == null || metodoPago.equalsIgnoreCase(item.metodoPago());
    }

    private Mesa resolverMesaParaOrden(Long idMesa, String tipoOrden, Long idNegocio) {
        if (!"MOSTRADOR".equalsIgnoreCase(tipoOrden)) {
            return null;
        }
        if (idMesa == null) {
            return null;
        }

        Mesa mesa = mesaRepository.findById(idMesa)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));
        if (!Objects.equals(mesa.getNegocio().getIdNegocio(), idNegocio)) {
            throw new IllegalArgumentException("La mesa no pertenece al negocio");
        }
        if (!"LIBRE".equalsIgnoreCase(mesa.getEstado())) {
            throw new IllegalArgumentException("La mesa seleccionada no está disponible");
        }
        return mesa;
    }

    private void ocuparMesaSiCorresponde(Orden orden) {
        if (orden.getMesa() == null) {
            return;
        }
        Mesa mesa = orden.getMesa();
        mesa.setEstado("OCUPADA");
        mesa.setReferenciaOrden(construirFolio(orden));
        if (mesa.getMeseroAsignado() == null || mesa.getMeseroAsignado().isBlank()) {
            mesa.setMeseroAsignado(orden.getSesionCaja().getUsuario().getNombre());
        }
        mesaRepository.save(mesa);
    }

    private void liberarMesaSiCorresponde(Orden orden) {
        if (orden.getMesa() == null) {
            return;
        }
        Mesa mesa = orden.getMesa();
        mesa.setEstado("LIBRE");
        mesa.setReferenciaOrden(null);
        mesa.setMeseroAsignado(null);
        mesaRepository.save(mesa);
    }

    private void liberarMesaSiCorrespondeSiAplicaPorEstado(Orden orden, String estado) {
        if (orden.getMesa() == null) {
            return;
        }
        if ("ENTREGADO".equalsIgnoreCase(estado) || "CANCELADO".equalsIgnoreCase(estado)) {
            liberarMesaSiCorresponde(orden);
        }
    }

    private BigDecimal calcularEfectivoDisponible(SesionCaja sesionCaja) {
        List<MovimientoCaja> movimientos = movimientoCajaRepository.findBySesionCajaIdSesionCajaOrderByFechaDesc(sesionCaja.getIdSesionCaja());
        BigDecimal ventasEfectivo = movimientos.stream()
                .filter(movimiento -> "INGRESO_VENTA_EFECTIVO".equalsIgnoreCase(movimiento.getTipo())
                        || "INGRESO_VENTA".equalsIgnoreCase(movimiento.getTipo()))
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ingresosManuales = movimientos.stream()
                .filter(movimiento -> "INGRESO_MANUAL".equalsIgnoreCase(movimiento.getTipo()))
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retiros = movimientos.stream()
                .filter(movimiento -> "RETIRO".equalsIgnoreCase(movimiento.getTipo())
                        || "RETIRO_SEGURIDAD".equalsIgnoreCase(movimiento.getTipo())
                        || "EGRESO".equalsIgnoreCase(movimiento.getTipo()))
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sesionCaja.getFondoInicial()
                .add(ventasEfectivo)
                .add(ingresosManuales)
                .subtract(retiros);
    }

    private String validarTelefono(String valor) {
        String telefono = validarTexto(valor, "El telefono del cliente es obligatorio");
        String soloDigitos = telefono.replaceAll("\\D", "");
        if (soloDigitos.length() < 10 || soloDigitos.length() > 15) {
            throw new IllegalArgumentException("El telefono debe tener entre 10 y 15 digitos");
        }
        return soloDigitos;
    }

    private void validarCambioEstado(Orden orden, String nuevoEstado) {
        String estadoActual = normalizarEstadoPedido(orden.getEstado());
        if ("CANCELADO".equals(estadoActual)) {
            throw new IllegalArgumentException("La orden ya fue cancelada");
        }
        if ("REEMBOLSADO".equals(estadoActual)) {
            throw new IllegalArgumentException("La orden ya fue reembolsada");
        }
        if ("CANCELADO".equals(nuevoEstado)) {
            throw new IllegalArgumentException("Usa la cancelacion de pedido para marcarlo como cancelado");
        }
        if ("ENTREGADO".equals(estadoActual) && !"ENTREGADO".equals(nuevoEstado)) {
            throw new IllegalArgumentException("La orden ya fue entregada");
        }
        if ("MOSTRADOR".equalsIgnoreCase(orden.getTipoOrden()) && "EN_RUTA".equals(nuevoEstado)) {
            throw new IllegalArgumentException("Una orden de mostrador no puede pasar a EN_RUTA");
        }
        if ("POR_ACEPTAR".equals(estadoActual) && !List.of("POR_ACEPTAR", "PREPARANDO").contains(nuevoEstado)) {
            throw new IllegalArgumentException("La orden debe ser aceptada por cocina antes de avanzar");
        }
        if ("PREPARANDO".equals(estadoActual) && !List.of("PREPARANDO", "LISTO").contains(nuevoEstado)) {
            throw new IllegalArgumentException("El pedido debe pasar primero a LISTO");
        }
        if ("LISTO".equals(estadoActual) && "PREPARANDO".equals(nuevoEstado)) {
            throw new IllegalArgumentException("El pedido ya no puede volver a PREPARANDO");
        }
        if ("EN_RUTA".equals(estadoActual) && !"ENTREGADO".equals(nuevoEstado)) {
            throw new IllegalArgumentException("Un pedido en ruta solo puede marcarse como ENTREGADO");
        }
        if ("LISTO".equals(estadoActual) && "ENTREGADO".equals(nuevoEstado) && "DOMICILIO".equalsIgnoreCase(orden.getTipoOrden())) {
            throw new IllegalArgumentException("El pedido debe pasar a EN_RUTA antes de ENTREGADO");
        }
        if ("DOMICILIO".equalsIgnoreCase(orden.getTipoOrden()) && "EN_RUTA".equals(nuevoEstado)) {
            OrdenDomicilio domicilio = orden.getOrdenDomicilio();
            if (domicilio == null || domicilio.getRepartidor() == null) {
                throw new IllegalArgumentException("Asigna un repartidor antes de marcar el pedido EN_RUTA");
            }
        }
    }

    private String normalizarEstadoEntregaDesdePedido(Orden orden, String nuevoEstado, String estadoActualDomicilio) {
        if (!"DOMICILIO".equalsIgnoreCase(orden.getTipoOrden())) {
            return estadoActualDomicilio;
        }
        if ("CANCELADO".equalsIgnoreCase(nuevoEstado)) {
            return estadoActualDomicilio;
        }
        return normalizarEstadoDomicilio(nuevoEstado);
    }

    private boolean tienePagoRegistrado(Long idOrden) {
        return pagoRepository.findTopByOrdenIdOrdenOrderByIdPagoDesc(idOrden).isPresent();
    }

    private String validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
