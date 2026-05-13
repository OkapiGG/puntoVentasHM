package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaResponse(
        Long idMovimientoCaja,
        String tipo,
        BigDecimal monto,
        LocalDateTime fecha,
        String motivo
) {
}
