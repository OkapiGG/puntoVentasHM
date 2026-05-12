package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.MovimientoInventario;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByInsumoNegocioIdNegocioOrderByFechaDesc(Long idNegocio);

    List<MovimientoInventario> findByInsumoIdInsumoOrderByFechaDesc(Long idInsumo);
}
