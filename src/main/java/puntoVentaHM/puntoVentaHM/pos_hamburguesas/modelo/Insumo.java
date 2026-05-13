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
@Table(name = "insumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Long idInsumo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_negocio", nullable = false)
    private Negocio negocio;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "unidad_medida", nullable = false, length = 30)
    private String unidadMedida = "pieza";

    @Column(name = "stock_actual", nullable = false, precision = 10, scale = 3)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Column(name = "stock_minimo", precision = 10, scale = 3)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "insumo")
    private List<RecetaProducto> recetasProducto = new ArrayList<>();

    @OneToMany(mappedBy = "insumo")
    private List<RecetaModificador> recetasModificador = new ArrayList<>();

    @OneToMany(mappedBy = "insumo")
    private List<MovimientoInventario> movimientosInventario = new ArrayList<>();
}
