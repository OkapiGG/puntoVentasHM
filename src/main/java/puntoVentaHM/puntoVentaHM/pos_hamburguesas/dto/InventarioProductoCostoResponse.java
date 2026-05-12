package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventarioProductoCostoResponse(
        Long idProducto,
        String nombre,
        String categoria,
        BigDecimal precioVenta,
        BigDecimal costoEstimado,
        List<RecetaItemResponse> receta
) {
}
