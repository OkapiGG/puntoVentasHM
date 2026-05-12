package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record AdminProductoRequest(
        Long idCategoria,
        String nombre,
        BigDecimal precio,
        String imagenUrl,
        Boolean activo,
        Boolean esPopular,
        Boolean esNuevo
) {
}
