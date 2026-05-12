package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.LineaOrden;

public interface LineaOrdenRepository extends JpaRepository<LineaOrden, Long> {
}
