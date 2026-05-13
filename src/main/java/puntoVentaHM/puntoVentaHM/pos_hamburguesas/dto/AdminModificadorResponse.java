package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminModificadorResponse(
        Long idModificador,
        Long idProducto,
        String productoNombre,
        String nombre,
        BigDecimal precioExtra,
        BigDecimal costoEstimado,
        Boolean activo,
        List<RecetaItemResponse> receta
) {
}
