package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.LoginRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.LoginResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
}
