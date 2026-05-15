package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InsumoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InventarioModificadorCostoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InventarioProductoCostoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MovimientoInventarioRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.MovimientoInventarioResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.RecetaItemResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Insumo;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.MovimientoInventario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.InsumoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ModificadorRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.MovimientoInventarioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ProductoRepository;

@Service
public class InventarioService {

    private final InsumoRepository insumoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;
    private final ModificadorRepository modificadorRepository;

    public InventarioService(
            InsumoRepository insumoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            ProductoRepository productoRepository,
            ModificadorRepository modificadorRepository
    ) {
        this.insumoRepository = insumoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
        this.modificadorRepository = modificadorRepository;
    }

    @Transactional(readOnly = true)
    public List<InsumoResponse> listarInsumosPorNegocio(Long idNegocio) {
        return insumoRepository.findByNegocioIdNegocioOrderByNombreAsc(idNegocio).stream()
                .map(this::mapInsumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> listarMovimientos(Long idNegocio) {
        return movimientoInventarioRepository.findByInsumoNegocioIdNegocioOrderByFechaDesc(idNegocio).stream()
                .map(this::mapMovimiento)
                .toList();
    }

    @Transactional
    public MovimientoInventarioResponse registrarMovimiento(Long idNegocio, Long idInsumo, MovimientoInventarioRequest request) {
        Insumo insumo = insumoRepository.findById(idInsumo)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));
        if (!insumo.getNegocio().getIdNegocio().equals(idNegocio)) {
            throw new IllegalArgumentException("El insumo no pertenece al negocio");
        }
        if (request == null || request.cantidad() == null || request.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser mayor a cero");
        }
        String tipo = request.tipo() == null ? "" : request.tipo().trim().toUpperCase();
        if (!List.of("ENTRADA_MANUAL", "MERMA", "AJUSTE").contains(tipo)) {
            throw new IllegalArgumentException("Tipo de movimiento de inventario inválido");
        }
        BigDecimal stockAnterior = insumo.getStockActual();
        BigDecimal stockResultante = switch (tipo) {
            case "ENTRADA_MANUAL" -> stockAnterior.add(request.cantidad());
            case "MERMA" -> stockAnterior.subtract(request.cantidad());
            case "AJUSTE" -> request.cantidad();
            default -> stockAnterior;
        };
        if (stockResultante.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El movimiento dejaría el stock en negativo");
        }
        insumo.setStockActual(stockResultante);
        insumoRepository.save(insumo);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setInsumo(insumo);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(request.cantidad());
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockResultante(stockResultante);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setMotivo(request.motivo());
        movimiento.setReferencia("Manual");
        return mapMovimiento(movimientoInventarioRepository.save(movimiento));
    }

    @Transactional(readOnly = true)
    public List<InventarioProductoCostoResponse> listarCostosProducto(Long idNegocio) {
        return productoRepository.findByCategoriaNegocioIdNegocioOrderByNombreAsc(idNegocio).stream()
                .map(this::mapCostoProducto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioModificadorCostoResponse> listarCostosModificador(Long idNegocio) {
        return modificadorRepository.findByProductoCategoriaNegocioIdNegocioOrderByNombreAsc(idNegocio).stream()
                .map(this::mapCostoModificador)
                .toList();
    }

    public void registrarSalidaVenta(Insumo insumo, BigDecimal cantidad, String referencia) {
        BigDecimal stockAnterior = insumo.getStockActual();
        BigDecimal stockResultante = stockAnterior.subtract(cantidad);
        insumo.setStockActual(stockResultante);
        insumoRepository.save(insumo);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setInsumo(insumo);
        movimiento.setTipo("SALIDA_VENTA");
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockResultante(stockResultante);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setMotivo("Descuento por venta");
        movimiento.setReferencia(referencia);
        movimientoInventarioRepository.save(movimiento);
    }

    public void registrarReversionCancelacion(Insumo insumo, BigDecimal cantidad, String referencia) {
        BigDecimal stockAnterior = insumo.getStockActual();
        BigDecimal stockResultante = stockAnterior.add(cantidad);
        insumo.setStockActual(stockResultante);
        insumoRepository.save(insumo);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setInsumo(insumo);
        movimiento.setTipo("REVERSION_CANCELACION");
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockResultante(stockResultante);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setMotivo("Reversión por cancelación de orden");
        movimiento.setReferencia(referencia);
        movimientoInventarioRepository.save(movimiento);
    }

    private InsumoResponse mapInsumo(Insumo insumo) {
        BigDecimal stockMinimo = insumo.getStockMinimo() == null ? BigDecimal.ZERO : insumo.getStockMinimo();
        return new InsumoResponse(
                insumo.getIdInsumo(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                insumo.getStockActual(),
                stockMinimo,
                insumo.getCostoUnitario() == null ? BigDecimal.ZERO : insumo.getCostoUnitario(),
                insumo.getStockActual().compareTo(stockMinimo) <= 0,
                insumo.getActivo()
        );
    }

    private MovimientoInventarioResponse mapMovimiento(MovimientoInventario movimiento) {
        return new MovimientoInventarioResponse(
                movimiento.getIdMovimientoInventario(),
                movimiento.getInsumo().getIdInsumo(),
                movimiento.getInsumo().getNombre(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockResultante(),
                movimiento.getFecha(),
                movimiento.getMotivo(),
                movimiento.getReferencia()
        );
    }

    private InventarioProductoCostoResponse mapCostoProducto(Producto producto) {
        List<RecetaItemResponse> receta = producto.getReceta().stream()
                .map(item -> new RecetaItemResponse(
                        item.getInsumo().getIdInsumo(),
                        item.getInsumo().getNombre(),
                        item.getInsumo().getUnidadMedida(),
                        item.getCantidad(),
                        costo(item.getCantidad(), item.getInsumo().getCostoUnitario())
                ))
                .toList();
        return new InventarioProductoCostoResponse(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getCategoria().getNombre(),
                producto.getPrecio(),
                receta.stream().map(RecetaItemResponse::costoEstimado).reduce(BigDecimal.ZERO, BigDecimal::add),
                receta
        );
    }

    private InventarioModificadorCostoResponse mapCostoModificador(Modificador modificador) {
        List<RecetaItemResponse> receta = modificador.getReceta().stream()
                .map(item -> new RecetaItemResponse(
                        item.getInsumo().getIdInsumo(),
                        item.getInsumo().getNombre(),
                        item.getInsumo().getUnidadMedida(),
                        item.getCantidad(),
                        costo(item.getCantidad(), item.getInsumo().getCostoUnitario())
                ))
                .toList();
        return new InventarioModificadorCostoResponse(
                modificador.getIdModificador(),
                modificador.getNombre(),
                modificador.getProducto().getNombre(),
                modificador.getPrecioExtra(),
                receta.stream().map(RecetaItemResponse::costoEstimado).reduce(BigDecimal.ZERO, BigDecimal::add),
                receta
        );
    }

    private BigDecimal costo(BigDecimal cantidad, BigDecimal costoUnitario) {
        return (cantidad == null ? BigDecimal.ZERO : cantidad)
                .multiply(costoUnitario == null ? BigDecimal.ZERO : costoUnitario);
    }
}
