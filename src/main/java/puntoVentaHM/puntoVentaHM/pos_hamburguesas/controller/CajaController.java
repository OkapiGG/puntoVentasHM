package puntoVentaHM.puntoVentaHM.pos_hamburguesas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AperturaCajaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.BitacoraCajaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CajaResumenPeriodoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CajaResumenUsuarioResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CierreCajaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MovimientoCajaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.SesionCajaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.SesionCajaResumenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.service.CajaService;

@RestController
@RequestMapping("/api/caja")
public class CajaController {

    private final CajaService cajaService;
    private final AccessControlService accessControlService;

    public CajaController(CajaService cajaService, AccessControlService accessControlService) {
        this.cajaService = cajaService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/usuarios/{idUsuario}/sesion-actual")
    public ResponseEntity<SesionCajaResponse> obtenerSesionActual(@PathVariable Long idUsuario, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.COCINERO);
        SesionCajaResponse sesionCaja = cajaService.obtenerSesionActual(idUsuario);
        if (sesionCaja == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sesionCaja);
    }

    @GetMapping("/usuarios/{idUsuario}/historial")
    public java.util.List<SesionCajaResumenResponse> listarHistorial(@PathVariable Long idUsuario, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.COCINERO);
        return cajaService.listarHistorial(idUsuario);
    }

    @GetMapping("/sesiones/{idSesionCaja}/corte")
    public SesionCajaResponse obtenerCorte(@PathVariable Long idSesionCaja, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return cajaService.obtenerCorte(idSesionCaja);
    }

    @GetMapping("/negocios/{idNegocio}/resumen-periodo")
    public CajaResumenPeriodoResponse obtenerResumenPeriodo(@PathVariable Long idNegocio, @RequestParam(defaultValue = "DIARIO") String periodo, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return cajaService.obtenerResumenPeriodo(idNegocio, periodo);
    }

    @GetMapping("/negocios/{idNegocio}/resumen-usuarios")
    public java.util.List<CajaResumenUsuarioResponse> obtenerResumenUsuarios(@PathVariable Long idNegocio, @RequestParam(defaultValue = "DIARIO") String periodo, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return cajaService.obtenerResumenPorUsuario(idNegocio, periodo);
    }

    @GetMapping("/negocios/{idNegocio}/bitacora")
    public java.util.List<BitacoraCajaResponse> obtenerBitacora(@PathVariable Long idNegocio, @RequestParam(defaultValue = "DIARIO") String periodo, HttpServletRequest request) {
        accessControlService.requireAnyRole(request, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return cajaService.obtenerBitacora(idNegocio, periodo);
    }

    @PostMapping("/usuarios/{idUsuario}/apertura")
    public SesionCajaResponse abrirCaja(@PathVariable Long idUsuario, @RequestBody AperturaCajaRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.COCINERO);
        return cajaService.abrirCaja(idUsuario, request);
    }

    @PostMapping("/usuarios/{idUsuario}/movimientos")
    public SesionCajaResponse registrarMovimiento(@PathVariable Long idUsuario, @RequestBody MovimientoCajaRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return cajaService.registrarMovimiento(idUsuario, request);
    }

    @PostMapping("/usuarios/{idUsuario}/cierre")
    public SesionCajaResponse cerrarCaja(@PathVariable Long idUsuario, @RequestBody CierreCajaRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.COCINERO);
        return cajaService.cerrarCaja(idUsuario, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
