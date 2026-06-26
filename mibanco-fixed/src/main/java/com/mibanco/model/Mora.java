package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "mora")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Mora {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "cuota_id", nullable = false) private Long cuotaId;
    @Column(name = "dias_atraso", nullable = false) private Integer diasAtraso;
    @Column(name = "interes_moratorio", nullable = false, precision = 10, scale = 2) private BigDecimal interesMoratorio;
    @Column(name = "fecha_calculo", nullable = false) @Builder.Default private LocalDate fechaCalculo = LocalDate.now();
}
