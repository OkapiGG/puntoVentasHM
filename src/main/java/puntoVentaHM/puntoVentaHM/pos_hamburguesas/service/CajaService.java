package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AperturaCajaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.BitacoraCajaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CajaResumenPeriodoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CajaResumenUsuarioResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CierreCajaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MovimientoCajaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MovimientoCajaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.SesionCajaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.SesionCajaResumenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.MovimientoCaja;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Orden;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.SesionCaja;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.MovimientoCajaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.OrdenRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.SesionCajaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;

@Service
public class CajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final OrdenRepository ordenRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.caja.umbral-retiro-seguridad:3000}")
    private BigDecimal umbralRetiroSeguridad;

    public CajaService(
            SesionCajaRepository sesionCajaRepository,
            MovimientoCajaRepository movimientoCajaRepository,
            OrdenRepository ordenRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.sesionCajaRepository = sesionCajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.ordenRepository = ordenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public SesionCajaResponse obtenerSesionActual(Long idUsuario) {
        return sesionCajaRepository.findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(idUsuario, "ABIERTA")
                .map(sesionCaja -> construirRespuesta(sesionCaja, null))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SesionCajaResumenResponse> listarHistorial(Long idUsuario) {
        return sesionCajaRepository.findByUsuarioIdUsuarioOrderByAperturaDesc(idUsuario).stream()
                .map(this::construirResumen)
                .toList();
    }

    @Transactional(readOnly = true)
    public CajaResumenPeriodoResponse obtenerResumenPeriodo(Long idNegocio, String periodo) {
        RangoPeriodo rango = resolverRangoPeriodo(periodo);
        List<SesionCaja> sesiones = sesionCajaRepository.findByUsuarioNegocioIdNegocioAndAperturaBetweenOrderByAperturaDesc(
                idNegocio,
                rango.inicio(),
                rango.fin()
        );
        return construirResumenPeriodo(periodoNormalizado(periodo), rango, sesiones);
    }

    @Transactional(readOnly = true)
    public List<CajaResumenUsuarioResponse> obtenerResumenPorUsuario(Long idNegocio, String periodo) {
        RangoPeriodo rango = resolverRangoPeriodo(periodo);
        List<SesionCaja> sesiones = sesionCajaRepository.findByUsuarioNegocioIdNegocioAndAperturaBetweenOrderByAperturaDesc(
                idNegocio,
                rango.inicio(),
                rango.fin()
        );

        java.util.Map<Long, List<SesionCaja>> sesionesPorUsuario = sesiones.stream()
                .collect(java.util.stream.Collectors.groupingBy(sesion -> sesion.getUsuario().getIdUsuario()));

        return sesionesPorUsuario.entrySet().stream()
                .map(entry -> construirResumenUsuario(entry.getKey(), entry.getValue(), rango))
                .sorted(Comparator.comparing(CajaResumenUsuarioResponse::nombreUsuario))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BitacoraCajaResponse> obtenerBitacora(Long idNegocio, String periodo) {
        RangoPeriodo rango = resolverRangoPeriodo(periodo);
        List<SesionCaja> sesiones = sesionCajaRepository.findByUsuarioNegocioIdNegocioAndAperturaBetweenOrderByAperturaDesc(
                idNegocio,
                rango.inicio(),
                rango.fin()
        );

        List<BitacoraCajaResponse> eventos = new ArrayList<>();
        for (SesionCaja sesion : sesiones) {
            for (MovimientoCaja movimiento : movimientoCajaRepository.findBySesionCajaIdSesionCajaOrderByFechaDesc(sesion.getIdSesionCaja())) {
                if (!movimiento.getFecha().isBefore(rango.inicio()) && !movimiento.getFecha().isAfter(rango.fin())) {
                    if ("RETIRO".equalsIgnoreCase(movimiento.getTipo()) || "RETIRO_SEGURIDAD".equalsIgnoreCase(movimiento.getTipo())) {
                        eventos.add(new BitacoraCajaResponse(
                                "RETIRO".equalsIgnoreCase(movimiento.getTipo()) ? "RETIRO_MANUAL" : "RETIRO_SEGURIDAD",
                                movimiento.getFecha(),
                                sesion.getUsuario().getIdUsuario(),
                                sesion.getUsuario().getNombre(),
                                movimiento.getIdMovimientoCaja(),
                                "Movimiento de caja",
                                movimiento.getMonto(),
                                movimiento.getMotivo()
                        ));
                    }
                }
            }
        }

        eventos.addAll(
                ordenRepository.findBySesionCajaUsuarioNegocioIdNegocioAndFechaCancelacionBetweenOrderByFechaCancelacionDesc(idNegocio, rango.inicio(), rango.fin())
                        .stream()
                        .map(orden -> new BitacoraCajaResponse(
                                "CANCELACION",
                                orden.getFechaCancelacion(),
                                orden.getSesionCaja().getUsuario().getIdUsuario(),
                                orden.getSesionCaja().getUsuario().getNombre(),
                                orden.getIdOrden(),
                                "Orden " + orden.getIdOrden(),
                                orden.getTotal(),
                                orden.getMotivoCancelacion()
                        ))
                        .toList()
        );

        return eventos.stream()
                .sorted(Comparator.comparing(BitacoraCajaResponse::fecha).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public SesionCajaResponse obtenerCorte(Long idSesionCaja) {
        SesionCaja sesionCaja = sesionCajaRepository.findById(idSesionCaja)
                .orElseThrow(() -> new IllegalArgumentException("Sesion de caja no encontrada"));
        return construirRespuesta(sesionCaja, null);
    }

    @Transactional
    public SesionCajaResponse abrirCaja(Long idUsuario, AperturaCajaRequest request) {
        sesionCajaRepository.findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(idUsuario, "ABIERTA")
                .ifPresent(sesionCaja -> {
                    throw new IllegalArgumentException("Ya existe una sesion de caja abierta");
                });

        BigDecimal fondoInicial = request == null || request.fondoInicial() == null
                ? BigDecimal.ZERO
                : request.fondoInicial();

        if (fondoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El fondo inicial no puede ser negativo");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        SesionCaja sesionCaja = new SesionCaja();
        sesionCaja.setUsuario(usuario);
        sesionCaja.setFondoInicial(fondoInicial);
        sesionCaja.setApertura(LocalDateTime.now());
        sesionCaja.setEstado("ABIERTA");

        return construirRespuesta(sesionCajaRepository.save(sesionCaja), null);
    }

    @Transactional
    public SesionCajaResponse registrarMovimiento(Long idUsuario, MovimientoCajaRequest request) {
        SesionCaja sesionCaja = obtenerSesionAbierta(idUsuario);
        if (request == null || request.monto() == null || request.monto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del movimiento debe ser mayor a cero");
        }

        String tipo = request.tipo() == null ? "" : request.tipo().trim().toUpperCase();
        if (!"INGRESO_MANUAL".equals(tipo) && !"RETIRO".equals(tipo)) {
            throw new IllegalArgumentException("Tipo de movimiento invalido");
        }

        String motivo = request.motivo() == null ? "" : request.motivo().trim();
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el motivo del movimiento");
        }

        MovimientoCaja movimientoCaja = new MovimientoCaja();
        movimientoCaja.setSesionCaja(sesionCaja);
        movimientoCaja.setTipo(tipo);
        movimientoCaja.setMonto(request.monto());
        movimientoCaja.setFecha(LocalDateTime.now());
        movimientoCaja.setMotivo(motivo);
        movimientoCajaRepository.save(movimientoCaja);
        aplicarRetiroSeguridadSiCorresponde(sesionCaja, "INGRESO_MANUAL".equals(tipo) ? "ingreso manual" : "movimiento manual");

        return construirRespuesta(sesionCaja, null);
    }

    @Transactional
    public SesionCajaResponse cerrarCaja(Long idUsuario, CierreCajaRequest request) {
        SesionCaja sesionCaja = obtenerSesionAbierta(idUsuario);
        BigDecimal montoDeclarado = request == null ? null : request.montoDeclarado();

        if (montoDeclarado != null && montoDeclarado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto declarado no puede ser negativo");
        }

        sesionCaja.setEstado("CERRADA");
        sesionCaja.setCierre(LocalDateTime.now());
        BigDecimal diferencia = montoDeclarado == null ? null : montoDeclarado.subtract(calcularSaldoEsperado(sesionCaja));
        sesionCaja.setMontoDeclaradoCierre(montoDeclarado);
        sesionCaja.setDiferenciaCierre(diferencia);
        sesionCajaRepository.save(sesionCaja);

        return construirRespuesta(sesionCaja, montoDeclarado);
    }

    private SesionCaja obtenerSesionAbierta(Long idUsuario) {
        return sesionCajaRepository.findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(idUsuario, "ABIERTA")
                .orElseThrow(() -> new IllegalArgumentException("No hay una sesion de caja abierta"));
    }

    private SesionCajaResponse construirRespuesta(SesionCaja sesionCaja, BigDecimal montoDeclarado) {
        List<MovimientoCajaResponse> movimientos = movimientoCajaRepository
                .findBySesionCajaIdSesionCajaOrderByFechaDesc(sesionCaja.getIdSesionCaja())
                .stream()
                .map(movimiento -> new MovimientoCajaResponse(
                        movimiento.getIdMovimientoCaja(),
                        movimiento.getTipo(),
                        movimiento.getMonto(),
                        movimiento.getFecha(),
                        movimiento.getMotivo()
                ))
                .toList();

        BigDecimal totalVentasEfectivo = sumarPorTipo(movimientos, "INGRESO_VENTA_EFECTIVO").add(sumarPorTipo(movimientos, "INGRESO_VENTA"));
        BigDecimal totalVentasTarjeta = sumarPorTipo(movimientos, "INGRESO_VENTA_TARJETA");
        BigDecimal totalVentas = totalVentasEfectivo.add(totalVentasTarjeta);
        BigDecimal totalIngresosManuales = sumarPorTipo(movimientos, "INGRESO_MANUAL");
        BigDecimal totalRetiros = sumarPorTipo(movimientos, "RETIRO");
        BigDecimal totalRetirosSeguridad = sumarPorTipo(movimientos, "RETIRO_SEGURIDAD");
        BigDecimal retirosTotales = totalRetiros.add(totalRetirosSeguridad);
        BigDecimal efectivoEnCaja = sesionCaja.getFondoInicial()
                .add(totalVentasEfectivo)
                .add(totalIngresosManuales)
                .subtract(retirosTotales);
        BigDecimal saldoEsperado = efectivoEnCaja.add(totalVentasTarjeta);
        BigDecimal montoDeclaradoFinal = montoDeclarado != null ? montoDeclarado : sesionCaja.getMontoDeclaradoCierre();
        BigDecimal diferencia = montoDeclaradoFinal != null
                ? montoDeclaradoFinal.subtract(efectivoEnCaja)
                : sesionCaja.getDiferenciaCierre();
        int cantidadVentas = (int) movimientos.stream()
                .filter(movimiento -> movimiento.tipo().startsWith("INGRESO_VENTA"))
                .count();
        int cantidadCancelaciones = (int) sesionCaja.getOrdenes().stream()
                .filter(orden -> "CANCELADO".equalsIgnoreCase(orden.getEstado()))
                .count();

        return new SesionCajaResponse(
                sesionCaja.getIdSesionCaja(),
                sesionCaja.getUsuario().getIdUsuario(),
                sesionCaja.getUsuario().getNombre(),
                sesionCaja.getEstado(),
                sesionCaja.getFondoInicial(),
                sesionCaja.getApertura(),
                sesionCaja.getCierre(),
                totalVentas,
                totalVentasEfectivo,
                totalVentasTarjeta,
                totalIngresosManuales,
                retirosTotales,
                totalRetirosSeguridad,
                efectivoEnCaja,
                saldoEsperado,
                montoDeclaradoFinal,
                diferencia,
                cantidadVentas,
                cantidadCancelaciones,
                movimientos.size(),
                movimientos
        );
    }

    private SesionCajaResumenResponse construirResumen(SesionCaja sesionCaja) {
        SesionCajaResponse respuesta = construirRespuesta(sesionCaja, null);
        return new SesionCajaResumenResponse(
                respuesta.idSesionCaja(),
                respuesta.idUsuario(),
                respuesta.nombreUsuario(),
                respuesta.estado(),
                respuesta.apertura(),
                respuesta.cierre(),
                respuesta.fondoInicial(),
                respuesta.totalVentas(),
                respuesta.totalVentasEfectivo(),
                respuesta.totalVentasTarjeta(),
                respuesta.totalIngresosManuales(),
                respuesta.totalRetiros(),
                respuesta.totalRetirosSeguridad(),
                respuesta.efectivoEnCaja(),
                respuesta.saldoEsperado(),
                respuesta.montoDeclarado(),
                respuesta.diferencia(),
                respuesta.cantidadVentas(),
                respuesta.cantidadCancelaciones(),
                respuesta.cantidadMovimientos()
        );
    }

    private BigDecimal calcularSaldoEsperado(SesionCaja sesionCaja) {
        List<MovimientoCajaResponse> movimientos = movimientoCajaRepository
                .findBySesionCajaIdSesionCajaOrderByFechaDesc(sesionCaja.getIdSesionCaja())
                .stream()
                .map(movimiento -> new MovimientoCajaResponse(
                        movimiento.getIdMovimientoCaja(),
                        movimiento.getTipo(),
                        movimiento.getMonto(),
                        movimiento.getFecha(),
                        movimiento.getMotivo()
                ))
                .toList();

        return sesionCaja.getFondoInicial()
                .add(sumarPorTipo(movimientos, "INGRESO_VENTA_EFECTIVO").add(sumarPorTipo(movimientos, "INGRESO_VENTA")))
                .add(sumarPorTipo(movimientos, "INGRESO_MANUAL"))
                .subtract(sumarPorTipo(movimientos, "RETIRO").add(sumarPorTipo(movimientos, "RETIRO_SEGURIDAD")));
    }

    private BigDecimal sumarPorTipo(List<MovimientoCajaResponse> movimientos, String tipo) {
        return movimientos.stream()
                .filter(movimiento -> tipo.equalsIgnoreCase(movimiento.tipo()))
                .map(MovimientoCajaResponse::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void aplicarRetiroSeguridadSiCorresponde(SesionCaja sesionCaja, String origen) {
        if (sesionCaja == null || !"ABIERTA".equalsIgnoreCase(sesionCaja.getEstado())) {
            return;
        }

        BigDecimal efectivoActual = calcularSaldoEsperado(sesionCaja);
        if (efectivoActual.compareTo(umbralRetiroSeguridad) <= 0) {
            return;
        }

        BigDecimal retiro = efectivoActual.subtract(umbralRetiroSeguridad).setScale(2, RoundingMode.HALF_UP);
        if (retiro.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        MovimientoCaja retiroSeguridad = new MovimientoCaja();
        retiroSeguridad.setSesionCaja(sesionCaja);
        retiroSeguridad.setTipo("RETIRO_SEGURIDAD");
        retiroSeguridad.setMonto(retiro);
        retiroSeguridad.setFecha(LocalDateTime.now());
        retiroSeguridad.setMotivo("Retiro automático por umbral después de " + origen);
        movimientoCajaRepository.save(retiroSeguridad);
    }

    private CajaResumenPeriodoResponse construirResumenPeriodo(String periodo, RangoPeriodo rango, List<SesionCaja> sesiones) {
        List<SesionCajaResponse> respuestas = sesiones.stream().map(sesion -> construirRespuesta(sesion, null)).toList();
        int cierres = (int) respuestas.stream().filter(respuesta -> "CERRADA".equalsIgnoreCase(respuesta.estado())).count();
        int ventas = respuestas.stream().mapToInt(SesionCajaResponse::cantidadVentas).sum();
        int cancelaciones = respuestas.stream().mapToInt(SesionCajaResponse::cantidadCancelaciones).sum();
        int retiros = respuestas.stream()
                .flatMap(respuesta -> respuesta.movimientos().stream())
                .filter(movimiento -> "RETIRO".equalsIgnoreCase(movimiento.tipo()) || "RETIRO_SEGURIDAD".equalsIgnoreCase(movimiento.tipo()))
                .toList()
                .size();

        return new CajaResumenPeriodoResponse(
                periodo,
                rango.inicio().toLocalDate(),
                rango.fin().toLocalDate(),
                respuestas.size(),
                cierres,
                ventas,
                cancelaciones,
                retiros,
                sumar(respuestas, SesionCajaResponse::totalVentas),
                sumar(respuestas, SesionCajaResponse::efectivoEnCaja),
                sumar(respuestas, SesionCajaResponse::totalIngresosManuales),
                sumar(respuestas, respuesta -> respuesta.totalRetiros().subtract(respuesta.totalRetirosSeguridad())),
                sumar(respuestas, SesionCajaResponse::totalRetirosSeguridad),
                sumar(respuestas, SesionCajaResponse::saldoEsperado),
                sumarOpcional(respuestas, SesionCajaResponse::montoDeclarado),
                sumarOpcional(respuestas, SesionCajaResponse::diferencia)
        );
    }

    private CajaResumenUsuarioResponse construirResumenUsuario(Long idUsuario, List<SesionCaja> sesiones, RangoPeriodo rango) {
        List<SesionCajaResponse> respuestas = sesiones.stream().map(sesion -> construirRespuesta(sesion, null)).toList();
        SesionCaja muestra = sesiones.getFirst();
        int cierres = (int) respuestas.stream().filter(respuesta -> "CERRADA".equalsIgnoreCase(respuesta.estado())).count();
        int retiros = respuestas.stream()
                .flatMap(respuesta -> respuesta.movimientos().stream())
                .filter(movimiento -> "RETIRO".equalsIgnoreCase(movimiento.tipo()) || "RETIRO_SEGURIDAD".equalsIgnoreCase(movimiento.tipo()))
                .toList()
                .size();

        return new CajaResumenUsuarioResponse(
                idUsuario,
                muestra.getUsuario().getNombre(),
                respuestas.size(),
                cierres,
                respuestas.stream().mapToInt(SesionCajaResponse::cantidadVentas).sum(),
                respuestas.stream().mapToInt(SesionCajaResponse::cantidadCancelaciones).sum(),
                retiros,
                sumar(respuestas, SesionCajaResponse::totalVentas),
                sumar(respuestas, SesionCajaResponse::efectivoEnCaja),
                sumar(respuestas, SesionCajaResponse::totalRetirosSeguridad),
                sumar(respuestas, SesionCajaResponse::saldoEsperado),
                sumarOpcional(respuestas, SesionCajaResponse::diferencia)
        );
    }

    private BigDecimal sumar(List<SesionCajaResponse> respuestas, java.util.function.Function<SesionCajaResponse, BigDecimal> extractor) {
        return respuestas.stream().map(extractor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumarOpcional(List<SesionCajaResponse> respuestas, java.util.function.Function<SesionCajaResponse, BigDecimal> extractor) {
        return respuestas.stream().map(extractor).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private RangoPeriodo resolverRangoPeriodo(String periodo) {
        String tipo = periodoNormalizado(periodo);
        LocalDate hoy = LocalDate.now();
        LocalDate inicio;
        LocalDate fin;

        switch (tipo) {
            case "SEMANAL" -> {
                inicio = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                fin = inicio.plusDays(6);
            }
            case "MENSUAL" -> {
                inicio = hoy.withDayOfMonth(1);
                fin = hoy.withDayOfMonth(hoy.lengthOfMonth());
            }
            default -> {
                inicio = hoy;
                fin = hoy;
            }
        }

        return new RangoPeriodo(inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
    }

    private String periodoNormalizado(String periodo) {
        String valor = periodo == null ? "DIARIO" : periodo.trim().toUpperCase();
        if (!List.of("DIARIO", "SEMANAL", "MENSUAL").contains(valor)) {
            throw new IllegalArgumentException("Periodo inválido");
        }
        return valor;
    }

    private record RangoPeriodo(LocalDateTime inicio, LocalDateTime fin) {}
}
