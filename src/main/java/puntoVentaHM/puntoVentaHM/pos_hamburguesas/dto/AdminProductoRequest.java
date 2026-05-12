package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminProductoRequest(
        Long idCategoria,
        String nombre,
        BigDecimal precio,
        String imagenUrl,
        Boolean activo,
        Boolean esPopular,
        Boolean esNuevo,
        List<RecetaItemRequest> receta
) {
}
