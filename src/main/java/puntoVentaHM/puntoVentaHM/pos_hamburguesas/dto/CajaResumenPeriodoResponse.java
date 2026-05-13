package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CajaResumenPeriodoResponse(
        String periodo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer sesiones,
        Integer cierres,
        Integer ventasCobradas,
        Integer cancelaciones,
        Integer retiros,
        BigDecimal totalVentas,
        BigDecimal efectivoEnCaja,
        BigDecimal ingresosManuales,
        BigDecimal retirosManuales,
        BigDecimal retirosSeguridad,
        BigDecimal saldoEsperado,
        BigDecimal montoDeclarado,
        BigDecimal diferenciaAcumulada
) {
}
