package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AdminPromocionResponse(
        Long idPromocion,
        String nombre,
        String descripcion,
        String tipoRegla,
        String tipoObjetivo,
        String idsObjetivo,
        Long idModificadorGratis,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalTime horaInicio,
        LocalTime horaFin,
        BigDecimal porcentajeDescuento,
        BigDecimal montoDescuento,
        Integer cantidadMinima,
        Boolean activa
) {
}
