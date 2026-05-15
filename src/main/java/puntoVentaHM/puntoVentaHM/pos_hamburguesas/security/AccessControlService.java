package puntoVentaHM.puntoVentaHM.pos_hamburguesas.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.SesionUsuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.SesionCajaRepository;

@Service
public class AccessControlService {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SesionCajaRepository sesionCajaRepository;
    private final TokenService tokenService;

    public AccessControlService(SesionCajaRepository sesionCajaRepository, TokenService tokenService) {
        this.sesionCajaRepository = sesionCajaRepository;
        this.tokenService = tokenService;
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
        String token = extraerToken(request);
        if (token == null) {
            throw new UnauthorizedAccessException("Falta identificar al usuario actual");
        }
        SesionUsuario sesion = tokenService.validarToken(token);
        if (!Boolean.TRUE.equals(sesion.getUsuario().getActivo())) {
            throw new UnauthorizedAccessException("Usuario inactivo");
        }
        return sesion.getUsuario();
    }

    public String extraerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    public void requireOpenShift(Usuario usuario) {
        sesionCajaRepository.findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(usuario.getIdUsuario(), "ABIERTA")
                .orElseThrow(() -> new ForbiddenOperationException("Debes abrir un turno antes de operar"));
    }

    public void requireSameTenant(Usuario usuario, Long idNegocio) {
        if (idNegocio == null) {
            throw new ForbiddenOperationException("Negocio no especificado");
        }
        if (!Objects.equals(usuario.getNegocio().getIdNegocio(), idNegocio)) {
            throw new ForbiddenOperationException("No tienes permisos sobre este negocio");
        }
    }
}
