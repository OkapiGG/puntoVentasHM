package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
        Long idOrden,
        String folio,
        String tipoOrden,
        String estado,
        boolean pagada,
        BigDecimal totalDescuentoPromocional,
        BigDecimal total,
        LocalDateTime fecha,
        DomicilioOrdenResponse domicilio,
        List<OrdenItemResponse> items
) {
}
