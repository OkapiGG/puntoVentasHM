package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record AdminModificadorResponse(
        Long idModificador,
        Long idProducto,
        String productoNombre,
        String nombre,
        BigDecimal precioExtra,
        Boolean activo
) {
}
