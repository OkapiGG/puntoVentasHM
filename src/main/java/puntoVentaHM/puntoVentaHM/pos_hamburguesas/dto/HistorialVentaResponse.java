package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialVentaResponse(
        Long idOrden,
        String folio,
        LocalDateTime fecha,
        String tipoOrden,
        String metodoPago,
        BigDecimal total,
        String estado
) {
}
