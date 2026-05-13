package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaProducto;
import java.util.List;

public interface RecetaProductoRepository extends JpaRepository<RecetaProducto, Long> {
    List<RecetaProducto> findByProductoIdProducto(Long idProducto);

    void deleteByProductoIdProducto(Long idProducto);
}
