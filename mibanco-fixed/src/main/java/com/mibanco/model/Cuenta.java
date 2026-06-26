package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cuenta")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", unique = true, nullable = false, length = 20)
    private String numeroCuenta;

    @Column(name = "producto", nullable = false, length = 100)
    private String producto;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", nullable = false)
    @Builder.Default
    private Moneda moneda = Moneda.PEN;

    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "interes_mensual", precision = 5, scale = 4)
    private BigDecimal interesMensual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Column(name = "fecha_apertura", nullable = false)
    @Builder.Default
    private LocalDate fechaApertura = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoCuenta estado = EstadoCuenta.ACTIVA;

    public enum Moneda { PEN, USD }
    public enum EstadoCuenta { ACTIVA, BLOQUEADA, CANCELADA }
}
