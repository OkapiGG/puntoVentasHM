package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.time.LocalDateTime;

public record CancelacionOrdenResponse(
        Long idOrden,
        String folio,
        String estado,
        String motivoCancelacion,
        LocalDateTime fechaCancelacion
) {
}
