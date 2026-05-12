package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;

public record CajaResumenUsuarioResponse(
        Long idUsuario,
        String nombreUsuario,
        Integer sesiones,
        Integer cierres,
        Integer ventasCobradas,
        Integer cancelaciones,
        Integer retiros,
        BigDecimal totalVentas,
        BigDecimal efectivoEnCaja,
        BigDecimal retirosSeguridad,
        BigDecimal saldoEsperado,
        BigDecimal diferenciaAcumulada
) {
}
