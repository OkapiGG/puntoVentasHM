package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
        Long idMovimientoInventario,
        Long idInsumo,
        String nombreInsumo,
        String tipo,
        BigDecimal cantidad,
        BigDecimal stockAnterior,
        BigDecimal stockResultante,
        LocalDateTime fecha,
        String motivo,
        String referencia
) {
}
