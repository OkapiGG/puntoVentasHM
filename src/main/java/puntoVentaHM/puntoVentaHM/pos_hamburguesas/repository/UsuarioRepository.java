package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreIgnoreCase(String nombre);

    List<Usuario> findByNegocioIdNegocioOrderByNombreAsc(Long idNegocio);

    List<Usuario> findByNegocioIdNegocioAndRolIgnoreCaseAndActivoTrueOrderByNombreAsc(Long idNegocio, String rol);

    List<Usuario> findByNegocioIdNegocioAndRolInAndActivoTrue(Long idNegocio, List<String> roles);
}
