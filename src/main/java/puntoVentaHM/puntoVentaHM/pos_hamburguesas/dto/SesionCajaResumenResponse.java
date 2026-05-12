package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionCajaResumenResponse(
        Long idSesionCaja,
        Long idUsuario,
        String nombreUsuario,
        String estado,
        LocalDateTime apertura,
        LocalDateTime cierre,
        BigDecimal fondoInicial,
        BigDecimal totalVentas,
        BigDecimal totalVentasEfectivo,
        BigDecimal totalVentasTarjeta,
        BigDecimal totalIngresosManuales,
        BigDecimal totalRetiros,
        BigDecimal totalRetirosSeguridad,
        BigDecimal efectivoEnCaja,
        BigDecimal saldoEsperado,
        BigDecimal montoDeclarado,
        BigDecimal diferencia,
        Integer cantidadVentas,
        Integer cantidadCancelaciones,
        Integer cantidadMovimientos
) {
}
