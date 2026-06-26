package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "empleado")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Empleado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id") private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sucursal_id") private Sucursal sucursal;
    @Column(name = "nombre", nullable = false, length = 200) private String nombre;
    @Column(name = "cargo", length = 100) private String cargo;
    @Column(name = "dni", nullable = false, unique = true, length = 8) private String dni;
    @Column(name = "telefono", length = 15) private String telefono;
    @Column(name = "email", length = 100) private String email;
    @Column(name = "activo", nullable = false) @Builder.Default private Boolean activo = true;
    @Column(name = "fecha_ingreso") private LocalDate fechaIngreso;
}
