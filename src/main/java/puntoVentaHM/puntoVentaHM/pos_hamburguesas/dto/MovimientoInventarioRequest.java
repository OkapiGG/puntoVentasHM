package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record MovimientoInventarioRequest(
        String tipo,
        BigDecimal cantidad,
        String motivo
) {
}
