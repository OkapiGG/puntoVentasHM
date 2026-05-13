package puntoVentaHM.puntoVentaHM.pos_hamburguesas.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Categoria;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Insumo;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Modificador;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Mesa;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Negocio;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Producto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.RecetaProducto;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.SesionCaja;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo.Usuario;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.CategoriaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.InsumoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ModificadorRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.MesaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.NegocioRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.ProductoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.RecetaProductoRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.SesionCajaRepository;
import puntoVentaHM.puntoVentaHM.pos_hamburguesas.repository.UsuarioRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            NegocioRepository negocioRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            ModificadorRepository modificadorRepository,
            MesaRepository mesaRepository,
            SesionCajaRepository sesionCajaRepository,
            InsumoRepository insumoRepository,
            RecetaProductoRepository recetaProductoRepository
    ) {
        return args -> {
            Negocio negocio = negocioRepository.findAll().stream().findFirst().orElseGet(() -> {
                Negocio nuevoNegocio = new Negocio();
                nuevoNegocio.setNombre("Hamburguesas May");
                nuevoNegocio.setActivo(true);
                return negocioRepository.save(nuevoNegocio);
            });

            if (negocio.getLimiteCaja() == null) {
                negocio.setLimiteCaja(new BigDecimal("5000.00"));
            }
            if (negocio.getCostoEnvioDefault() == null) {
                negocio.setCostoEnvioDefault(new BigDecimal("30.00"));
            }
            if (negocio.getAlertasCaja() == null) {
                negocio.setAlertasCaja(true);
            }
            if (negocio.getNotificaNuevosPedidos() == null) {
                negocio.setNotificaNuevosPedidos(true);
            }
            if (negocio.getNotificaReportesDiarios() == null) {
                negocio.setNotificaReportesDiarios(false);
            }
            negocio = negocioRepository.save(negocio);

            Usuario usuario = usuarioRepository.findByNombreIgnoreCase("alan").orElseGet(Usuario::new);
            usuario.setNegocio(negocio);
            usuario.setNombre("alan");
            usuario.setPinAcceso("1234");
            usuario.setRol("ADMIN");
            usuario.setAvatarUrl(null);
            usuario.setActivo(true);
            usuario = usuarioRepository.save(usuario);

            Usuario cajero = usuarioRepository.findByNombreIgnoreCase("cajero").orElseGet(Usuario::new);
            cajero.setNegocio(negocio);
            cajero.setNombre("cajero");
            cajero.setPinAcceso("1234");
            cajero.setRol("CAJERO");
            cajero.setAvatarUrl(null);
            cajero.setActivo(true);
            usuarioRepository.save(cajero);

            if (sesionCajaRepository.findTopByUsuarioIdUsuarioAndEstadoOrderByAperturaDesc(usuario.getIdUsuario(), "ABIERTA").isEmpty()) {
                SesionCaja sesionCaja = new SesionCaja();
                sesionCaja.setUsuario(usuario);
                sesionCaja.setFondoInicial(new BigDecimal("500.00"));
                sesionCaja.setApertura(LocalDateTime.now());
                sesionCaja.setEstado("ABIERTA");
                sesionCajaRepository.save(sesionCaja);
            }

            if (categoriaRepository.count() == 0) {
                Categoria hamburguesas = crearCategoria(categoriaRepository, negocio, "HAMBURGUESAS CLASICAS");
                Categoria especiales = crearCategoria(categoriaRepository, negocio, "HAMBURGUESAS ESPECIALES");
                Categoria bebidas = crearCategoria(categoriaRepository, negocio, "BEBIDAS");
                Categoria complementos = crearCategoria(categoriaRepository, negocio, "COMPLEMENTOS");

                productoRepository.save(crearProducto(hamburguesas, "LA CLASICA", "95.00", true, false));
                productoRepository.save(crearProducto(hamburguesas, "HAWAIANA", "99.00", true, false));
                productoRepository.save(crearProducto(especiales, "BBQ BURGUER", "110.00", true, true));
                productoRepository.save(crearProducto(especiales, "DOBLE CARNE", "120.00", true, true));
                productoRepository.save(crearProducto(complementos, "PAPAS A LA FRANCESA", "55.00", false, false));
                productoRepository.save(crearProducto(complementos, "NUGGETS", "65.00", false, false));
                productoRepository.save(crearProducto(bebidas, "COCA COLA", "35.00", false, false));
                productoRepository.save(crearProducto(bebidas, "MALTEADA VAINILLA", "65.00", true, true));
            }

            if (modificadorRepository.count() == 0) {
                Producto clasica = productoRepository.findByNombreIgnoreCase("LA CLASICA").orElse(null);
                Producto hawaiana = productoRepository.findByNombreIgnoreCase("HAWAIANA").orElse(null);
                Producto bbq = productoRepository.findByNombreIgnoreCase("BBQ BURGUER").orElse(null);
                Producto doble = productoRepository.findByNombreIgnoreCase("DOBLE CARNE").orElse(null);

                List.of(
                        clasica != null ? crearModificador(clasica, "Queso Extra", "15.00") : null,
                        clasica != null ? crearModificador(clasica, "Tocino", "20.00") : null,
                        hawaiana != null ? crearModificador(hawaiana, "Queso Extra", "15.00") : null,
                        hawaiana != null ? crearModificador(hawaiana, "Jalapeños", "10.00") : null,
                        bbq != null ? crearModificador(bbq, "Aros de Cebolla", "18.00") : null,
                        bbq != null ? crearModificador(bbq, "Tocino", "20.00") : null,
                        doble != null ? crearModificador(doble, "Carne Extra", "35.00") : null,
                        doble != null ? crearModificador(doble, "Queso Extra", "15.00") : null
                ).stream().filter(java.util.Objects::nonNull).forEach(modificadorRepository::save);
            }

            Map<String, Insumo> insumos = asegurarInsumosBase(negocio, insumoRepository);

            if (recetaProductoRepository.count() == 0) {
                Producto clasica = productoRepository.findByNombreIgnoreCase("LA CLASICA").orElse(null);
                Producto hawaiana = productoRepository.findByNombreIgnoreCase("HAWAIANA").orElse(null);
                Producto bbq = productoRepository.findByNombreIgnoreCase("BBQ BURGUER").orElse(null);
                Producto doble = productoRepository.findByNombreIgnoreCase("DOBLE CARNE").orElse(null);
                Producto papas = productoRepository.findByNombreIgnoreCase("PAPAS A LA FRANCESA").orElse(null);
                Producto nuggets = productoRepository.findByNombreIgnoreCase("NUGGETS").orElse(null);
                Producto coca = productoRepository.findByNombreIgnoreCase("COCA COLA").orElse(null);
                Producto malteada = productoRepository.findByNombreIgnoreCase("MALTEADA VAINILLA").orElse(null);

                crearRecetasHamburguesa(recetaProductoRepository, clasica, insumos, false, false, false);
                crearRecetasHamburguesa(recetaProductoRepository, hawaiana, insumos, true, false, false);
                crearRecetasHamburguesa(recetaProductoRepository, bbq, insumos, false, true, false);
                crearRecetasHamburguesa(recetaProductoRepository, doble, insumos, false, false, true);
                crearRecetaSimple(recetaProductoRepository, papas, insumos.get("Papa"));
                crearRecetaSimple(recetaProductoRepository, nuggets, insumos.get("Nugget"));
                crearRecetaSimple(recetaProductoRepository, coca, insumos.get("Refresco"));
                crearRecetaSimple(recetaProductoRepository, malteada, insumos.get("Base malteada vainilla"));
            }

            if (mesaRepository.findByNegocioIdNegocioOrderByNumeroAsc(negocio.getIdNegocio()).isEmpty()) {
                for (int numero = 1; numero <= 12; numero++) {
                    Mesa mesa = new Mesa();
                    mesa.setNegocio(negocio);
                    mesa.setNumero(numero);
                    mesa.setEstado("LIBRE");
                    mesaRepository.save(mesa);
                }
            }
        };
    }

    private static Categoria crearCategoria(CategoriaRepository categoriaRepository, Negocio negocio, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNegocio(negocio);
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    private static Producto crearProducto(
            Categoria categoria,
            String nombre,
            String precio,
            boolean popular,
            boolean nuevo
    ) {
        Producto producto = new Producto();
        producto.setCategoria(categoria);
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setActivo(true);
        producto.setEsPopular(popular);
        producto.setEsNuevo(nuevo);
        producto.setImagenUrl(null);
        return producto;
    }

    private static Modificador crearModificador(Producto producto, String nombre, String precioExtra) {
        Modificador modificador = new Modificador();
        modificador.setProducto(producto);
        modificador.setNombre(nombre);
        modificador.setPrecioExtra(new BigDecimal(precioExtra));
        modificador.setActivo(true);
        return modificador;
    }

    private static Map<String, Insumo> asegurarInsumosBase(Negocio negocio, InsumoRepository insumoRepository) {
        Map<String, Insumo> insumos = new LinkedHashMap<>();
        insumos.put("Pan", asegurarInsumo(insumoRepository, negocio, "Pan", "pieza", "200"));
        insumos.put("Carne", asegurarInsumo(insumoRepository, negocio, "Carne", "pieza", "180"));
        insumos.put("Queso", asegurarInsumo(insumoRepository, negocio, "Queso", "rebanada", "250"));
        insumos.put("Lechuga", asegurarInsumo(insumoRepository, negocio, "Lechuga", "porcion", "200"));
        insumos.put("Jamon", asegurarInsumo(insumoRepository, negocio, "Jamon", "rebanada", "120"));
        insumos.put("Pina", asegurarInsumo(insumoRepository, negocio, "Pina", "porcion", "100"));
        insumos.put("Salsa BBQ", asegurarInsumo(insumoRepository, negocio, "Salsa BBQ", "porcion", "120"));
        insumos.put("Papa", asegurarInsumo(insumoRepository, negocio, "Papa", "porcion", "120"));
        insumos.put("Nugget", asegurarInsumo(insumoRepository, negocio, "Nugget", "pieza", "300"));
        insumos.put("Refresco", asegurarInsumo(insumoRepository, negocio, "Refresco", "botella", "150"));
        insumos.put("Base malteada vainilla", asegurarInsumo(insumoRepository, negocio, "Base malteada vainilla", "vaso", "90"));
        return insumos;
    }

    private static Insumo asegurarInsumo(
            InsumoRepository insumoRepository,
            Negocio negocio,
            String nombre,
            String unidad,
            String stock
    ) {
        Insumo insumo = insumoRepository.findByNegocioIdNegocioAndNombreIgnoreCase(negocio.getIdNegocio(), nombre)
                .orElseGet(Insumo::new);
        insumo.setNegocio(negocio);
        insumo.setNombre(nombre);
        insumo.setUnidadMedida(unidad);
        if (insumo.getStockActual() == null || insumo.getStockActual().compareTo(BigDecimal.ZERO) == 0) {
            insumo.setStockActual(new BigDecimal(stock));
        }
        insumo.setActivo(true);
        return insumoRepository.save(insumo);
    }

    private static void crearRecetasHamburguesa(
            RecetaProductoRepository recetaProductoRepository,
            Producto producto,
            Map<String, Insumo> insumos,
            boolean hawaiana,
            boolean bbq,
            boolean doble
    ) {
        if (producto == null) {
            return;
        }
        crearReceta(recetaProductoRepository, producto, insumos.get("Pan"), "1");
        crearReceta(recetaProductoRepository, producto, insumos.get("Carne"), doble ? "2" : "1");
        crearReceta(recetaProductoRepository, producto, insumos.get("Queso"), "1");
        crearReceta(recetaProductoRepository, producto, insumos.get("Lechuga"), "1");
        if (hawaiana) {
            crearReceta(recetaProductoRepository, producto, insumos.get("Jamon"), "1");
            crearReceta(recetaProductoRepository, producto, insumos.get("Pina"), "1");
        }
        if (bbq) {
            crearReceta(recetaProductoRepository, producto, insumos.get("Salsa BBQ"), "1");
        }
    }

    private static void crearRecetaSimple(
            RecetaProductoRepository recetaProductoRepository,
            Producto producto,
            Insumo insumo
    ) {
        if (producto == null || insumo == null) {
            return;
        }
        crearReceta(recetaProductoRepository, producto, insumo, "1");
    }

    private static void crearReceta(
            RecetaProductoRepository recetaProductoRepository,
            Producto producto,
            Insumo insumo,
            String cantidad
    ) {
        if (producto == null || insumo == null) {
            return;
        }
        RecetaProducto recetaProducto = new RecetaProducto();
        recetaProducto.setProducto(producto);
        recetaProducto.setInsumo(insumo);
        recetaProducto.setCantidad(new BigDecimal(cantidad));
        recetaProductoRepository.save(recetaProducto);
    }
}
