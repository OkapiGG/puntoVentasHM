package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record RecetaItemResponse(
        Long idInsumo,
        String nombreInsumo,
        String unidadMedida,
        BigDecimal cantidad,
        BigDecimal costoEstimado
) {
}
