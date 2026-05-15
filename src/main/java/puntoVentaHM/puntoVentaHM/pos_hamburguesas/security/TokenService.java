package puntoVentaHM.puntoVentaHM.pos_hamburguesas.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.SesionUsuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.SesionUsuarioRepository;

@Service
public class TokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final SesionUsuarioRepository sesionUsuarioRepository;
    private final long duracionHoras;

    public TokenService(
            SesionUsuarioRepository sesionUsuarioRepository,
            @Value("${app.seguridad.token-duracion-horas:12}") long duracionHoras
    ) {
        this.sesionUsuarioRepository = sesionUsuarioRepository;
        this.duracionHoras = duracionHoras;
    }

    @Transactional
    public String emitirToken(Usuario usuario) {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        String tokenPlano = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = hash(tokenPlano);

        SesionUsuario sesion = new SesionUsuario();
        sesion.setUsuario(usuario);
        sesion.setTokenHash(hash);
        LocalDateTime ahora = LocalDateTime.now();
        sesion.setCreadaEn(ahora);
        sesion.setUltimoUso(ahora);
        sesion.setExpiraEn(ahora.plusHours(duracionHoras));
        sesionUsuarioRepository.save(sesion);

        return tokenPlano;
    }

    @Transactional
    public SesionUsuario validarToken(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isBlank()) {
            throw new UnauthorizedAccessException("Token de sesión ausente");
        }
        String hash = hash(tokenPlano);
        SesionUsuario sesion = sesionUsuarioRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedAccessException("Token de sesión inválido"));

        LocalDateTime ahora = LocalDateTime.now();
        if (sesion.getExpiraEn().isBefore(ahora)) {
            sesionUsuarioRepository.delete(sesion);
            throw new UnauthorizedAccessException("Sesión expirada");
        }
        sesion.setUltimoUso(ahora);
        return sesion;
    }

    @Transactional
    public void revocarToken(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isBlank()) {
            return;
        }
        sesionUsuarioRepository.deleteByTokenHash(hash(tokenPlano));
    }

    @Transactional
    public void limpiarExpiradas() {
        sesionUsuarioRepository.deleteExpiradas(LocalDateTime.now());
    }

    private String hash(String tokenPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
