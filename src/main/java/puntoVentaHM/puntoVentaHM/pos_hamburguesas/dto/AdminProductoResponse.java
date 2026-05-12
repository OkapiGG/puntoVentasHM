package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record AdminProductoResponse(
        Long idProducto,
        Long idCategoria,
        String categoriaNombre,
        String nombre,
        BigDecimal precio,
        String imagenUrl,
        Boolean activo,
        Boolean esPopular,
        Boolean esNuevo,
        Integer totalModificadores
) {
}
