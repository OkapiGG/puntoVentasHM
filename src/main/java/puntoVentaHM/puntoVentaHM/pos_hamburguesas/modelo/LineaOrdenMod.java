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
@Table(name = "lineas_orden_mod")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaOrdenMod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linea_orden_mod")
    private Long idLineaOrdenMod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_linea_orden", nullable = false)
    private LineaOrden lineaOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modificador", nullable = false)
    private Modificador modificador;

    @Column(name = "nombre_snapshot", nullable = false, length = 120)
    private String nombreSnapshot;

    @Column(name = "precio_extra_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioExtraSnapshot;
}
