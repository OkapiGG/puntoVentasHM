package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminModificadorRequest(
        Long idProducto,
        String nombre,
        BigDecimal precioExtra,
        Boolean activo,
        List<RecetaItemRequest> receta
) {
}
