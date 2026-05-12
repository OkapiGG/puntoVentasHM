package puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lineas_orden_promocion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaOrdenPromocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linea_orden_promocion")
    private Long idLineaOrdenPromocion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_linea_orden", nullable = false)
    private LineaOrden lineaOrden;

    @Column(name = "promocion_nombre", nullable = false, length = 160)
    private String promocionNombre;

    @Column(name = "tipo_regla", nullable = false, length = 40)
    private String tipoRegla;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descuento;

    @Column(length = 255)
    private String descripcion;
}
