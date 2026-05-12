package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminProductoResponse(
        Long idProducto,
        Long idCategoria,
        String categoriaNombre,
        String nombre,
        BigDecimal precio,
        BigDecimal costoEstimado,
        String imagenUrl,
        Boolean activo,
        Boolean esPopular,
        Boolean esNuevo,
        Integer totalModificadores,
        List<RecetaItemResponse> receta
) {
}
