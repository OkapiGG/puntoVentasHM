package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record OrdenItemModResponse(
        Long idModificador,
        String nombre,
        BigDecimal precioExtra
) {
}
