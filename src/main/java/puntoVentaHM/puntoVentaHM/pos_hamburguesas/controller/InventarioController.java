package puntoVentaHM.puntoVentaHM.pos_hamburguesas.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InsumoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.service.InventarioService;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final AccessControlService accessControlService;

    public InventarioController(InventarioService inventarioService, AccessControlService accessControlService) {
        this.inventarioService = inventarioService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/negocios/{idNegocio}/insumos")
    public List<InsumoResponse> listarInsumos(@PathVariable Long idNegocio, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return inventarioService.listarInsumosPorNegocio(idNegocio);
    }
}
