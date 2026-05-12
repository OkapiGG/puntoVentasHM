package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.CategoriaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ModificadorResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.ProductoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Categoria;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.CategoriaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ModificadorRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ProductoRepository;

@Service
public class CatalogoService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ModificadorRepository modificadorRepository;
    private final SlugService slugService;

    public CatalogoService(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            ModificadorRepository modificadorRepository,
            SlugService slugService
    ) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.modificadorRepository = modificadorRepository;
        this.slugService = slugService;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::toCategoriaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosPorCategoriaSlug(String categoriaSlug) {
        Categoria categoria = categoriaRepository.findAllByOrderByNombreAsc()
                .stream()
                .filter(item -> slugService.toSlug(item.getNombre()).equals(categoriaSlug))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));

        return productoRepository.findByCategoriaIdCategoriaAndActivoTrueOrderByNombreAsc(categoria.getIdCategoria())
                .stream()
                .map(this::toProductoResponse)
                .toList();
    }

    private CategoriaResponse toCategoriaResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                slugService.toSlug(categoria.getNombre())
        );
    }

    private ProductoResponse toProductoResponse(Producto producto) {
        return new ProductoResponse(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getImagenUrl(),
                producto.getActivo(),
                producto.getEsPopular(),
                producto.getEsNuevo(),
                producto.getCategoria().getNombre(),
                modificadorRepository.findByProductoIdProductoAndActivoTrueOrderByNombreAsc(producto.getIdProducto())
                        .stream()
                        .map(this::toModificadorResponse)
                        .toList()
        );
    }

    private ModificadorResponse toModificadorResponse(Modificador modificador) {
        return new ModificadorResponse(
                modificador.getIdModificador(),
                modificador.getNombre(),
                modificador.getPrecioExtra()
        );
    }
}
