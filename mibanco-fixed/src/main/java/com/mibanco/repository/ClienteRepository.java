package com.mibanco.repository;

import com.mibanco.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByRuc(String ruc);
    List<Cliente> findByEstado(Cliente.EstadoCliente estado);

    // Buscar cliente por número de teléfono (para verificar Yapa)
    Optional<Cliente> findByTelefono(String telefono);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombreCompleto) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Cliente> buscar(@Param("q") String query);
}
