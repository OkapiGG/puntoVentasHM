package puntoVentaHM.puntoVentaHM.pos_hamburguesas.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminCatalogoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminCategoriaRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminCategoriaResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminInsumoRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminModificadorRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminModificadorResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminPromocionRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminPromocionResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminProductoRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminProductoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminUsuarioRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.AdminUsuarioResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.InsumoResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.RecetaItemRequest;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto.RecetaItemResponse;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Categoria;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Insumo;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Negocio;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Promocion;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaModificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaProducto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.CategoriaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.InsumoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ModificadorRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.NegocioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.PromocionRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ProductoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.RecetaModificadorRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.RecetaProductoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.security.RolSistema;

@Service
public class AdminService {

    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ModificadorRepository modificadorRepository;
    private final PromocionRepository promocionRepository;
    private final InsumoRepository insumoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecetaProductoRepository recetaProductoRepository;
    private final RecetaModificadorRepository recetaModificadorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            NegocioRepository negocioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            ModificadorRepository modificadorRepository,
            PromocionRepository promocionRepository,
            InsumoRepository insumoRepository,
            UsuarioRepository usuarioRepository,
            RecetaProductoRepository recetaProductoRepository,
            RecetaModificadorRepository recetaModificadorRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.negocioRepository = negocioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.modificadorRepository = modificadorRepository;
        this.promocionRepository = promocionRepository;
        this.insumoRepository = insumoRepository;
        this.usuarioRepository = usuarioRepository;
        this.recetaProductoRepository = recetaProductoRepository;
        this.recetaModificadorRepository = recetaModificadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AdminCatalogoResponse obtenerCatalogo(Long idNegocio) {
        validarNegocio(idNegocio);
        return new AdminCatalogoResponse(
                categoriaRepository.findByNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapCategoria).toList(),
                productoRepository.findByCategoriaNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapProducto).toList(),
                modificadorRepository.findByProductoCategoriaNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapModificador).toList(),
                promocionRepository.findByNegocioIdNegocioOrderByNombreAsc(idNegocio).stream().map(this::mapPromocion).toList(),
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
        producto = productoRepository.save(producto);
        reemplazarRecetaProducto(producto, request.receta());
        return mapProducto(producto);
    }

    @Transactional
    public AdminProductoResponse actualizarProducto(Long idNegocio, Long idProducto, AdminProductoRequest request) {
        Producto producto = obtenerProducto(idNegocio, idProducto);
        aplicarProducto(producto, idNegocio, request);
        producto = productoRepository.save(producto);
        reemplazarRecetaProducto(producto, request.receta());
        return mapProducto(producto);
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
        modificador = modificadorRepository.save(modificador);
        reemplazarRecetaModificador(modificador, request.receta());
        return mapModificador(modificador);
    }

    @Transactional
    public AdminModificadorResponse actualizarModificador(Long idNegocio, Long idModificador, AdminModificadorRequest request) {
        Modificador modificador = obtenerModificador(idNegocio, idModificador);
        aplicarModificador(modificador, idNegocio, request);
        modificador = modificadorRepository.save(modificador);
        reemplazarRecetaModificador(modificador, request.receta());
        return mapModificador(modificador);
    }

    @Transactional
    public void eliminarModificador(Long idNegocio, Long idModificador) {
        Modificador modificador = obtenerModificador(idNegocio, idModificador);
        modificador.setActivo(false);
        modificadorRepository.save(modificador);
    }

    @Transactional
    public AdminPromocionResponse crearPromocion(Long idNegocio, AdminPromocionRequest request) {
        Promocion promocion = new Promocion();
        aplicarPromocion(promocion, idNegocio, request);
        return mapPromocion(promocionRepository.save(promocion));
    }

    @Transactional
    public AdminPromocionResponse actualizarPromocion(Long idNegocio, Long idPromocion, AdminPromocionRequest request) {
        Promocion promocion = obtenerPromocion(idNegocio, idPromocion);
        aplicarPromocion(promocion, idNegocio, request);
        return mapPromocion(promocionRepository.save(promocion));
    }

    @Transactional
    public void eliminarPromocion(Long idNegocio, Long idPromocion) {
        Promocion promocion = obtenerPromocion(idNegocio, idPromocion);
        promocion.setActiva(false);
        promocionRepository.save(promocion);
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
        insumo.setStockMinimo(request.stockMinimo() == null ? BigDecimal.ZERO : request.stockMinimo());
        insumo.setCostoUnitario(request.costoUnitario() == null ? BigDecimal.ZERO : request.costoUnitario());
        insumo.setActivo(request.activo() == null ? true : request.activo());
    }

    private void reemplazarRecetaProducto(Producto producto, List<RecetaItemRequest> receta) {
        recetaProductoRepository.deleteByProductoIdProducto(producto.getIdProducto());
        if (receta == null) {
            return;
        }
        for (RecetaItemRequest item : receta) {
            if (item == null || item.idInsumo() == null || item.cantidad() == null || item.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            RecetaProducto recetaProducto = new RecetaProducto();
            recetaProducto.setProducto(producto);
            recetaProducto.setInsumo(obtenerInsumo(producto.getCategoria().getNegocio().getIdNegocio(), item.idInsumo()));
            recetaProducto.setCantidad(item.cantidad());
            recetaProductoRepository.save(recetaProducto);
        }
    }

    private void reemplazarRecetaModificador(Modificador modificador, List<RecetaItemRequest> receta) {
        recetaModificadorRepository.deleteByModificadorIdModificador(modificador.getIdModificador());
        if (receta == null) {
            return;
        }
        for (RecetaItemRequest item : receta) {
            if (item == null || item.idInsumo() == null || item.cantidad() == null || item.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            RecetaModificador recetaModificador = new RecetaModificador();
            recetaModificador.setModificador(modificador);
            recetaModificador.setInsumo(obtenerInsumo(modificador.getProducto().getCategoria().getNegocio().getIdNegocio(), item.idInsumo()));
            recetaModificador.setCantidad(item.cantidad());
            recetaModificadorRepository.save(recetaModificador);
        }
    }

    private void aplicarPromocion(Promocion promocion, Long idNegocio, AdminPromocionRequest request) {
        promocion.setNegocio(validarNegocio(idNegocio));
        promocion.setNombre(validarTexto(request.nombre(), "El nombre de la promoción es obligatorio"));
        promocion.setDescripcion(textoOpcional(request.descripcion()));
        promocion.setTipoRegla(validarEnCatalogo(request.tipoRegla(), List.of("PORCENTAJE", "DOS_X_UNO", "EXTRA_GRATIS", "DESCUENTO_HORARIO", "COMBO"), "Tipo de regla inválido"));
        promocion.setTipoObjetivo(validarEnCatalogo(request.tipoObjetivo(), List.of("PRODUCTO", "CATEGORIA", "COMBO"), "Tipo de objetivo inválido"));
        promocion.setIdsObjetivo(validarTexto(request.idsObjetivo(), "Debes indicar los IDs objetivo"));
        promocion.setIdModificadorGratis(request.idModificadorGratis());
        if (request.fechaInicio() == null || request.fechaFin() == null || request.fechaFin().isBefore(request.fechaInicio())) {
            throw new IllegalArgumentException("El rango de fechas de la promoción es inválido");
        }
        promocion.setFechaInicio(request.fechaInicio());
        promocion.setFechaFin(request.fechaFin());
        promocion.setHoraInicio(request.horaInicio());
        promocion.setHoraFin(request.horaFin());
        promocion.setPorcentajeDescuento(request.porcentajeDescuento());
        promocion.setMontoDescuento(request.montoDescuento());
        promocion.setCantidadMinima(request.cantidadMinima());
        promocion.setActiva(request.activa() == null ? true : request.activa());
    }

    private void aplicarUsuario(Usuario usuario, Long idNegocio, AdminUsuarioRequest request, boolean actualizacion) {
        usuario.setNegocio(validarNegocio(idNegocio));
        usuario.setNombre(validarTexto(request.nombre(), "El nombre del usuario es obligatorio"));

        String pin = request.pinAcceso() == null ? "" : request.pinAcceso().trim();
        if (!actualizacion || !pin.isBlank()) {
            if (pin.isBlank()) {
                throw new IllegalArgumentException("El pin de acceso es obligatorio");
            }
            usuario.setPinAcceso(passwordEncoder.encode(pin));
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

    private Promocion obtenerPromocion(Long idNegocio, Long idPromocion) {
        if (idPromocion == null) {
            throw new IllegalArgumentException("La promoción es obligatoria");
        }
        Promocion promocion = promocionRepository.findById(idPromocion)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));
        if (!promocion.getNegocio().getIdNegocio().equals(idNegocio)) {
            throw new IllegalArgumentException("La promoción no pertenece al negocio");
        }
        return promocion;
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

    private String validarEnCatalogo(String valor, List<String> catalogo, String mensaje) {
        String normalizado = validarTexto(valor, mensaje).toUpperCase();
        if (!catalogo.contains(normalizado)) {
            throw new IllegalArgumentException(mensaje);
        }
        return normalizado;
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
                costoRecetaProducto(producto),
                producto.getImagenUrl(),
                producto.getActivo(),
                producto.getEsPopular(),
                producto.getEsNuevo(),
                producto.getModificadores().size(),
                producto.getReceta().stream().map(this::mapRecetaProducto).toList()
        );
    }

    private AdminModificadorResponse mapModificador(Modificador modificador) {
        return new AdminModificadorResponse(
                modificador.getIdModificador(),
                modificador.getProducto().getIdProducto(),
                modificador.getProducto().getNombre(),
                modificador.getNombre(),
                modificador.getPrecioExtra(),
                costoRecetaModificador(modificador),
                modificador.getActivo(),
                modificador.getReceta().stream().map(this::mapRecetaModificador).toList()
        );
    }

    private InsumoResponse mapInsumo(Insumo insumo) {
        return new InsumoResponse(
                insumo.getIdInsumo(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                insumo.getStockActual(),
                insumo.getStockMinimo() == null ? BigDecimal.ZERO : insumo.getStockMinimo(),
                insumo.getCostoUnitario() == null ? BigDecimal.ZERO : insumo.getCostoUnitario(),
                insumo.getStockActual().compareTo(insumo.getStockMinimo() == null ? BigDecimal.ZERO : insumo.getStockMinimo()) <= 0,
                insumo.getActivo()
        );
    }

    private BigDecimal costoRecetaProducto(Producto producto) {
        return producto.getReceta().stream()
                .map(receta -> costoReceta(receta.getCantidad(), receta.getInsumo().getCostoUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal costoRecetaModificador(Modificador modificador) {
        return modificador.getReceta().stream()
                .map(receta -> costoReceta(receta.getCantidad(), receta.getInsumo().getCostoUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal costoReceta(BigDecimal cantidad, BigDecimal costoUnitario) {
        return (cantidad == null ? BigDecimal.ZERO : cantidad)
                .multiply(costoUnitario == null ? BigDecimal.ZERO : costoUnitario);
    }

    private RecetaItemResponse mapRecetaProducto(RecetaProducto receta) {
        return new RecetaItemResponse(
                receta.getInsumo().getIdInsumo(),
                receta.getInsumo().getNombre(),
                receta.getInsumo().getUnidadMedida(),
                receta.getCantidad(),
                costoReceta(receta.getCantidad(), receta.getInsumo().getCostoUnitario())
        );
    }

    private RecetaItemResponse mapRecetaModificador(RecetaModificador receta) {
        return new RecetaItemResponse(
                receta.getInsumo().getIdInsumo(),
                receta.getInsumo().getNombre(),
                receta.getInsumo().getUnidadMedida(),
                receta.getCantidad(),
                costoReceta(receta.getCantidad(), receta.getInsumo().getCostoUnitario())
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

    private AdminPromocionResponse mapPromocion(Promocion promocion) {
        return new AdminPromocionResponse(
                promocion.getIdPromocion(),
                promocion.getNombre(),
                promocion.getDescripcion(),
                promocion.getTipoRegla(),
                promocion.getTipoObjetivo(),
                promocion.getIdsObjetivo(),
                promocion.getIdModificadorGratis(),
                promocion.getFechaInicio(),
                promocion.getFechaFin(),
                promocion.getHoraInicio(),
                promocion.getHoraFin(),
                promocion.getPorcentajeDescuento(),
                promocion.getMontoDescuento(),
                promocion.getCantidadMinima(),
                promocion.getActiva()
        );
    }
}
