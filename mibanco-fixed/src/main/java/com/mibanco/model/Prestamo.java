package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "prestamo")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "tasa_interes_mensual", nullable = false, precision = 6, scale = 4)
    private BigDecimal tasaInteresMensual;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "cuota_mensual", precision = 15, scale = 2)
    private BigDecimal cuotaMensual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoPrestamo producto;

    @Column(name = "fecha_solicitud", nullable = false)
    @Builder.Default
    private LocalDate fechaSolicitud = LocalDate.now();

    @Column(name = "fecha_aprobacion")
    private LocalDate fechaAprobacion;

    @Column(name = "fecha_desembolso")
    private LocalDate fechaDesembolso;

    @Column(name = "periodo_gracia_meses")
    @Builder.Default
    private Integer periodoGraciaMeses = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoPrestamo estado = EstadoPrestamo.PENDIENTE;

    @Column(name = "tcea", precision = 6, scale = 4)
    private BigDecimal tcea;

    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CuotaPrestamo> cuotas;

    public enum EstadoPrestamo { PENDIENTE, APROBADO, DESEMBOLSADO, MOROSO, CANCELADO }
}
