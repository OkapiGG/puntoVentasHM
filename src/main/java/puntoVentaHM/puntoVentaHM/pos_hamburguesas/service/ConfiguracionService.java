package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarNegocioConfigRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.NegocioConfigResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Negocio;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.NegocioRepository;

@Service
public class ConfiguracionService {

    private final NegocioRepository negocioRepository;

    public ConfiguracionService(NegocioRepository negocioRepository) {
        this.negocioRepository = negocioRepository;
    }

    @Transactional(readOnly = true)
    public NegocioConfigResponse obtenerConfiguracion(Long idNegocio) {
        return mapConfig(obtenerNegocio(idNegocio));
    }

    @Transactional
    public NegocioConfigResponse actualizarConfiguracion(Long idNegocio, ActualizarNegocioConfigRequest request) {
        Negocio negocio = obtenerNegocio(idNegocio);
        negocio.setNombre(validarTexto(request.nombre(), "El nombre del negocio es obligatorio"));
        negocio.setLimiteCaja(validarNoNegativo(request.limiteCaja(), "El limite de caja es invalido"));
        negocio.setCostoEnvioDefault(validarNoNegativo(request.costoEnvioDefault(), "El costo de envio es invalido"));
        negocio.setAlertasCaja(request.alertasCaja() == null ? Boolean.TRUE : request.alertasCaja());
        negocio.setNotificaNuevosPedidos(
                request.notificaNuevosPedidos() == null ? Boolean.TRUE : request.notificaNuevosPedidos());
        negocio.setNotificaReportesDiarios(
                request.notificaReportesDiarios() == null ? Boolean.FALSE : request.notificaReportesDiarios());
        return mapConfig(negocioRepository.save(negocio));
    }

    private Negocio obtenerNegocio(Long idNegocio) {
        return negocioRepository.findById(idNegocio)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
    }

    private String validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    private BigDecimal validarNoNegativo(BigDecimal valor, String mensaje) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    private NegocioConfigResponse mapConfig(Negocio negocio) {
        return new NegocioConfigResponse(
                negocio.getIdNegocio(),
                negocio.getNombre(),
                negocio.getLimiteCaja(),
                negocio.getCostoEnvioDefault(),
                negocio.getAlertasCaja(),
                negocio.getNotificaNuevosPedidos(),
                negocio.getNotificaReportesDiarios()
        );
    }
}
