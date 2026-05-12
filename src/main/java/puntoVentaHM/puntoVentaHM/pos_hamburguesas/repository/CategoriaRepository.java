package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNombreAsc();

    List<Categoria> findByNegocioIdNegocioOrderByNombreAsc(Long idNegocio);
}
