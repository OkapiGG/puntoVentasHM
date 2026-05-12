package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record CotizacionOrdenResponse(
        BigDecimal subtotalProductos,
        BigDecimal costoEnvio,
        BigDecimal totalDescuentoPromocional,
        BigDecimal total,
        List<OrdenItemResponse> items
) {
}
