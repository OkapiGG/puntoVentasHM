package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.SesionUsuario;

public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {

    @Query("SELECT s FROM SesionUsuario s JOIN FETCH s.usuario u JOIN FETCH u.negocio WHERE s.tokenHash = :tokenHash")
    Optional<SesionUsuario> findByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("DELETE FROM SesionUsuario s WHERE s.tokenHash = :tokenHash")
    void deleteByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("DELETE FROM SesionUsuario s WHERE s.expiraEn < :ahora")
    void deleteExpiradas(@Param("ahora") LocalDateTime ahora);
}
