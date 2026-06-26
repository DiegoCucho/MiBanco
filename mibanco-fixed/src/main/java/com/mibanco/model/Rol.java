package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "rol")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Rol {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
    @Column(name = "descripcion", length = 200)
    private String descripcion;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rol_permiso",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id"))
    private Set<Permiso> permisos;
}
