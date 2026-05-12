package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoResponse(
        Long idProducto,
        String nombre,
        BigDecimal precio,
        String imagenUrl,
        Boolean activo,
        Boolean esPopular,
        Boolean esNuevo,
        String categoria,
        List<ModificadorResponse> modificadores
) {
}
