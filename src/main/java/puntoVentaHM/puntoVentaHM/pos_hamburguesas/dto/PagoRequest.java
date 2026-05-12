package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record PagoRequest(
        String metodo,
        BigDecimal efectivoRecibido
) {
}
