package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record DomicilioOrdenRequest(
        String nombreCliente,
        String direccion,
        String telefono,
        BigDecimal costoEnvio
) {
}
