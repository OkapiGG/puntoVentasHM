package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

import java.util.List;

public record AdminCatalogoResponse(
        List<AdminCategoriaResponse> categorias,
        List<AdminProductoResponse> productos,
        List<AdminModificadorResponse> modificadores,
        List<AdminPromocionResponse> promociones,
        List<InsumoResponse> insumos,
        List<AdminUsuarioResponse> usuarios
) {
}
