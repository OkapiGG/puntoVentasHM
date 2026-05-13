package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Orden;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findBySesionCajaUsuarioIdUsuarioAndEstadoOrderByFechaDesc(Long idUsuario, String estado);

    List<Orden> findBySesionCajaUsuarioIdUsuarioAndEstadoInOrderByFechaDesc(Long idUsuario, Collection<String> estados);

    List<Orden> findBySesionCajaUsuarioIdUsuarioOrderByFechaDesc(Long idUsuario);

    List<Orden> findBySesionCajaUsuarioIdUsuarioAndFechaOperacionOrderByFechaDesc(Long idUsuario, LocalDate fechaOperacion);

    List<Orden> findBySesionCajaUsuarioNegocioIdNegocioAndFechaCancelacionBetweenOrderByFechaCancelacionDesc(Long idNegocio, java.time.LocalDateTime inicio, java.time.LocalDateTime fin);

    long countBySesionCajaUsuarioNegocioIdNegocioAndFechaOperacion(Long idNegocio, LocalDate fechaOperacion);
}
