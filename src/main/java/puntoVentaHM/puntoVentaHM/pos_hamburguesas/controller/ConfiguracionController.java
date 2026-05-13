package puntoVentaHM.puntoVentaHM.pos_hamburguesas.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarNegocioConfigRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.NegocioConfigResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.service.ConfiguracionService;

@RestController
@RequestMapping("/api/configuracion/negocios/{idNegocio}")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;
    private final AccessControlService accessControlService;

    public ConfiguracionController(
            ConfiguracionService configuracionService,
            AccessControlService accessControlService
    ) {
        this.configuracionService = configuracionService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public NegocioConfigResponse obtenerConfiguracion(@PathVariable Long idNegocio, HttpServletRequest request) {
        accessControlService.requireAuthenticated(request);
        return configuracionService.obtenerConfiguracion(idNegocio);
    }

    @PutMapping
    public NegocioConfigResponse actualizarConfiguracion(
            @PathVariable Long idNegocio,
            @RequestBody ActualizarNegocioConfigRequest body,
            HttpServletRequest request
    ) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return configuracionService.actualizarConfiguracion(idNegocio, body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
