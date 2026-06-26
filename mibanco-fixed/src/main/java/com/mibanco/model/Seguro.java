package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "seguro")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Seguro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false) private Cliente cliente;
    @Column(name = "prestamo_id") private Long prestamoId;
    @Enumerated(EnumType.STRING) @Column(name = "tipo", nullable = false) private TipoSeguro tipo;
    @Column(name = "prima_mensual", precision = 10, scale = 2) private BigDecimal primaMensual;
    @Column(name = "fecha_inicio", nullable = false) private LocalDate fechaInicio;
    @Column(name = "fecha_fin") private LocalDate fechaFin;
    @Enumerated(EnumType.STRING) @Column(name = "estado") @Builder.Default private EstadoSeguro estado = EstadoSeguro.ACTIVO;
    public enum TipoSeguro { VIDA, DESGRAVAMEN, MULTIRRIESGO, HOGAR, ACCIDENTES }
    public enum EstadoSeguro { ACTIVO, VENCIDO, CANCELADO }
}
