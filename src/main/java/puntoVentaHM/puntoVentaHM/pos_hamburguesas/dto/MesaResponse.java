package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record MesaResponse(
        Long idMesa,
        Integer numero,
        String estado,
        String meseroAsignado,
        String referenciaOrden
) {
}
