package puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lineas_orden")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linea_orden")
    private Long idLineaOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false)
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(name = "nombre_snapshot", nullable = false, length = 160)
    private String nombreSnapshot;

    @Column(name = "precio_unitario_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitarioSnapshot;

    @Column(name = "descuento_promocional_snapshot", precision = 10, scale = 2)
    private BigDecimal descuentoPromocionalSnapshot = BigDecimal.ZERO;

    @OneToMany(mappedBy = "lineaOrden")
    private List<LineaOrdenMod> modificadores = new ArrayList<>();

    @OneToMany(mappedBy = "lineaOrden")
    private List<LineaOrdenPromocion> promociones = new ArrayList<>();
}
