package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record RecetaItemRequest(
        Long idInsumo,
        BigDecimal cantidad
) {
}
