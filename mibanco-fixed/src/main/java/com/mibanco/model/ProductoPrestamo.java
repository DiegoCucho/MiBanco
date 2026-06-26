package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto_prestamo")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "tcea_maxima", nullable = false, precision = 6, scale = 4)
    private BigDecimal tceaMaxima;

    @Column(name = "plazo_minimo", nullable = false)
    private Integer plazoMinimo;

    @Column(name = "plazo_maximo", nullable = false)
    private Integer plazoMaximo;

    @Column(name = "gracia_maxima")
    @Builder.Default
    private Integer graciaMaxima = 3;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
