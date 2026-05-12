package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaProducto;

public interface RecetaProductoRepository extends JpaRepository<RecetaProducto, Long> {
}
