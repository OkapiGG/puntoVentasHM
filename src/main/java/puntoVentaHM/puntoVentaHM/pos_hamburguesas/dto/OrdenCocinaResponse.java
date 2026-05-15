package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrdenCocinaResponse(
        Long idOrden,
        String folio,
        LocalDateTime fecha,
        String tipoOrden,
        Integer cantidadItems,
        String estado,
        Integer numeroMesa,
        String nombreCliente,
        List<OrdenCocinaItem> items
) {
    public record OrdenCocinaItem(
            String nombre,
            Integer cantidad,
            List<String> modificadores
    ) {
    }
}
