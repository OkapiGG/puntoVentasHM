package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record AdminModificadorRequest(
        Long idProducto,
        String nombre,
        BigDecimal precioExtra,
        Boolean activo
) {
}
