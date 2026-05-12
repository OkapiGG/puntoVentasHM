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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sesiones_caja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SesionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion_caja")
    private Long idSesionCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fondo_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal fondoInicial = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime apertura;

    private LocalDateTime cierre;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "monto_declarado_cierre", precision = 10, scale = 2)
    private BigDecimal montoDeclaradoCierre;

    @Column(name = "diferencia_cierre", precision = 10, scale = 2)
    private BigDecimal diferenciaCierre;

    @OneToMany(mappedBy = "sesionCaja")
    private List<MovimientoCaja> movimientosCaja = new ArrayList<>();

    @OneToMany(mappedBy = "sesionCaja")
    private List<Orden> ordenes = new ArrayList<>();
}
