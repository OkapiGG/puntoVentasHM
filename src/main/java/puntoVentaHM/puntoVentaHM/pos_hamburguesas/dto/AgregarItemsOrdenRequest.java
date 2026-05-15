package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.util.List;

public record AgregarItemsOrdenRequest(
        List<OrdenItemRequest> items
) {
}
