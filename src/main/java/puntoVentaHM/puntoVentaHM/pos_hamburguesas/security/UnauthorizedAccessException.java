package puntoVentaHM.puntoVentaHM.pos_hamburguesas.security;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
