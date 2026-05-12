package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record AdminUsuarioRequest(
        String nombre,
        String pinAcceso,
        String rol,
        String avatarUrl,
        Boolean activo
) {
}
