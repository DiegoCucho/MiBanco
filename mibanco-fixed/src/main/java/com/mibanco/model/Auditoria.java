package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "auditoria")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Auditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "usuario_id") private Long usuarioId;
    @Column(name = "tabla_afectada", nullable = false, length = 100) private String tablaAfectada;
    @Enumerated(EnumType.STRING) @Column(name = "accion", nullable = false) private AccionAuditoria accion;
    @Column(name = "registro_id") private Long registroId;
    @Column(name = "fecha", nullable = false) @Builder.Default private LocalDateTime fecha = LocalDateTime.now();
    @Column(name = "ip_origen", length = 45) private String ipOrigen;
    public enum AccionAuditoria { INSERT, UPDATE, DELETE }
}
