package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Promocion;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    List<Promocion> findByNegocioIdNegocioOrderByNombreAsc(Long idNegocio);

    List<Promocion> findByNegocioIdNegocioAndActivaTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByNombreAsc(
            Long idNegocio,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
}
