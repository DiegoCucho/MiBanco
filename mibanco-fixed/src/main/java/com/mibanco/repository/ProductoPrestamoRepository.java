package com.mibanco.repository;
import com.mibanco.model.ProductoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoPrestamoRepository extends JpaRepository<ProductoPrestamo, Long> {
    List<ProductoPrestamo> findByActivoTrue();
}
