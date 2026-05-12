package puntoVentaHM.puntoVentaHM.pos_hamburguesas.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;

@Service
public class AccessControlService {

    private static final String USER_HEADER = "X-User-Id";

    private final UsuarioRepository usuarioRepository;

    public AccessControlService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario requireAnyRole(HttpServletRequest request, RolSistema... allowedRoles) {
        Usuario usuario = requireAuthenticated(request);
        RolSistema rolActual = RolSistema.from(usuario.getRol());
        Set<RolSistema> permitidos = Arrays.stream(allowedRoles).collect(Collectors.toSet());
        if (!permitidos.contains(rolActual)) {
            throw new ForbiddenOperationException("No tienes permisos para esta operacion");
        }
        return usuario;
    }

    public Usuario requireAuthenticated(HttpServletRequest request) {
        String rawUserId = request.getHeader(USER_HEADER);
        if (rawUserId == null || rawUserId.trim().isBlank()) {
            throw new UnauthorizedAccessException("Falta identificar al usuario actual");
        }

        Long idUsuario;
        try {
            idUsuario = Long.valueOf(rawUserId.trim());
        } catch (NumberFormatException exception) {
            throw new UnauthorizedAccessException("Identificador de usuario invalido");
        }

        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UnauthorizedAccessException("Usuario no encontrado"));
    }
}
