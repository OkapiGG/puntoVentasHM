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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ordenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion_caja", nullable = false)
    private SesionCaja sesionCaja;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion = LocalDate.now();

    @Column(name = "folio_dia", nullable = false)
    private Integer folioDia = 1;

    @Column(name = "tipo_orden", nullable = false, length = 20)
    private String tipoOrden = "MOSTRADOR";

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    private LocalDateTime fechaCancelacion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "orden")
    private List<LineaOrden> lineasOrden = new ArrayList<>();

    @OneToMany(mappedBy = "orden")
    private List<Pago> pagos = new ArrayList<>();

    @OneToOne(mappedBy = "orden")
    private OrdenDomicilio ordenDomicilio;
}
