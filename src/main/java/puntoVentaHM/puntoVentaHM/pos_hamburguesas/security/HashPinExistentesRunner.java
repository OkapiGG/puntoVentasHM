package puntoVentaHM.puntoVentaHM.pos_hamburguesas.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;

/**
 * Migra PINs almacenados en texto plano a BCrypt en el primer arranque tras
 * habilitar la seguridad. Idempotente: si ya están hasheados, no hace nada.
 */
@Configuration
public class HashPinExistentesRunner {

    private static final Logger LOG = LoggerFactory.getLogger(HashPinExistentesRunner.class);

    @Bean
    public ApplicationRunner migrarPinsAplanoBcryptRunner(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> migrar(usuarioRepository, passwordEncoder);
    }

    @Transactional
    void migrar(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        int migrados = 0;
        for (Usuario usuario : usuarioRepository.findAll()) {
            String pin = usuario.getPinAcceso();
            if (pin == null || pin.isBlank()) {
                continue;
            }
            if (esBCrypt(pin)) {
                continue;
            }
            usuario.setPinAcceso(passwordEncoder.encode(pin));
            usuarioRepository.save(usuario);
            migrados++;
        }
        if (migrados > 0) {
            LOG.info("Migrados {} PINs a BCrypt", migrados);
        }
    }

    private boolean esBCrypt(String valor) {
        return valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$");
    }
}
