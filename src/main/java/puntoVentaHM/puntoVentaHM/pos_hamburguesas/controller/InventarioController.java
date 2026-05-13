package puntoVentaHM.puntoVentaHM.pos_hamburguesas.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InsumoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InventarioModificadorCostoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InventarioProductoCostoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MovimientoInventarioRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MovimientoInventarioResponse;
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

    @GetMapping("/negocios/{idNegocio}/movimientos")
    public List<MovimientoInventarioResponse> listarMovimientos(@PathVariable Long idNegocio, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return inventarioService.listarMovimientos(idNegocio);
    }

    @PostMapping("/negocios/{idNegocio}/insumos/{idInsumo}/movimientos")
    public MovimientoInventarioResponse registrarMovimiento(
            @PathVariable Long idNegocio,
            @PathVariable Long idInsumo,
            @RequestBody MovimientoInventarioRequest movimiento,
            HttpServletRequest request
    ) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return inventarioService.registrarMovimiento(idNegocio, idInsumo, movimiento);
    }

    @GetMapping("/negocios/{idNegocio}/costos-producto")
    public List<InventarioProductoCostoResponse> listarCostosProducto(@PathVariable Long idNegocio, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return inventarioService.listarCostosProducto(idNegocio);
    }

    @GetMapping("/negocios/{idNegocio}/costos-modificador")
    public List<InventarioModificadorCostoResponse> listarCostosModificador(@PathVariable Long idNegocio, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return inventarioService.listarCostosModificador(idNegocio);
    }
}
