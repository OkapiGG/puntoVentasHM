package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.util.List;

public record CrearOrdenRequest(
        Long idUsuario,
        String tipoOrden,
        Long idMesa,
        DomicilioOrdenRequest domicilio,
        List<OrdenItemRequest> items
) {
}
