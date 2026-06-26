package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sucursal")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "horario", length = 100)
    private String horario;

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private Boolean activa = true;
}
