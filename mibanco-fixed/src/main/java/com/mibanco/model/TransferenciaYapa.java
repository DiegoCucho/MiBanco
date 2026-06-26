package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "transferencia_yapa")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferenciaYapa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "remitente_cliente_id", nullable = false) private Cliente remitenteCliente;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_destino_id") private Cliente clienteDestino;
    @Column(name = "numero_destino", nullable = false, length = 15) private String numeroDestino;
    @Column(name = "monto", nullable = false, precision = 10, scale = 2) private BigDecimal monto;
    @Column(name = "fecha", nullable = false) @Builder.Default private LocalDateTime fecha = LocalDateTime.now();
    @Enumerated(EnumType.STRING) @Column(name = "estado") @Builder.Default private EstadoYapa estado = EstadoYapa.COMPLETADO;
    @Column(name = "referencia", length = 50, unique = true) private String referencia;
    public enum EstadoYapa { PENDIENTE, COMPLETADO, RECHAZADO }
}
