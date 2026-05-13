package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record AdminInsumoRequest(
        String nombre,
        String unidadMedida,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        BigDecimal costoUnitario,
        Boolean activo
) {
}
