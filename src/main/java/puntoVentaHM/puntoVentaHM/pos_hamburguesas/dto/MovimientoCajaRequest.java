package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record MovimientoCajaRequest(
        String tipo,
        BigDecimal monto,
        String motivo
) {
}
