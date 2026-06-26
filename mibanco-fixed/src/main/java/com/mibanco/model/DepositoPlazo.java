package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "deposito_plazo")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositoPlazo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false) private Cliente cliente;
    @Column(name = "monto", nullable = false, precision = 15, scale = 2) private BigDecimal monto;
    @Column(name = "tasa_interes_mensual", nullable = false, precision = 6, scale = 4) private BigDecimal tasaInteresMensual;
    @Column(name = "plazo_meses", nullable = false) private Integer plazoMeses;
    @Column(name = "fecha_inicio", nullable = false) private LocalDate fechaInicio;
    @Column(name = "fecha_fin", nullable = false) private LocalDate fechaFin;
    @Column(name = "saldo_proyectado", precision = 15, scale = 2) private BigDecimal saldoProyectado;
    @Enumerated(EnumType.STRING) @Column(name = "estado") @Builder.Default private EstadoDeposito estado = EstadoDeposito.ACTIVO;
    public enum EstadoDeposito { ACTIVO, VENCIDO, CANCELADO }
}
