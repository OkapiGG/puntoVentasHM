package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.util.List;

public record OrdenItemRequest(
        Long idProducto,
        Integer cantidad,
        List<OrdenItemModRequest> modificadores
) {
}
