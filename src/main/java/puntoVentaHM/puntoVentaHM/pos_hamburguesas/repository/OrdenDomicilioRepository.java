package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.OrdenDomicilio;

public interface OrdenDomicilioRepository extends JpaRepository<OrdenDomicilio, Long> {

    Optional<OrdenDomicilio> findByOrdenIdOrden(Long idOrden);

    List<OrdenDomicilio> findByOrdenSesionCajaUsuarioNegocioIdNegocioAndOrdenEstadoInOrderByOrdenFechaDesc(Long idNegocio, Collection<String> estados);

    List<OrdenDomicilio> findByRepartidorIdUsuarioAndOrdenEstadoInOrderByOrdenFechaDesc(Long idRepartidor, Collection<String> estados);
}
