package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cuota_prestamo")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CuotaPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestamo_id", nullable = false)
    private Prestamo prestamo;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_pago_programada", nullable = false)
    private LocalDate fechaPagoProgramada;

    @Column(name = "capital", nullable = false, precision = 15, scale = 2)
    private BigDecimal capital;

    @Column(name = "interes", nullable = false, precision = 15, scale = 2)
    private BigDecimal interes;

    @Column(name = "seguro", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal seguro = BigDecimal.ZERO;

    @Column(name = "mora", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal mora = BigDecimal.ZERO;

    @Column(name = "monto_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "saldo_capital", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoCapital;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    @Column(name = "fecha_pago_real")
    private LocalDate fechaPagoReal;

    public enum EstadoCuota { PENDIENTE, PAGADA, MOROSA }
}
