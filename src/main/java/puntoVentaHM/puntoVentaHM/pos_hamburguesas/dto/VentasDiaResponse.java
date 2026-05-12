package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VentasDiaResponse(
        LocalDate fecha,
        BigDecimal totalVentas,
        Integer cantidadVentas,
        Integer cantidadCanceladas,
        Integer cantidadActivas,
        List<HistorialVentaResponse> ventasPagadas
) {
}
