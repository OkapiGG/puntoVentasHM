package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findTopByOrdenIdOrdenOrderByIdPagoDesc(Long idOrden);
}
