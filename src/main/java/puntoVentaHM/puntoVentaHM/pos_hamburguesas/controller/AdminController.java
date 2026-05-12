package puntoVentaHM.puntoVentaHM.pos_hamburguesas.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminCatalogoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminCategoriaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminCategoriaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminInsumoRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminModificadorRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminModificadorResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminProductoRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminProductoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminUsuarioRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminUsuarioResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InsumoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.service.AdminService;

@RestController
@RequestMapping("/api/admin/negocios/{idNegocio}")
public class AdminController {

    private final AdminService adminService;
    private final AccessControlService accessControlService;

    public AdminController(AdminService adminService, AccessControlService accessControlService) {
        this.adminService = adminService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/catalogo")
    public AdminCatalogoResponse obtenerCatalogo(@PathVariable Long idNegocio, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.obtenerCatalogo(idNegocio);
    }

    @PostMapping("/categorias")
    public AdminCategoriaResponse crearCategoria(@PathVariable Long idNegocio, @RequestBody AdminCategoriaRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.crearCategoria(idNegocio, body);
    }

    @PutMapping("/categorias/{idCategoria}")
    public AdminCategoriaResponse actualizarCategoria(@PathVariable Long idNegocio, @PathVariable Long idCategoria, @RequestBody AdminCategoriaRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.actualizarCategoria(idNegocio, idCategoria, body);
    }

    @DeleteMapping("/categorias/{idCategoria}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long idNegocio, @PathVariable Long idCategoria, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        adminService.eliminarCategoria(idNegocio, idCategoria);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/productos")
    public AdminProductoResponse crearProducto(@PathVariable Long idNegocio, @RequestBody AdminProductoRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.crearProducto(idNegocio, body);
    }

    @PutMapping("/productos/{idProducto}")
    public AdminProductoResponse actualizarProducto(@PathVariable Long idNegocio, @PathVariable Long idProducto, @RequestBody AdminProductoRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.actualizarProducto(idNegocio, idProducto, body);
    }

    @DeleteMapping("/productos/{idProducto}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long idNegocio, @PathVariable Long idProducto, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        adminService.eliminarProducto(idNegocio, idProducto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/modificadores")
    public AdminModificadorResponse crearModificador(@PathVariable Long idNegocio, @RequestBody AdminModificadorRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.crearModificador(idNegocio, body);
    }

    @PutMapping("/modificadores/{idModificador}")
    public AdminModificadorResponse actualizarModificador(@PathVariable Long idNegocio, @PathVariable Long idModificador, @RequestBody AdminModificadorRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.actualizarModificador(idNegocio, idModificador, body);
    }

    @DeleteMapping("/modificadores/{idModificador}")
    public ResponseEntity<Void> eliminarModificador(@PathVariable Long idNegocio, @PathVariable Long idModificador, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        adminService.eliminarModificador(idNegocio, idModificador);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/insumos")
    public InsumoResponse crearInsumo(@PathVariable Long idNegocio, @RequestBody AdminInsumoRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.crearInsumo(idNegocio, body);
    }

    @PutMapping("/insumos/{idInsumo}")
    public InsumoResponse actualizarInsumo(@PathVariable Long idNegocio, @PathVariable Long idInsumo, @RequestBody AdminInsumoRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.actualizarInsumo(idNegocio, idInsumo, body);
    }

    @DeleteMapping("/insumos/{idInsumo}")
    public ResponseEntity<Void> eliminarInsumo(@PathVariable Long idNegocio, @PathVariable Long idInsumo, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        adminService.eliminarInsumo(idNegocio, idInsumo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuarios")
    public AdminUsuarioResponse crearUsuario(@PathVariable Long idNegocio, @RequestBody AdminUsuarioRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.crearUsuario(idNegocio, body);
    }

    @PutMapping("/usuarios/{idUsuario}")
    public AdminUsuarioResponse actualizarUsuario(@PathVariable Long idNegocio, @PathVariable Long idUsuario, @RequestBody AdminUsuarioRequest body, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        return adminService.actualizarUsuario(idNegocio, idUsuario, body);
    }

    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long idNegocio, @PathVariable Long idUsuario, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE);
        adminService.eliminarUsuario(idNegocio, idUsuario);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
