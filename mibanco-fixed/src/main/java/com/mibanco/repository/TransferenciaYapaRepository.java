package com.mibanco.repository;
import com.mibanco.model.TransferenciaYapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransferenciaYapaRepository extends JpaRepository<TransferenciaYapa, Long> {

    // JOIN FETCH clienteDestino para evitar LazyInitializationException en yapa.html
    @Query("""
        SELECT y FROM TransferenciaYapa y
        LEFT JOIN FETCH y.clienteDestino
        WHERE y.remitenteCliente.id = :clienteId
        ORDER BY y.fecha DESC
        """)
    List<TransferenciaYapa> findByRemitenteClienteIdOrderByFechaDesc(@Param("clienteId") Long clienteId);
}
