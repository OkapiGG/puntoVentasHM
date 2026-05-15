package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenSeguimientoResponse(
        Long idOrden,
        String folio,
        LocalDateTime fecha,
        String nombreCajero,
        String tipoOrden,
        BigDecimal total,
        Integer cantidadItems,
        String estado,
        boolean pagada,
        String motivoCancelacion,
        Integer numeroMesa,
        DomicilioOrdenResponse domicilio
) {
}
