package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "historial_pago")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialPago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "cuota_id", nullable = false) private Long cuotaId;
    @Column(name = "fecha_pago", nullable = false) @Builder.Default private LocalDate fechaPago = LocalDate.now();
    @Column(name = "monto_pagado", nullable = false, precision = 15, scale = 2) private BigDecimal montoPagado;
    @Column(name = "canal", length = 30) private String canal;
    @Column(name = "empleado_id") private Long empleadoId;
    @Column(name = "referencia", length = 50) private String referencia;
}
