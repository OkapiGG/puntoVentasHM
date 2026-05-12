package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record DomicilioOrdenRequest(
        String nombreCliente,
        String direccion,
        String telefono
) {
}
