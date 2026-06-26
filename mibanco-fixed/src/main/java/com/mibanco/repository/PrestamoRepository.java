package com.mibanco.repository;
import com.mibanco.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    // JOIN FETCH producto para evitar LazyInitializationException en prestamos.html
    @Query("SELECT p FROM Prestamo p JOIN FETCH p.producto WHERE p.cliente.id = :clienteId ORDER BY p.fechaSolicitud DESC")
    List<Prestamo> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT p FROM Prestamo p JOIN FETCH p.producto JOIN FETCH p.cliente WHERE p.estado = :estado ORDER BY p.fechaSolicitud DESC")
    List<Prestamo> findByEstado(@Param("estado") Prestamo.EstadoPrestamo estado);

    @Query("SELECT p FROM Prestamo p JOIN FETCH p.producto JOIN FETCH p.cliente ORDER BY p.fechaSolicitud DESC")
    List<Prestamo> findAll();

    List<Prestamo> findByClienteIdAndEstado(Long clienteId, Prestamo.EstadoPrestamo estado);
}
