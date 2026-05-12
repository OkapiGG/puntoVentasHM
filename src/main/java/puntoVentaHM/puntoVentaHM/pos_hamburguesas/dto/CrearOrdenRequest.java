package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.util.List;

public record CrearOrdenRequest(
        Long idUsuario,
        String tipoOrden,
        DomicilioOrdenRequest domicilio,
        List<OrdenItemRequest> items
) {
}
