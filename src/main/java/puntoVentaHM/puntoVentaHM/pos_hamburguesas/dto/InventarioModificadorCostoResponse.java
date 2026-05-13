package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventarioModificadorCostoResponse(
        Long idModificador,
        String nombre,
        String productoNombre,
        BigDecimal precioExtra,
        BigDecimal costoEstimado,
        List<RecetaItemResponse> receta
) {
}
