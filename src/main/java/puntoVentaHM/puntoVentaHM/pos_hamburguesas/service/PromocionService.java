package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemModRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.OrdenItemPromocionResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.PromocionEtiquetaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Promocion;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ModificadorRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.PromocionRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ProductoRepository;

@Service
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final ProductoRepository productoRepository;
    private final ModificadorRepository modificadorRepository;

    public PromocionService(
            PromocionRepository promocionRepository,
            ProductoRepository productoRepository,
            ModificadorRepository modificadorRepository
    ) {
        this.promocionRepository = promocionRepository;
        this.productoRepository = productoRepository;
        this.modificadorRepository = modificadorRepository;
    }

    @Transactional(readOnly = true)
    public List<Promocion> listarPromocionesActivas(Long idNegocio, LocalDateTime fechaHora) {
        return promocionRepository
                .findByNegocioIdNegocioAndActivaTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqualOrderByNombreAsc(
                        idNegocio,
                        fechaHora.toLocalDate(),
                        fechaHora.toLocalDate()
                ).stream()
                .filter(promocion -> horarioAplica(promocion, fechaHora))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromocionEtiquetaResponse> listarEtiquetasProducto(Long idNegocio, Producto producto, LocalDateTime fechaHora) {
        return listarPromocionesActivas(idNegocio, fechaHora).stream()
                .filter(promocion -> aplicaAProducto(promocion, producto))
                .map(this::toEtiqueta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResultadoPromocion cotizar(Long idNegocio, List<OrdenItemRequest> itemsRequest) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Promocion> promociones = listarPromocionesActivas(idNegocio, ahora);
        List<LineaCotizacion> lineas = construirLineas(itemsRequest);

        for (Promocion promocion : promociones) {
            switch (promocion.getTipoRegla()) {
                case "PORCENTAJE", "DESCUENTO_HORARIO" -> aplicarPorcentaje(promocion, lineas);
                case "DOS_X_UNO" -> aplicarDosPorUno(promocion, lineas);
                case "EXTRA_GRATIS" -> aplicarExtraGratis(promocion, lineas);
                case "COMBO" -> aplicarCombo(promocion, lineas);
                default -> {
                }
            }
        }

        return new ResultadoPromocion(lineas);
    }

    private List<LineaCotizacion> construirLineas(List<OrdenItemRequest> itemsRequest) {
        List<LineaCotizacion> lineas = new ArrayList<>();
        for (OrdenItemRequest itemRequest : itemsRequest) {
            Producto producto = productoRepository.findById(itemRequest.idProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + itemRequest.idProducto()));

            List<Modificador> modificadores = new ArrayList<>();
            for (OrdenItemModRequest modRequest : itemRequest.modificadores() == null ? List.<OrdenItemModRequest>of() : itemRequest.modificadores()) {
                if (modRequest == null || modRequest.idModificador() == null) {
                    continue;
                }
                Modificador modificador = modificadorRepository.findById(modRequest.idModificador())
                        .orElseThrow(() -> new IllegalArgumentException("Modificador no encontrado: " + modRequest.idModificador()));
                modificadores.add(modificador);
            }
            lineas.add(new LineaCotizacion(producto, itemRequest.cantidad(), modificadores));
        }
        return lineas;
    }

    private void aplicarPorcentaje(Promocion promocion, List<LineaCotizacion> lineas) {
        BigDecimal porcentaje = valorNoNegativo(promocion.getPorcentajeDescuento());
        if (porcentaje.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        for (LineaCotizacion linea : lineas) {
            if (!aplicaAProducto(promocion, linea.producto)) {
                continue;
            }
            BigDecimal descuento = linea.totalLista()
                    .multiply(porcentaje)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            linea.agregarDescuento(promocion, descuento, descripcionPromocion(promocion));
        }
    }

    private void aplicarDosPorUno(Promocion promocion, List<LineaCotizacion> lineas) {
        int cantidadMinima = promocion.getCantidadMinima() == null || promocion.getCantidadMinima() < 2 ? 2 : promocion.getCantidadMinima();
        for (LineaCotizacion linea : lineas) {
            if (!aplicaAProducto(promocion, linea.producto) || linea.cantidad < cantidadMinima) {
                continue;
            }
            int unidadesBonificadas = linea.cantidad / cantidadMinima;
            BigDecimal descuento = linea.precioListaUnitario().multiply(BigDecimal.valueOf(unidadesBonificadas));
            linea.agregarDescuento(promocion, descuento, descripcionPromocion(promocion));
        }
    }

    private void aplicarExtraGratis(Promocion promocion, List<LineaCotizacion> lineas) {
        if (promocion.getIdModificadorGratis() == null) {
            return;
        }
        for (LineaCotizacion linea : lineas) {
            if (!aplicaAProducto(promocion, linea.producto)) {
                continue;
            }
            BigDecimal descuento = linea.modificadores.stream()
                    .filter(modificador -> Objects.equals(modificador.getIdModificador(), promocion.getIdModificadorGratis()))
                    .map(Modificador::getPrecioExtra)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .multiply(BigDecimal.valueOf(linea.cantidad));
            if (descuento.compareTo(BigDecimal.ZERO) > 0) {
                linea.agregarDescuento(promocion, descuento, descripcionPromocion(promocion));
            }
        }
    }

    private void aplicarCombo(Promocion promocion, List<LineaCotizacion> lineas) {
        Set<Long> idsCombo = parseIds(promocion.getIdsObjetivo());
        if (idsCombo.isEmpty()) {
            return;
        }

        Map<Long, LineaCotizacion> lineasPorProducto = lineas.stream()
                .filter(linea -> idsCombo.contains(linea.producto.getIdProducto()))
                .collect(Collectors.toMap(linea -> linea.producto.getIdProducto(), linea -> linea, (a, b) -> a));

        if (!lineasPorProducto.keySet().containsAll(idsCombo)) {
            return;
        }

        int sets = idsCombo.stream()
                .mapToInt(idProducto -> lineasPorProducto.get(idProducto).cantidad)
                .min()
                .orElse(0);
        if (sets <= 0) {
            return;
        }

        BigDecimal subtotalCombo = idsCombo.stream()
                .map(idProducto -> lineasPorProducto.get(idProducto).precioListaUnitario())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(sets));

        BigDecimal descuentoTotal = valorNoNegativo(promocion.getMontoDescuento());
        if (descuentoTotal.compareTo(BigDecimal.ZERO) <= 0 && valorNoNegativo(promocion.getPorcentajeDescuento()).compareTo(BigDecimal.ZERO) > 0) {
            descuentoTotal = subtotalCombo
                    .multiply(promocion.getPorcentajeDescuento())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (descuentoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal acumulado = BigDecimal.ZERO;
        int index = 0;
        List<Long> idsOrdenados = new ArrayList<>(idsCombo);
        for (Long idProducto : idsOrdenados) {
            LineaCotizacion linea = lineasPorProducto.get(idProducto);
            BigDecimal descuentoLinea;
            if (index == idsOrdenados.size() - 1) {
                descuentoLinea = descuentoTotal.subtract(acumulado);
            } else {
                BigDecimal baseLinea = linea.precioListaUnitario().multiply(BigDecimal.valueOf(sets));
                descuentoLinea = descuentoTotal.multiply(baseLinea).divide(subtotalCombo, 2, RoundingMode.HALF_UP);
                acumulado = acumulado.add(descuentoLinea);
            }
            linea.agregarDescuento(promocion, descuentoLinea, descripcionPromocion(promocion));
            index++;
        }
    }

    private boolean horarioAplica(Promocion promocion, LocalDateTime fechaHora) {
        if (promocion.getHoraInicio() == null || promocion.getHoraFin() == null) {
            return true;
        }
        return !fechaHora.toLocalTime().isBefore(promocion.getHoraInicio())
                && !fechaHora.toLocalTime().isAfter(promocion.getHoraFin());
    }

    private boolean aplicaAProducto(Promocion promocion, Producto producto) {
        return switch (promocion.getTipoObjetivo()) {
            case "PRODUCTO" -> parseIds(promocion.getIdsObjetivo()).contains(producto.getIdProducto());
            case "CATEGORIA" -> parseIds(promocion.getIdsObjetivo()).contains(producto.getCategoria().getIdCategoria());
            case "COMBO" -> parseIds(promocion.getIdsObjetivo()).contains(producto.getIdProducto());
            default -> false;
        };
    }

    private Set<Long> parseIds(String idsObjetivo) {
        if (idsObjetivo == null || idsObjetivo.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(idsObjetivo.split(","))
                .map(String::trim)
                .filter(valor -> !valor.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    private BigDecimal valorNoNegativo(BigDecimal valor) {
        return valor == null || valor.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : valor;
    }

    private String descripcionPromocion(Promocion promocion) {
        return promocion.getDescripcion() == null || promocion.getDescripcion().isBlank()
                ? promocion.getNombre()
                : promocion.getDescripcion();
    }

    private PromocionEtiquetaResponse toEtiqueta(Promocion promocion) {
        return new PromocionEtiquetaResponse(
                promocion.getIdPromocion(),
                promocion.getNombre(),
                promocion.getTipoRegla(),
                descripcionPromocion(promocion)
        );
    }

    public static final class ResultadoPromocion {
        private final List<LineaCotizacion> lineas;

        public ResultadoPromocion(List<LineaCotizacion> lineas) {
            this.lineas = lineas;
        }

        public List<LineaCotizacion> getLineas() {
            return lineas;
        }

        public BigDecimal totalDescuento() {
            return lineas.stream()
                    .map(LineaCotizacion::descuentoTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal subtotalLista() {
            return lineas.stream()
                    .map(LineaCotizacion::totalLista)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public static final class LineaCotizacion {
        private final Producto producto;
        private final int cantidad;
        private final List<Modificador> modificadores;
        private final List<OrdenItemPromocionResponse> promociones = new ArrayList<>();
        private BigDecimal descuentoTotal = BigDecimal.ZERO;

        public LineaCotizacion(Producto producto, Integer cantidad, List<Modificador> modificadores) {
            this.producto = producto;
            this.cantidad = cantidad == null ? 0 : cantidad;
            this.modificadores = modificadores;
        }

        public Producto getProducto() {
            return producto;
        }

        public String getClave() {
            List<Long> ids = modificadores.stream().map(Modificador::getIdModificador).sorted().toList();
            return producto.getIdProducto() + ":" + ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        }

        public int getCantidad() {
            return cantidad;
        }

        public List<Modificador> getModificadores() {
            return modificadores;
        }

        public BigDecimal precioListaUnitario() {
            BigDecimal extras = modificadores.stream().map(Modificador::getPrecioExtra).reduce(BigDecimal.ZERO, BigDecimal::add);
            return producto.getPrecio().add(extras);
        }

        public BigDecimal totalLista() {
            return precioListaUnitario().multiply(BigDecimal.valueOf(cantidad));
        }

        public BigDecimal descuentoTotal() {
            return descuentoTotal;
        }

        public BigDecimal subtotalFinal() {
            return totalLista().subtract(descuentoTotal);
        }

        public List<OrdenItemPromocionResponse> getPromociones() {
            return promociones;
        }

        public void agregarDescuento(Promocion promocion, BigDecimal descuento, String descripcion) {
            if (descuento == null || descuento.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            BigDecimal descuentoAplicable = descuento.min(totalLista().subtract(descuentoTotal));
            if (descuentoAplicable.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            descuentoTotal = descuentoTotal.add(descuentoAplicable);
            promociones.add(new OrdenItemPromocionResponse(
                    promocion.getNombre(),
                    promocion.getTipoRegla(),
                    descuentoAplicable,
                    descripcion
            ));
        }
    }
}
