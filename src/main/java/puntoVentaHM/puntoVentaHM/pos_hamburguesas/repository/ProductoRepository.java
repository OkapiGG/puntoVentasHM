package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaIdCategoriaAndActivoTrueOrderByNombreAsc(Long idCategoria);

    List<Producto> findByCategoriaNegocioIdNegocioOrderByNombreAsc(Long idNegocio);

    Optional<Producto> findByNombreIgnoreCase(String nombre);
}
