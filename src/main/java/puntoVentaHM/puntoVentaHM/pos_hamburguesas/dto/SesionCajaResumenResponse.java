package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionCajaResumenResponse(
        Long idSesionCaja,
        String estado,
        LocalDateTime apertura,
        LocalDateTime cierre,
        BigDecimal fondoInicial,
        BigDecimal totalVentas,
        BigDecimal totalIngresosManuales,
        BigDecimal totalRetiros,
        BigDecimal saldoEsperado,
        BigDecimal montoDeclarado,
        BigDecimal diferencia,
        Integer cantidadVentas,
        Integer cantidadMovimientos
) {
}
