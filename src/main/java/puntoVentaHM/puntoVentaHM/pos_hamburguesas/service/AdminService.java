package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Categoria;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Insumo;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Negocio;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.CategoriaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.InsumoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ModificadorRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.NegocioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ProductoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;

@Service
public class AdminService {

    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ModificadorRepository modificadorRepository;
    private final InsumoRepository insumoRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminService(
            NegocioRepository negocioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            ModificadorRepository modificadorRepository,
            InsumoRepository insumoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.negocioRepository = negocioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.modificadorRepository = modificadorRepository;
        this.insumoRepository = insumoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public AdminCatalogoResponse obtenerCatalogo(Long idNegocio) {
        validarNegocio(idNegocio);
        return new AdminCatalogoResponse(
                categoriaRepository.findByNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapCategoria).toList(),
                productoRepository.findByCategoriaNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapProducto).toList(),
                modificadorRepository.findByProductoCategoriaNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapModificador).toList(),
                insumoRepository.findByNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapInsumo).toList(),
                usuarioRepository.findByNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapUsuario).toList()
        );
    }

    @Transactional
    public AdminCategoriaResponse crearCategoria(Long idNegocio, AdminCategoriaRequest request) {
        Negocio negocio = validarNegocio(idNegocio);
        String nombre = validarTexto(request.nombre(), "El nombre de la categoria es obligatorio");
        Categoria categoria = new Categoria();
        categoria.setNegocio(negocio);
        categoria.setNombre(nombre);
        return mapCategoria(categoriaRepository.save(categoria));
    }

    @Transactional
    public AdminCategoriaResponse actualizarCategoria(Long idNegocio, Long idCategoria, AdminCategoriaRequest request) {
        Categoria categoria = obtenerCategoria(idNegocio, idCategoria);
        categoria.setNombre(validarTexto(request.nombre(), "El nombre de la categoria es obligatorio"));
        return mapCategoria(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminarCategoria(Long idNegocio, Long idCategoria) {
        Categoria categoria = obtenerCategoria(idNegocio, idCategoria);
        if (!categoria.getProductos().isEmpty()) {
            throw new IllegalArgumentException("No puedes eliminar una categoria con productos asociados");
        }
        categoriaRepository.delete(categoria);
    }

    @Transactional
    public AdminProductoResponse crearProducto(Long idNegocio, AdminProductoRequest request) {
        Producto producto = new Producto();
        aplicarProducto(producto, idNegocio, request);
        return mapProducto(productoRepository.save(producto));
    }

    @Transactional
    public AdminProductoResponse actualizarProducto(Long idNegocio, Long idProducto, AdminProductoRequest request) {
        Producto producto = obtenerProducto(idNegocio, idProducto);
        aplicarProducto(producto, idNegocio, request);
        return mapProducto(productoRepository.save(producto));
    }

    @Transactional
    public void eliminarProducto(Long idNegocio, Long idProducto) {
        Producto producto = obtenerProducto(idNegocio, idProducto);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Transactional
    public AdminModificadorResponse crearModificador(Long idNegocio, AdminModificadorRequest request) {
        Modificador modificador = new Modificador();
        aplicarModificador(modificador, idNegocio, request);
        return mapModificador(modificadorRepository.save(modificador));
    }

    @Transactional
    public AdminModificadorResponse actualizarModificador(Long idNegocio, Long idModificador, AdminModificadorRequest request) {
        Modificador modificador = obtenerModificador(idNegocio, idModificador);
        aplicarModificador(modificador, idNegocio, request);
        return mapModificador(modificadorRepository.save(modificador));
    }

    @Transactional
    public void eliminarModificador(Long idNegocio, Long idModificador) {
        Modificador modificador = obtenerModificador(idNegocio, idModificador);
        modificador.setActivo(false);
        modificadorRepository.save(modificador);
    }

    @Transactional
    public InsumoResponse crearInsumo(Long idNegocio, AdminInsumoRequest request) {
        Insumo insumo = new Insumo();
        aplicarInsumo(insumo, idNegocio, request);
        return mapInsumo(insumoRepository.save(insumo));
    }

    @Transactional
    public InsumoResponse actualizarInsumo(Long idNegocio, Long idInsumo, AdminInsumoRequest request) {
        Insumo insumo = obtenerInsumo(idNegocio, idInsumo);
        aplicarInsumo(insumo, idNegocio, request);
        return mapInsumo(insumoRepository.save(insumo));
    }

    @Transactional
    public void eliminarInsumo(Long idNegocio, Long idInsumo) {
        Insumo insumo = obtenerInsumo(idNegocio, idInsumo);
        insumo.setActivo(false);
        insumoRepository.save(insumo);
    }

    @Transactional
    public AdminUsuarioResponse crearUsuario(Long idNegocio, AdminUsuarioRequest request) {
        Usuario usuario = new Usuario();
        aplicarUsuario(usuario, idNegocio, request, false);
        return mapUsuario(usuarioRepository.save(usuario));
    }

    @Transactional
    public AdminUsuarioResponse actualizarUsuario(Long idNegocio, Long idUsuario, AdminUsuarioRequest request) {
        Usuario usuario = obtenerUsuario(idNegocio, idUsuario);
        aplicarUsuario(usuario, idNegocio, request, true);
        return mapUsuario(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminarUsuario(Long idNegocio, Long idUsuario) {
        Usuario usuario = obtenerUsuario(idNegocio, idUsuario);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private void aplicarProducto(Producto producto, Long idNegocio, AdminProductoRequest request) {
        Categoria categoria = obtenerCategoria(idNegocio, request.idCategoria());
        producto.setCategoria(categoria);
        producto.setNombre(validarTexto(request.nombre(), "El nombre del producto es obligatorio"));
        if (request.precio() == null || request.precio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del producto es invalido");
        }
        producto.setPrecio(request.precio());
        producto.setImagenUrl(textoOpcional(request.imagenUrl()));
        producto.setActivo(request.activo() == null ? true : request.activo());
        producto.setEsPopular(request.esPopular() == null ? false : request.esPopular());
        producto.setEsNuevo(request.esNuevo() == null ? false : request.esNuevo());
    }

    private void aplicarModificador(Modificador modificador, Long idNegocio, AdminModificadorRequest request) {
        Producto producto = obtenerProducto(idNegocio, request.idProducto());
        modificador.setProducto(producto);
        modificador.setNombre(validarTexto(request.nombre(), "El nombre del modificador es obligatorio"));
        if (request.precioExtra() == null || request.precioExtra().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio extra del modificador es invalido");
        }
        modificador.setPrecioExtra(request.precioExtra());
        modificador.setActivo(request.activo() == null ? true : request.activo());
    }

    private void aplicarInsumo(Insumo insumo, Long idNegocio, AdminInsumoRequest request) {
        insumo.setNegocio(validarNegocio(idNegocio));
        insumo.setNombre(validarTexto(request.nombre(), "El nombre del insumo es obligatorio"));
        insumo.setUnidadMedida(validarTexto(request.unidadMedida(), "La unidad de medida es obligatoria"));
        if (request.stockActual() == null || request.stockActual().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El stock del insumo es invalido");
        }
        insumo.setStockActual(request.stockActual());
        insumo.setActivo(request.activo() == null ? true : request.activo());
    }

    private void aplicarUsuario(Usuario usuario, Long idNegocio, AdminUsuarioRequest request, boolean actualizacion) {
        usuario.setNegocio(validarNegocio(idNegocio));
        usuario.setNombre(validarTexto(request.nombre(), "El nombre del usuario es obligatorio"));

        String pin = request.pinAcceso() == null ? "" : request.pinAcceso().trim();
        if (!actualizacion || !pin.isBlank()) {
            if (pin.isBlank()) {
                throw new IllegalArgumentException("El pin de acceso es obligatorio");
            }
            usuario.setPinAcceso(pin);
        }

        usuario.setRol(RolSistema.from(request.rol()).name());
        usuario.setAvatarUrl(textoOpcional(request.avatarUrl()));
        usuario.setActivo(request.activo() == null ? true : request.activo());
    }

    private Negocio validarNegocio(Long idNegocio) {
        return negocioRepository.findById(idNegocio)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
    }

    private Categoria obtenerCategoria(Long idNegocio, Long idCategoria) {
        if (idCategoria == null) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        if (!categoria.getNegocio().getIdNegocio().equals(idNegocio)) {
            throw new IllegalArgumentException("La categoria no pertenece al negocio");
        }
        return categoria;
    }

    private Producto obtenerProducto(Long idNegocio, Long idProducto) {
        if (idProducto == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (!producto.getCategoria().getNegocio().getIdNegocio().equals(idNegocio)) {
            throw new IllegalArgumentException("El producto no pertenece al negocio");
        }
        return producto;
    }

    private Modificador obtenerModificador(Long idNegocio, Long idModificador) {
        if (idModificador == null) {
            throw new IllegalArgumentException("El modificador es obligatorio");
        }
        Modificador modificador = modificadorRepository.findById(idModificador)
                .orElseThrow(() -> new IllegalArgumentException("Modificador no encontrado"));
        if (!modificador.getProducto().getCategoria().getNegocio().getIdNegocio().equals(idNegocio)) {
            throw new IllegalArgumentException("El modificador no pertenece al negocio");
        }
        return modificador;
    }

    private Insumo obtenerInsumo(Long idNegocio, Long idInsumo) {
        if (idInsumo == null) {
            throw new IllegalArgumentException("El insumo es obligatorio");
        }
        Insumo insumo = insumoRepository.findById(idInsumo)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));
        if (!insumo.getNegocio().getIdNegocio().equals(idNegocio)) {
            throw new IllegalArgumentException("El insumo no pertenece al negocio");
        }
        return insumo;
    }

    private Usuario obtenerUsuario(Long idNegocio, Long idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!usuario.getNegocio().getIdNegocio().equals(idNegocio)) {
            throw new IllegalArgumentException("El usuario no pertenece al negocio");
        }
        return usuario;
    }

    private String validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.trim().isBlank() ? null : valor.trim();
    }

    private AdminCategoriaResponse mapCategoria(Categoria categoria) {
        return new AdminCategoriaResponse(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getProductos().size()
        );
    }

    private AdminProductoResponse mapProducto(Producto producto) {
        return new AdminProductoResponse(
                producto.getIdProducto(),
                producto.getCategoria().getIdCategoria(),
                producto.getCategoria().getNombre(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getImagenUrl(),
                producto.getActivo(),
                producto.getEsPopular(),
                producto.getEsNuevo(),
                producto.getModificadores().size()
        );
    }

    private AdminModificadorResponse mapModificador(Modificador modificador) {
        return new AdminModificadorResponse(
                modificador.getIdModificador(),
                modificador.getProducto().getIdProducto(),
                modificador.getProducto().getNombre(),
                modificador.getNombre(),
                modificador.getPrecioExtra(),
                modificador.getActivo()
        );
    }

    private InsumoResponse mapInsumo(Insumo insumo) {
        return new InsumoResponse(
                insumo.getIdInsumo(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                insumo.getStockActual(),
                insumo.getActivo()
        );
    }

    private AdminUsuarioResponse mapUsuario(Usuario usuario) {
        return new AdminUsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getAvatarUrl(),
                usuario.getActivo()
        );
    }
}
