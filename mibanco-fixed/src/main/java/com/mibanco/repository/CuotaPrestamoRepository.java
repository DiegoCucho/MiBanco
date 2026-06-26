package com.mibanco.repository;
import com.mibanco.model.CuotaPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CuotaPrestamoRepository extends JpaRepository<CuotaPrestamo, Long> {
    List<CuotaPrestamo> findByPrestamoIdOrderByNumeroCuota(Long prestamoId);
}
