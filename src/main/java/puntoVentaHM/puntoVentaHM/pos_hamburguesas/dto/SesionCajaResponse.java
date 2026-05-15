package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SesionCajaResponse(
        Long idSesionCaja,
        Long idUsuario,
        String nombreUsuario,
        String estado,
        BigDecimal fondoInicial,
        LocalDateTime apertura,
        LocalDateTime cierre,
        BigDecimal totalVentas,
        BigDecimal totalVentasEfectivo,
        BigDecimal totalVentasTarjeta,
        BigDecimal totalVentasTransferencia,
        BigDecimal totalIngresosManuales,
        BigDecimal totalRetiros,
        BigDecimal totalRetirosSeguridad,
        BigDecimal efectivoEnCaja,
        BigDecimal saldoEsperado,
        BigDecimal montoDeclarado,
        BigDecimal diferencia,
        Integer cantidadVentas,
        Integer cantidadCancelaciones,
        Integer cantidadMovimientos,
        List<MovimientoCajaResponse> movimientos
) {
}
