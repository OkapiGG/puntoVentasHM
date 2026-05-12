package puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ordenes_domicilio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenDomicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_domicilio")
    private Long idOrdenDomicilio;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false, unique = true)
    private Orden orden;

    @Column(name = "nombre_cliente", length = 120)
    private String nombreCliente;

    @Column(length = 255)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(name = "estado_entrega", nullable = false, length = 30)
    private String estadoEntrega = "PENDIENTE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_repartidor")
    private Usuario repartidor;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;
}
