package puntoVentaHM.puntoVentaHM.pos_hamburguesas.dto;

public record PromocionEtiquetaResponse(
        Long idPromocion,
        String nombre,
        String tipoRegla,
        String descripcion
) {
}
