package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record LoginResponse(
        Long idUsuario,
        String nombre,
        String rol,
        Long idNegocio,
        String avatarUrl
) {
}
