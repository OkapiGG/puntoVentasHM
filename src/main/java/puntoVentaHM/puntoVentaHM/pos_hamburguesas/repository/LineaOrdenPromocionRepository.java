package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.LineaOrdenPromocion;

public interface LineaOrdenPromocionRepository extends JpaRepository<LineaOrdenPromocion, Long> {
}
