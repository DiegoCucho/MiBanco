package com.mibanco.repository;
import com.mibanco.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
    // JOIN FETCH cuenta para evitar LazyInitializationException en tarjetas.html
    @Query("SELECT t FROM Tarjeta t JOIN FETCH t.cuenta WHERE t.cliente.id = :clienteId")
    List<Tarjeta> findByClienteId(@Param("clienteId") Long clienteId);
}
