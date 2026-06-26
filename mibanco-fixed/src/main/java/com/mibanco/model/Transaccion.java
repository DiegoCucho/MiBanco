package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaccion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoTransaccion tipo;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id")
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id")
    private Cuenta cuentaDestino;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private LocalDate fecha = LocalDate.now();

    @Column(name = "hora")
    @Builder.Default
    private LocalTime hora = LocalTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Column(name = "empleado_id")
    private Long empleadoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal")
    private Canal canal;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "referencia", length = 50, unique = true)
    private String referencia;

    public enum TipoTransaccion {
        DEPOSITO, RETIRO, TRANSFERENCIA, PAGO_PRESTAMO,
        PAGO_SERVICIO, YAPA, CAMBIO_DIVISA
    }

    public enum Canal { AGENCIA, APP, WEB, WHATSAPP, CAJERO }
}
