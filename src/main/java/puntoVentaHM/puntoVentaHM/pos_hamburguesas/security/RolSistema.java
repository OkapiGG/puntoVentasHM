package puntoVentaHM.puntoVentaHM.pos_hamburguesas.security;

public enum RolSistema {
    ADMIN,
    GERENTE,
    CAJERO,
    REPARTIDOR;

    public static RolSistema from(String value) {
        if (value == null || value.trim().isBlank()) {
            throw new UnauthorizedAccessException("Rol de usuario no configurado");
        }
        try {
            return RolSistema.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedAccessException("Rol de usuario invalido");
        }
    }
}
