package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tarjeta")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Tarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_enmascarado", nullable = false, length = 19)
    private String numeroEnmascarado; // Ej: **** **** **** 4334

    @Column(name = "ultimo4", nullable = false, length = 4)
    private String ultimo4;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoTarjeta tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoTarjeta estado = EstadoTarjeta.ACTIVA;

    public enum TipoTarjeta { DEBITO, CREDITO }
    public enum EstadoTarjeta { ACTIVA, BLOQUEADA, CANCELADA }
}
