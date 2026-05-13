package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record ActualizarMesaRequest(
        String estado,
        String meseroAsignado,
        String referenciaOrden
) {
}
