package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.LineaOrdenMod;

public interface LineaOrdenModRepository extends JpaRepository<LineaOrdenMod, Long> {
}
