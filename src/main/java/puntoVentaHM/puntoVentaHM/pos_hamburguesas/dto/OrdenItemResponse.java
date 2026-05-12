package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrdenItemResponse(
        Long idProducto,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        List<OrdenItemModResponse> modificadores
) {
}
