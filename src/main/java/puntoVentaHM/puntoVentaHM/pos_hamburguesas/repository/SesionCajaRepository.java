package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.SesionCaja;

public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    Optional<SesionCaja> findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(Long idUsuario, String estado);

    Optional<SesionCaja> findTopByUsuarioIdUsuarioOrderByAperturaDesc(Long idUsuario);

    List<SesionCaja> findByUsuarioIdUsuarioOrderByAperturaDesc(Long idUsuario);

    List<SesionCaja> findByUsuarioNegocioIdNegocioAndAperturaBetweenOrderByAperturaDesc(Long idNegocio, LocalDateTime inicio, LocalDateTime fin);
}
