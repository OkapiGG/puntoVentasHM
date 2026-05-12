package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AperturaCajaRequest;
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
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.SesionCajaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;

@Service
public class CajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final UsuarioRepository usuarioRepository;

    public CajaService(
            SesionCajaRepository sesionCajaRepository,
            MovimientoCajaRepository movimientoCajaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.sesionCajaRepository = sesionCajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public SesionCajaResponse obtenerSesionActual(Long idUsuario) {
        return sesionCajaRepository.findTopByUsuarioIdUsuarioOrderByAperturaDesc(idUsuario)
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
        movimientoCaja.setMotivo(motivo);
        movimientoCajaRepository.save(movimientoCaja);

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
                .findBySesionCajaIdSesionCajaOrderByIdMovimientoCajaDesc(sesionCaja.getIdSesionCaja())
                .stream()
                .map(movimiento -> new MovimientoCajaResponse(
                        movimiento.getIdMovimientoCaja(),
                        movimiento.getTipo(),
                        movimiento.getMonto(),
                        movimiento.getMotivo()
                ))
                .toList();

        BigDecimal totalVentas = sumarPorTipo(movimientos, "INGRESO_VENTA");
        BigDecimal totalIngresosManuales = sumarPorTipo(movimientos, "INGRESO_MANUAL");
        BigDecimal totalRetiros = sumarPorTipo(movimientos, "RETIRO");
        BigDecimal saldoEsperado = sesionCaja.getFondoInicial()
                .add(totalVentas)
                .add(totalIngresosManuales)
                .subtract(totalRetiros);
        BigDecimal montoDeclaradoFinal = montoDeclarado != null ? montoDeclarado : sesionCaja.getMontoDeclaradoCierre();
        BigDecimal diferencia = montoDeclaradoFinal != null
                ? montoDeclaradoFinal.subtract(saldoEsperado)
                : sesionCaja.getDiferenciaCierre();
        int cantidadVentas = (int) sesionCaja.getOrdenes().stream()
                .filter(orden -> "PAGADA".equalsIgnoreCase(orden.getEstado()))
                .count();

        return new SesionCajaResponse(
                sesionCaja.getIdSesionCaja(),
                sesionCaja.getEstado(),
                sesionCaja.getFondoInicial(),
                sesionCaja.getApertura(),
                sesionCaja.getCierre(),
                totalVentas,
                totalIngresosManuales,
                totalRetiros,
                saldoEsperado,
                montoDeclaradoFinal,
                diferencia,
                cantidadVentas,
                movimientos.size(),
                movimientos
        );
    }

    private SesionCajaResumenResponse construirResumen(SesionCaja sesionCaja) {
        SesionCajaResponse respuesta = construirRespuesta(sesionCaja, null);
        return new SesionCajaResumenResponse(
                respuesta.idSesionCaja(),
                respuesta.estado(),
                respuesta.apertura(),
                respuesta.cierre(),
                respuesta.fondoInicial(),
                respuesta.totalVentas(),
                respuesta.totalIngresosManuales(),
                respuesta.totalRetiros(),
                respuesta.saldoEsperado(),
                respuesta.montoDeclarado(),
                respuesta.diferencia(),
                respuesta.cantidadVentas(),
                respuesta.cantidadMovimientos()
        );
    }

    private BigDecimal calcularSaldoEsperado(SesionCaja sesionCaja) {
        List<MovimientoCajaResponse> movimientos = movimientoCajaRepository
                .findBySesionCajaIdSesionCajaOrderByIdMovimientoCajaDesc(sesionCaja.getIdSesionCaja())
                .stream()
                .map(movimiento -> new MovimientoCajaResponse(
                        movimiento.getIdMovimientoCaja(),
                        movimiento.getTipo(),
                        movimiento.getMonto(),
                        movimiento.getMotivo()
                ))
                .toList();

        return sesionCaja.getFondoInicial()
                .add(sumarPorTipo(movimientos, "INGRESO_VENTA"))
                .add(sumarPorTipo(movimientos, "INGRESO_MANUAL"))
                .subtract(sumarPorTipo(movimientos, "RETIRO"));
    }

    private BigDecimal sumarPorTipo(List<MovimientoCajaResponse> movimientos, String tipo) {
        return movimientos.stream()
                .filter(movimiento -> tipo.equalsIgnoreCase(movimiento.tipo()))
                .map(MovimientoCajaResponse::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
