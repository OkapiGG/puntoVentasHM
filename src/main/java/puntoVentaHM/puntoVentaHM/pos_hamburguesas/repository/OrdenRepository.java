package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Orden;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Orden o WHERE o.idOrden = :idOrden")
    Optional<Orden> findByIdConLock(@Param("idOrden") Long idOrden);

    List<Orden> findBySesionCajaUsuarioIdUsuarioAndEstadoOrderByFechaDesc(Long idUsuario, String estado);

    List<Orden> findBySesionCajaUsuarioIdUsuarioAndEstadoInOrderByFechaDesc(Long idUsuario, Collection<String> estados);

    List<Orden> findBySesionCajaUsuarioIdUsuarioOrderByFechaDesc(Long idUsuario);

    List<Orden> findBySesionCajaUsuarioIdUsuarioAndFechaOperacionOrderByFechaDesc(Long idUsuario, LocalDate fechaOperacion);

    List<Orden> findBySesionCajaUsuarioNegocioIdNegocioOrderByFechaDesc(Long idNegocio);

    List<Orden> findBySesionCajaUsuarioNegocioIdNegocioAndFechaBetweenOrderByFechaDesc(Long idNegocio, LocalDateTime inicio, LocalDateTime fin);

    List<Orden> findBySesionCajaUsuarioNegocioIdNegocioAndFechaCancelacionBetweenOrderByFechaCancelacionDesc(Long idNegocio, java.time.LocalDateTime inicio, java.time.LocalDateTime fin);

    long countBySesionCajaUsuarioNegocioIdNegocioAndFechaOperacion(Long idNegocio, LocalDate fechaOperacion);
}
