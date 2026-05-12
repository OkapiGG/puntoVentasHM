package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenActivaResponse(
        Long idOrden,
        String folio,
        LocalDateTime fecha,
        String tipoOrden,
        BigDecimal total,
        Integer cantidadItems,
        String estado,
        DomicilioOrdenResponse domicilio
) {
}
