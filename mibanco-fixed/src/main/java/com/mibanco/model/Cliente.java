package com.mibanco.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_persona", nullable = false)
    private TipoPersona tipoPersona;

    @Column(name = "dni", length = 8, unique = true)
    private String dni;

    @Column(name = "ruc", length = 11, unique = true)
    private String ruc;

    @Column(name = "nombre_completo", length = 200)
    private String nombreCompleto;

    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_constitucion")
    private LocalDate fechaConstitucion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoCliente estado = EstadoCliente.ACTIVO;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Cuenta> cuentas;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Prestamo> prestamos;

    public enum TipoPersona { NATURAL, EMPRESA }
    public enum EstadoCliente { ACTIVO, INACTIVO, BLOQUEADO }

    public String getNombreDisplay() {
        return tipoPersona == TipoPersona.NATURAL ? nombreCompleto : razonSocial;
    }

    public String getDocumentoDisplay() {
        return tipoPersona == TipoPersona.NATURAL ? "DNI: " + dni : "RUC: " + ruc;
    }
}
