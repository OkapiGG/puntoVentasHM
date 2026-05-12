package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record PagoResponse(
        Long idPago,
        Long idOrden,
        String metodo,
        BigDecimal monto,
        BigDecimal efectivoRecibido,
        BigDecimal cambio,
        String estadoOrden
) {
}
