package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Insumo;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    List<Insumo> findByNegocioIdNegocioOrderByNombreAsc(Long idNegocio);

    Optional<Insumo> findByNegocioIdNegocioAndNombreIgnoreCase(Long idNegocio, String nombre);
}
