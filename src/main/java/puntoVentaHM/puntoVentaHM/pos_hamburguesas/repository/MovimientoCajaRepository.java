package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.MovimientoCaja;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findBySesionCajaIdSesionCajaOrderByFechaDesc(Long idSesionCaja);
}
