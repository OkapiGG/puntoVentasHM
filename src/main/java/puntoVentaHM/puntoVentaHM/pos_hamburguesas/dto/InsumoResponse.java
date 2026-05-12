package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record InsumoResponse(
        Long idInsumo,
        String nombre,
        String unidadMedida,
        BigDecimal stockActual,
        Boolean activo
) {
}
