package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record CambiarPinRequest(
        String pinActual,
        String pinNuevo
) {
}
