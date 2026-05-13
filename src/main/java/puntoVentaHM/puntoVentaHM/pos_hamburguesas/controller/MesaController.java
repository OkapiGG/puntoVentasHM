package puntoVentaHM.puntoVentaHM.pos_hamburguesas.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarMesaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MesaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.service.MesaService;

@RestController
@RequestMapping("/api/mesas/negocios/{idNegocio}")
public class MesaController {

    private final MesaService mesaService;
    private final AccessControlService accessControlService;

    public MesaController(MesaService mesaService, AccessControlService accessControlService) {
        this.mesaService = mesaService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public List<MesaResponse> listarMesas(@PathVariable Long idNegocio, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return mesaService.listarMesas(idNegocio);
    }

    @PutMapping("/{idMesa}")
    public MesaResponse actualizarMesa(
            @PathVariable Long idNegocio,
            @PathVariable Long idMesa,
            @RequestBody ActualizarMesaRequest body,
            HttpServletRequest request
    ) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return mesaService.actualizarMesa(idNegocio, idMesa, body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
