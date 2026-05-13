package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record NegocioConfigResponse(
        Long idNegocio,
        String nombre,
        BigDecimal limiteCaja,
        BigDecimal costoEnvioDefault,
        Boolean alertasCaja,
        Boolean notificaNuevosPedidos,
        Boolean notificaReportesDiarios
) {
}
