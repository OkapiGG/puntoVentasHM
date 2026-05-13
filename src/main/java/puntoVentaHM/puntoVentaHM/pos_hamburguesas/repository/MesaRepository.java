package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findByNegocioIdNegocioOrderByNumeroAsc(Long idNegocio);
    Optional<Mesa> findByNegocioIdNegocioAndNumero(Long idNegocio, Integer numero);
}
