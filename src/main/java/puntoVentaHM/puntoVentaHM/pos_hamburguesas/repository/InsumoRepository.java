package puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Insumo;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    List<Insumo> findByNegocioIdNegocioOrderByNombreAsc(Long idNegocio);

    Optional<Insumo> findByNegocioIdNegocioAndNombreIgnoreCase(Long idNegocio, String nombre);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Insumo i WHERE i.idInsumo = :idInsumo")
    Optional<Insumo> findByIdConLock(@Param("idInsumo") Long idInsumo);
}
