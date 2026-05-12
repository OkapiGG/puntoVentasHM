package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record DomicilioOrdenResponse(
        String nombreCliente,
        String direccion,
        String telefono,
        String estadoEntrega,
        Long idRepartidor,
        String nombreRepartidor
) {
}
