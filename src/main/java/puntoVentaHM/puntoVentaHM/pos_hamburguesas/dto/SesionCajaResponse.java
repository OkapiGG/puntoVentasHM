package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SesionCajaResponse(
        Long idSesionCaja,
        String estado,
        BigDecimal fondoInicial,
        LocalDateTime apertura,
        LocalDateTime cierre,
        BigDecimal totalVentas,
        BigDecimal totalIngresosManuales,
        BigDecimal totalRetiros,
        BigDecimal saldoEsperado,
        BigDecimal montoDeclarado,
        BigDecimal diferencia,
        Integer cantidadVentas,
        Integer cantidadMovimientos,
        List<MovimientoCajaResponse> movimientos
) {
}
