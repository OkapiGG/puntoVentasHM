package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record AdminCategoriaResponse(
        Long idCategoria,
        String nombre,
        Integer totalProductos
) {
}
