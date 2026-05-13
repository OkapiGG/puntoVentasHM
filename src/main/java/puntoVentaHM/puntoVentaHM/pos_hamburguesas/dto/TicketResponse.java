package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TicketResponse(
        Long idOrden,
        String folio,
        String tipoOrden,
        String negocio,
        String cajero,
        LocalDateTime fecha,
        String metodoPago,
        BigDecimal totalDescuentoPromocional,
        BigDecimal total,
        BigDecimal efectivoRecibido,
        BigDecimal cambio,
        DomicilioOrdenResponse domicilio,
        List<OrdenItemResponse> items
) {
}
