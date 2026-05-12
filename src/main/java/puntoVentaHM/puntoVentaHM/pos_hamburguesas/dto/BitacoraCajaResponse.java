package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BitacoraCajaResponse(
        String tipoEvento,
        LocalDateTime fecha,
        Long idUsuario,
        String nombreUsuario,
        Long referenciaId,
        String referencia,
        BigDecimal monto,
        String motivo
) {
}
