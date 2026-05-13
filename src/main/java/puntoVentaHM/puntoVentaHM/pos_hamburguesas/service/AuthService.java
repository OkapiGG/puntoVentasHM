package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CambiarPinRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.LoginRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.LoginResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AccessControlService accessControlService;

    public AuthService(UsuarioRepository usuarioRepository, AccessControlService accessControlService) {
        this.usuarioRepository = usuarioRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByNombreIgnoreCaseAndPinAcceso(request.usuario(), request.password())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        return new LoginResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getNegocio().getIdNegocio(),
                usuario.getAvatarUrl()
        );
    }

    @Transactional
    public void cambiarPin(jakarta.servlet.http.HttpServletRequest httpRequest, CambiarPinRequest request) {
        Usuario usuario = accessControlService.requireAuthenticated(httpRequest);
        String pinActual = request.pinActual() == null ? "" : request.pinActual().trim();
        String pinNuevo = request.pinNuevo() == null ? "" : request.pinNuevo().trim();

        if (!usuario.getPinAcceso().equals(pinActual)) {
            throw new IllegalArgumentException("El pin actual es incorrecto");
        }
        if (pinNuevo.length() < 4) {
            throw new IllegalArgumentException("El nuevo pin debe tener al menos 4 caracteres");
        }

        usuario.setPinAcceso(pinNuevo);
        usuarioRepository.save(usuario);
    }
}
