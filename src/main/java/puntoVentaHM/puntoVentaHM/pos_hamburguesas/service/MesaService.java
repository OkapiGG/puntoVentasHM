package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarMesaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MesaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Mesa;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.MesaRepository;

@Service
public class MesaService {

    private static final List<String> ESTADOS_VALIDOS = List.of("LIBRE", "OCUPADA", "RESERVADA", "LIMPIEZA");

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    @Transactional(readOnly = true)
    public List<MesaResponse> listarMesas(Long idNegocio) {
        return mesaRepository.findByNegocioIdNegocioOrderByNumeroAsc(idNegocio).stream()
                .map(this::mapMesa)
                .toList();
    }

    @Transactional
    public MesaResponse actualizarMesa(Long idNegocio, Long idMesa, ActualizarMesaRequest request) {
        Mesa mesa = mesaRepository.findById(idMesa)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));
        if (!mesa.getNegocio().getIdNegocio().equals(idNegocio)) {
            throw new IllegalArgumentException("La mesa no pertenece al negocio");
        }

        String estado = normalizarEstado(request.estado());
        mesa.setEstado(estado);
        mesa.setMeseroAsignado(textoOpcional(request.meseroAsignado()));
        mesa.setReferenciaOrden(textoOpcional(request.referenciaOrden()));

        if ("LIBRE".equals(estado)) {
            mesa.setMeseroAsignado(null);
            mesa.setReferenciaOrden(null);
        }

        return mapMesa(mesaRepository.save(mesa));
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.trim().isBlank()) {
            throw new IllegalArgumentException("El estado de la mesa es obligatorio");
        }
        String valor = estado.trim().toUpperCase();
        if (!ESTADOS_VALIDOS.contains(valor)) {
            throw new IllegalArgumentException("Estado de mesa invalido");
        }
        return valor;
    }

    private String textoOpcional(String valor) {
        if (valor == null || valor.trim().isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private MesaResponse mapMesa(Mesa mesa) {
        return new MesaResponse(
                mesa.getIdMesa(),
                mesa.getNumero(),
                mesa.getEstado(),
                mesa.getMeseroAsignado(),
                mesa.getReferenciaOrden()
        );
    }
}
