package com.mibanco.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "beneficiario")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Beneficiario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false) private Cliente cliente;
    @Column(name = "nombre", nullable = false, length = 200) private String nombre;
    @Column(name = "banco", nullable = false, length = 100) private String banco;
    @Column(name = "numero_cuenta", nullable = false, length = 30) private String numeroCuenta;
    @Column(name = "alias", length = 100) private String alias;
}
