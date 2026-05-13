package puntoVentaHM.puntoVentaHM.pos_hamburguesas.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "negocios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_negocio")
    private Long idNegocio;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "limite_caja", nullable = false)
    private java.math.BigDecimal limiteCaja = new java.math.BigDecimal("5000.00");

    @Column(name = "costo_envio_default", nullable = false)
    private java.math.BigDecimal costoEnvioDefault = new java.math.BigDecimal("30.00");

    @Column(name = "alertas_caja", nullable = false)
    private Boolean alertasCaja = true;

    @Column(name = "notifica_nuevos_pedidos", nullable = false)
    private Boolean notificaNuevosPedidos = true;

    @Column(name = "notifica_reportes_diarios", nullable = false)
    private Boolean notificaReportesDiarios = false;

    @OneToMany(mappedBy = "negocio")
    private List<Usuario> usuarios = new ArrayList<>();

    @OneToMany(mappedBy = "negocio")
    private List<Categoria> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "negocio")
    private List<Insumo> insumos = new ArrayList<>();

    @OneToMany(mappedBy = "negocio")
    private List<Mesa> mesas = new ArrayList<>();
}
