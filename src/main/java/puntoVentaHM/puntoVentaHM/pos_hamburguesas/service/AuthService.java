package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CambiarPinRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.LoginRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.LoginResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.TokenService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AccessControlService accessControlService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            AccessControlService accessControlService,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.accessControlService = accessControlService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String nombre = request.usuario() == null ? "" : request.usuario().trim();
        String password = request.password() == null ? "" : request.password();
        if (nombre.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Credenciales invalidas");
        }

        Usuario usuario = usuarioRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas"));

        if (!passwordEncoder.matches(password, usuario.getPinAcceso())) {
            throw new IllegalArgumentException("Credenciales invalidas");
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        String token = tokenService.emitirToken(usuario);

        return new LoginResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getNegocio().getIdNegocio(),
                usuario.getAvatarUrl(),
                token
        );
    }

    @Transactional
    public void cambiarPin(jakarta.servlet.http.HttpServletRequest httpRequest, CambiarPinRequest request) {
        Usuario usuario = accessControlService.requireAuthenticated(httpRequest);
        String pinActual = request.pinActual() == null ? "" : request.pinActual().trim();
        String pinNuevo = request.pinNuevo() == null ? "" : request.pinNuevo().trim();

        if (!passwordEncoder.matches(pinActual, usuario.getPinAcceso())) {
            throw new IllegalArgumentException("El pin actual es incorrecto");
        }
        if (pinNuevo.length() < 4) {
            throw new IllegalArgumentException("El nuevo pin debe tener al menos 4 caracteres");
        }

        usuario.setPinAcceso(passwordEncoder.encode(pinNuevo));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void logout(jakarta.servlet.http.HttpServletRequest httpRequest) {
        String token = accessControlService.extraerToken(httpRequest);
        if (token != null) {
            tokenService.revocarToken(token);
        }
    }
}
