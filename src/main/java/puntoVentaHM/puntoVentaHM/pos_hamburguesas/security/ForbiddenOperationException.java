package puntoVentaHM.puntoVentaHM.pos_hamburguesas.security;

public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
