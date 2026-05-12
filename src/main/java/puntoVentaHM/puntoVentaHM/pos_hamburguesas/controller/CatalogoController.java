package puntoVentaHM.puntoVentaHM.pos_hamburguesas.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CategoriaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ProductoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.service.CatalogoService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final AccessControlService accessControlService;

    public CatalogoController(CatalogoService catalogoService, AccessControlService accessControlService) {
        this.catalogoService = catalogoService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/categorias")
    public List<CategoriaResponse> listarCategorias(HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.REPARTIDOR);
        return catalogoService.listarCategorias();
    }

    @GetMapping("/categorias/{slug}/productos")
    public List<ProductoResponse> listarProductosPorCategoria(@PathVariable String slug, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.REPARTIDOR);
        return catalogoService.listarProductosPorCategoriaSlug(slug);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }
}
