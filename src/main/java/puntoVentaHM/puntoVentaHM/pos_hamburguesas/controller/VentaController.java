package puntoVentaHM.puntoVentaHM.pos_hamburguesas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CancelacionOrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CancelarOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CotizacionOrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CrearOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarEstadoDomicilioRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ActualizarEstadoOrdenRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AsignarRepartidorRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.HistorialVentaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenActivaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.PagoRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.PagoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.RepartidorOptionResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.TicketResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.VentasDiaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.AccessControlService;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.service.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final AccessControlService accessControlService;

    public VentaController(VentaService ventaService, AccessControlService accessControlService) {
        this.ventaService = ventaService;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/ordenes")
    public OrdenResponse crearOrden(@RequestBody CrearOrdenRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.crearOrden(request);
    }

    @PostMapping("/ordenes/cotizacion")
    public CotizacionOrdenResponse cotizarOrden(@RequestBody CrearOrdenRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.cotizarOrden(request);
    }

    @PostMapping("/ordenes/{idOrden}/cancelacion")
    public CancelacionOrdenResponse cancelarOrden(@PathVariable Long idOrden, @RequestBody CancelarOrdenRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.cancelarOrden(idOrden, request);
    }

    @PostMapping("/ordenes/{idOrden}/estado")
    public OrdenActivaResponse actualizarEstadoOrden(@PathVariable Long idOrden, @RequestBody ActualizarEstadoOrdenRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.REPARTIDOR);
        return ventaService.actualizarEstadoOrden(idOrden, request);
    }

    @PostMapping("/ordenes/{idOrden}/domicilio/estado")
    public OrdenActivaResponse actualizarEstadoDomicilio(@PathVariable Long idOrden, @RequestBody ActualizarEstadoDomicilioRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.REPARTIDOR);
        return ventaService.actualizarEstadoDomicilio(idOrden, request);
    }

    @PostMapping("/ordenes/{idOrden}/domicilio/asignacion")
    public OrdenActivaResponse asignarRepartidor(@PathVariable Long idOrden, @RequestBody AsignarRepartidorRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.asignarRepartidor(idOrden, request);
    }

    @PostMapping("/ordenes/{idOrden}/pago")
    public PagoResponse registrarPago(@PathVariable Long idOrden, @RequestBody PagoRequest request, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.registrarPago(idOrden, request);
    }

    @GetMapping("/ordenes/{idOrden}/ticket")
    public TicketResponse obtenerTicket(@PathVariable Long idOrden, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.obtenerTicket(idOrden);
    }

    @GetMapping("/usuarios/{idUsuario}/historial")
    public java.util.List<HistorialVentaResponse> listarHistorial(@PathVariable Long idUsuario, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.listarVentasPagadasPorUsuario(idUsuario);
    }

    @GetMapping("/usuarios/{idUsuario}/activas")
    public java.util.List<OrdenActivaResponse> listarActivas(@PathVariable Long idUsuario, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.REPARTIDOR);
        return ventaService.listarOrdenesActivasPorUsuario(idUsuario);
    }

    @GetMapping("/usuarios/{idUsuario}/resumen-hoy")
    public VentasDiaResponse obtenerResumenHoy(@PathVariable Long idUsuario, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.obtenerVentasDelDia(idUsuario);
    }

    @GetMapping("/negocios/{idNegocio}/domicilios-activos")
    public java.util.List<OrdenActivaResponse> listarDomiciliosActivos(@PathVariable Long idNegocio, HttpServletRequest httpRequest) {
        var usuario = accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO, RolSistema.REPARTIDOR);
        if (RolSistema.REPARTIDOR.name().equalsIgnoreCase(usuario.getRol())) {
            return ventaService.listarColaDomiciliosPorRepartidor(usuario.getIdUsuario());
        }
        return ventaService.listarColaDomiciliosPorNegocio(idNegocio);
    }

    @GetMapping("/negocios/{idNegocio}/repartidores")
    public java.util.List<RepartidorOptionResponse> listarRepartidores(@PathVariable Long idNegocio, HttpServletRequest httpRequest) {
        accessControlService.requireAnyRole(httpRequest, RolSistema.ADMIN, RolSistema.GERENTE, RolSistema.CAJERO);
        return ventaService.listarRepartidoresPorNegocio(idNegocio);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
