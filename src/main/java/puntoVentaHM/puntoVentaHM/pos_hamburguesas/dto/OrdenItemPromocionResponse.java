package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record OrdenItemPromocionResponse(
        String nombre,
        String tipoRegla,
        BigDecimal descuento,
        String descripcion
) {
}
