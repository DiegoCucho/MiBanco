package com.mibanco.repository;
import com.mibanco.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    /**
     * Trae todas las transacciones del cliente (como origen o destino),
     * con JOIN FETCH para evitar LazyInitializationException en los templates.
     * Se usan subconsultas para filtrar sin que el LEFT JOIN rompa el WHERE.
     */
    @Query("""
        SELECT DISTINCT t FROM Transaccion t
        LEFT JOIN FETCH t.cuentaOrigen co
        LEFT JOIN FETCH t.cuentaDestino cd
        WHERE (co IS NOT NULL AND co.cliente.id = :id)
           OR (cd IS NOT NULL AND cd.cliente.id = :id)
        ORDER BY t.fecha DESC, t.hora DESC
        """)
    List<Transaccion> findByClienteId(@Param("id") Long clienteId);
}
