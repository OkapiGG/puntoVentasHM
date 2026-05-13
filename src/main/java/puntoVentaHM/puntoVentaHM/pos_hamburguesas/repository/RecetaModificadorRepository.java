package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaModificador;

public interface RecetaModificadorRepository extends JpaRepository<RecetaModificador, Long> {

    List<RecetaModificador> findByModificadorIdModificador(Long idModificador);

    void deleteByModificadorIdModificador(Long idModificador);
}
