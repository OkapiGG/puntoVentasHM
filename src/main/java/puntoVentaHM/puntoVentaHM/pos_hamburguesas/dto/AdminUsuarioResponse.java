package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record AdminUsuarioResponse(
        Long idUsuario,
        String nombre,
        String rol,
        String avatarUrl,
        Boolean activo
) {
}
