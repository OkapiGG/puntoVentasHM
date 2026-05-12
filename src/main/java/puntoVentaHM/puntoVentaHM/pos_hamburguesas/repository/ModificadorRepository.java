package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;

public interface ModificadorRepository extends JpaRepository<Modificador, Long> {

    List<Modificador> findByProductoIdProductoAndActivoTrueOrderByNombreAsc(Long idProducto);

    List<Modificador> findByProductoCategoriaNegocioIdNegocioOrderByNombreAsc(Long idNegocio);
}
