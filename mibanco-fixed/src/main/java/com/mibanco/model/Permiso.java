package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permiso")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Permiso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;
    @Column(name = "descripcion", length = 300)
    private String descripcion;
}
