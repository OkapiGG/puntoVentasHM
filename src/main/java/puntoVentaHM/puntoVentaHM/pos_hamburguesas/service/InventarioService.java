package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InsumoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.InsumoRepository;

@Service
public class InventarioService {

    private final InsumoRepository insumoRepository;

    public InventarioService(InsumoRepository insumoRepository) {
        this.insumoRepository = insumoRepository;
    }

    @Transactional(readOnly = true)
    public List<InsumoResponse> listarInsumosPorNegocio(Long idNegocio) {
        return insumoRepository.findByNegocioIdNegocioOrderByNombreAsc(idNegocio).stream()
                .map(insumo -> new InsumoResponse(
                        insumo.getIdInsumo(),
                        insumo.getNombre(),
                        insumo.getUnidadMedida(),
                        insumo.getStockActual(),
                        insumo.getActivo()
                ))
                .toList();
    }
}
