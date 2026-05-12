package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record MovimientoCajaResponse(
        Long idMovimientoCaja,
        String tipo,
        BigDecimal monto,
        String motivo
) {
}
