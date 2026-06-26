package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "servicio_pago")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ServicioPago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false) private Cliente cliente;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_servicio", nullable = false) private TipoServicio tipoServicio;
    @Column(name = "empresa", length = 100) private String empresa;
    @Column(name = "numero_suministro", length = 50) private String numeroSuministro;
    @Column(name = "monto", nullable = false, precision = 10, scale = 2) private BigDecimal monto;
    @Column(name = "fecha_pago", nullable = false) @Builder.Default private LocalDateTime fechaPago = LocalDateTime.now();
    @Column(name = "referencia", length = 50) private String referencia;
    public enum TipoServicio { LUZ, AGUA, TELEFONIA, CELULAR, INTERNET, EDUCACION }
}
