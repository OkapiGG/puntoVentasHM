package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record DomicilioOrdenResponse(
        String nombreCliente,
        String direccion,
        String telefono,
        BigDecimal costoEnvio,
        String estadoEntrega,
        Long idRepartidor,
        String nombreRepartidor
) {
}
