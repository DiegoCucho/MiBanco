package com.mibanco.service;

import com.mibanco.model.*;
import com.mibanco.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BancoService {

    @Autowired private CuentaRepository       cuentaRepo;
    @Autowired private TransaccionRepository  transaccionRepo;
    @Autowired private TransferenciaYapaRepository yapaRepo;
    @Autowired private PrestamoRepository     prestamoRepo;
    @Autowired private ProductoPrestamoRepository productoRepo;
    @Autowired private CuotaPrestamoRepository cuotaRepo;
    @Autowired private TarjetaRepository      tarjetaRepo;
    @Autowired private ClienteRepository      clienteRepo;

    // ===================== CUENTAS =====================

    public List<Cuenta> getCuentasCliente(Long clienteId) {
        return cuentaRepo.findByClienteIdAndEstado(clienteId, Cuenta.EstadoCuenta.ACTIVA);
    }

    public BigDecimal getSaldoTotal(Long clienteId) {
        return getCuentasCliente(clienteId).stream()
            .filter(c -> c.getMoneda() == Cuenta.Moneda.PEN)
            .map(Cuenta::getSaldo)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ===================== TRANSACCIONES =====================

    public List<Transaccion> getMovimientosCliente(Long clienteId) {
        return transaccionRepo.findByClienteId(clienteId);
    }

    // ===================== YAPA =====================

    /**
     * Verifica si un número de teléfono pertenece a un cliente registrado en Mibanco.
     * Devuelve el cliente si existe, o empty si es un número externo.
     */
    public Optional<Cliente> verificarDestinatarioYapa(String numeroDestino) {
        // Limpiar el número: quitar espacios, guiones, prefijo +51
        String limpio = numeroDestino.replaceAll("[\\s\\-]", "").replaceFirst("^\\+?51", "");
        return clienteRepo.findByTelefono(limpio);
    }

    @Transactional
    public TransferenciaYapa enviarYapa(Cliente remitente, String numeroDest, BigDecimal monto, String descripcion) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("El monto debe ser mayor a 0");
        if (monto.compareTo(new BigDecimal("500")) > 0)
            throw new RuntimeException("El monto máximo por Yapa es S/ 500.00");

        // Limpiar número destino
        String numeroLimpio = numeroDest.replaceAll("[\\s\\-]", "").replaceFirst("^\\+?51", "");

        // Verificar que no se envíe a sí mismo
        if (remitente.getTelefono() != null && remitente.getTelefono().equals(numeroLimpio))
            throw new RuntimeException("No puedes enviarte una Yapa a ti mismo");

        // Buscar si el destinatario existe en Mibanco
        Optional<Cliente> destinatarioOpt = clienteRepo.findByTelefono(numeroLimpio);
        if (destinatarioOpt.isEmpty())
            throw new RuntimeException(
                "El número " + numeroLimpio + " no está registrado en Mibanco. " +
                "Solo puedes enviar Yapas a clientes Mibanco."
            );

        Cliente destinatario = destinatarioOpt.get();
        if (destinatario.getEstado() != Cliente.EstadoCliente.ACTIVO)
            throw new RuntimeException("El destinatario no tiene una cuenta activa en Mibanco");

        // Buscar cuenta origen PEN activa del remitente
        List<Cuenta> cuentas = cuentaRepo.findByClienteIdAndEstado(remitente.getId(), Cuenta.EstadoCuenta.ACTIVA);
        Cuenta cuentaOrigen = cuentas.stream()
            .filter(c -> c.getMoneda() == Cuenta.Moneda.PEN)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No tienes cuenta en soles activa"));

        if (cuentaOrigen.getSaldo().compareTo(monto) < 0)
            throw new RuntimeException("Saldo insuficiente. Disponible: S/ " +
                cuentaOrigen.getSaldo().setScale(2, RoundingMode.HALF_UP));

        // Buscar cuenta destino PEN del destinatario para acreditar
        List<Cuenta> cuentasDest = cuentaRepo.findByClienteIdAndEstado(destinatario.getId(), Cuenta.EstadoCuenta.ACTIVA);
        Cuenta cuentaDest = cuentasDest.stream()
            .filter(c -> c.getMoneda() == Cuenta.Moneda.PEN)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("El destinatario no tiene cuenta activa en soles"));

        // Mover saldo
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(monto));
        cuentaDest.setSaldo(cuentaDest.getSaldo().add(monto));
        cuentaRepo.save(cuentaOrigen);
        cuentaRepo.save(cuentaDest);

        String ref = "YAP-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        TransferenciaYapa yapa = TransferenciaYapa.builder()
            .remitenteCliente(remitente)
            .clienteDestino(destinatario)
            .numeroDestino(numeroLimpio)
            .monto(monto)
            .estado(TransferenciaYapa.EstadoYapa.COMPLETADO)
            .referencia(ref)
            .build();
        yapaRepo.save(yapa);

        // Registrar en historial de transacciones
        Transaccion tx = Transaccion.builder()
            .tipo(Transaccion.TipoTransaccion.YAPA)
            .monto(monto)
            .cuentaOrigen(cuentaOrigen)
            .cuentaDestino(cuentaDest)
            .canal(Transaccion.Canal.APP)
            .descripcion("Yapa a " + destinatario.getNombreDisplay() + " (" + numeroLimpio + ")"
                + (descripcion != null && !descripcion.isBlank() ? " - " + descripcion : ""))
            .referencia(ref)
            .build();
        transaccionRepo.save(tx);

        return yapa;
    }

    public List<TransferenciaYapa> getYapasCliente(Long clienteId) {
        return yapaRepo.findByRemitenteClienteIdOrderByFechaDesc(clienteId);
    }

    // ===================== TRANSFERENCIAS =====================

    @Transactional
    public Transaccion realizarTransferencia(Cliente cliente, String numeroCuentaDest, BigDecimal monto, String descripcion) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("El monto debe ser mayor a 0");

        List<Cuenta> cuentas = cuentaRepo.findByClienteIdAndEstado(cliente.getId(), Cuenta.EstadoCuenta.ACTIVA);
        Cuenta cuentaOrigen = cuentas.stream()
            .filter(c -> c.getMoneda() == Cuenta.Moneda.PEN)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No tienes cuenta en soles activa"));

        if (cuentaOrigen.getSaldo().compareTo(monto) < 0)
            throw new RuntimeException("Saldo insuficiente");

        Cuenta cuentaDest = cuentaRepo.findByNumeroCuenta(numeroCuentaDest).orElse(null);

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(monto));
        cuentaRepo.save(cuentaOrigen);

        if (cuentaDest != null) {
            cuentaDest.setSaldo(cuentaDest.getSaldo().add(monto));
            cuentaRepo.save(cuentaDest);
        }

        String ref = "TRF-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaccion tx = Transaccion.builder()
            .tipo(Transaccion.TipoTransaccion.TRANSFERENCIA)
            .monto(monto)
            .cuentaOrigen(cuentaOrigen)
            .cuentaDestino(cuentaDest)
            .canal(Transaccion.Canal.APP)
            .descripcion(descripcion != null && !descripcion.isBlank() ? descripcion : "Transferencia a " + numeroCuentaDest)
            .referencia(ref)
            .build();
        return transaccionRepo.save(tx);
    }

    // ===================== PRÉSTAMOS =====================

    public List<ProductoPrestamo> getProductosPrestamo() {
        return productoRepo.findByActivoTrue();
    }

    public List<Prestamo> getPrestamosCliente(Long clienteId) {
        return prestamoRepo.findByClienteId(clienteId);
    }

    public List<Prestamo> getPrestamosPendientes() {
        return prestamoRepo.findByEstado(Prestamo.EstadoPrestamo.PENDIENTE);
    }

    public List<Prestamo> getTodosLosPrestamos() {
        return prestamoRepo.findAll();
    }

    @Transactional
    public Prestamo solicitarPrestamo(Cliente cliente, Long productoId, BigDecimal monto, Integer plazoMeses, Integer periodoGracia) {
        ProductoPrestamo producto = productoRepo.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (plazoMeses < producto.getPlazoMinimo() || plazoMeses > producto.getPlazoMaximo())
            throw new RuntimeException("Plazo inválido (" + producto.getPlazoMinimo() + "-" + producto.getPlazoMaximo() + " meses)");

        if (monto.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("El monto debe ser mayor a 0");

        BigDecimal tasaMensual = producto.getTceaMaxima().divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        int nEfectivo = plazoMeses - (periodoGracia != null ? periodoGracia : 0);
        BigDecimal cuota = BigDecimal.ZERO;
        if (nEfectivo > 0) {
            double i = tasaMensual.doubleValue();
            double c = monto.doubleValue() * i / (1 - Math.pow(1 + i, -nEfectivo));
            cuota = new BigDecimal(c).setScale(2, RoundingMode.HALF_UP);
        }

        Prestamo prestamo = Prestamo.builder()
            .cliente(cliente)
            .producto(producto)
            .montoTotal(monto)
            .tasaInteresMensual(tasaMensual)
            .plazoMeses(plazoMeses)
            .cuotaMensual(cuota)
            .periodoGraciaMeses(periodoGracia != null ? periodoGracia : 0)
            .tcea(producto.getTceaMaxima())
            .estado(Prestamo.EstadoPrestamo.PENDIENTE)
            .fechaSolicitud(LocalDate.now())
            .build();

        return prestamoRepo.save(prestamo);
    }

    @Transactional
    public Prestamo aprobarPrestamo(Long prestamoId) {
        Prestamo p = prestamoRepo.findById(prestamoId)
            .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
        if (p.getEstado() != Prestamo.EstadoPrestamo.PENDIENTE)
            throw new RuntimeException("Solo se pueden aprobar préstamos PENDIENTES");
        p.setEstado(Prestamo.EstadoPrestamo.APROBADO);
        p.setFechaAprobacion(LocalDate.now());
        return prestamoRepo.save(p);
    }

    @Transactional
    public Prestamo rechazarPrestamo(Long prestamoId) {
        Prestamo p = prestamoRepo.findById(prestamoId)
            .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
        if (p.getEstado() != Prestamo.EstadoPrestamo.PENDIENTE)
            throw new RuntimeException("Solo se pueden rechazar préstamos PENDIENTES");
        p.setEstado(Prestamo.EstadoPrestamo.CANCELADO);
        return prestamoRepo.save(p);
    }

    @Transactional
    public Prestamo desembolsarPrestamo(Long prestamoId) {
        Prestamo p = prestamoRepo.findById(prestamoId)
            .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
        if (p.getEstado() != Prestamo.EstadoPrestamo.APROBADO)
            throw new RuntimeException("El préstamo debe estar APROBADO para desembolsar");

        List<Cuenta> cuentas = cuentaRepo.findByClienteIdAndEstado(p.getCliente().getId(), Cuenta.EstadoCuenta.ACTIVA);
        Cuenta cuenta = cuentas.stream()
            .filter(c -> c.getMoneda() == Cuenta.Moneda.PEN)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cliente no tiene cuenta activa en soles"));

        cuenta.setSaldo(cuenta.getSaldo().add(p.getMontoTotal()));
        cuentaRepo.save(cuenta);

        generarCronograma(p);

        p.setEstado(Prestamo.EstadoPrestamo.DESEMBOLSADO);
        p.setFechaDesembolso(LocalDate.now());

        String ref = "DSB-" + LocalDate.now().getYear() + "-" + String.format("%06d", p.getId());
        Transaccion tx = Transaccion.builder()
            .tipo(Transaccion.TipoTransaccion.DEPOSITO)
            .monto(p.getMontoTotal())
            .cuentaDestino(cuenta)
            .canal(Transaccion.Canal.AGENCIA)
            .descripcion("Desembolso préstamo #" + p.getId() + " - " + p.getProducto().getNombre())
            .referencia(ref)
            .build();
        transaccionRepo.save(tx);

        return prestamoRepo.save(p);
    }

    private void generarCronograma(Prestamo p) {
        List<CuotaPrestamo> existentes = cuotaRepo.findByPrestamoIdOrderByNumeroCuota(p.getId());
        if (!existentes.isEmpty()) cuotaRepo.deleteAll(existentes);

        double tasa   = p.getTasaInteresMensual().doubleValue();
        double monto  = p.getMontoTotal().doubleValue();
        int plazo     = p.getPlazoMeses();
        int gracia    = p.getPeriodoGraciaMeses() != null ? p.getPeriodoGraciaMeses() : 0;
        int nEfectivo = plazo - gracia;

        double cuota = nEfectivo > 0 ? monto * tasa / (1 - Math.pow(1 + tasa, -nEfectivo)) : 0;
        double saldo  = monto;

        List<CuotaPrestamo> cuotas = new ArrayList<>();
        LocalDate fechaInicio = LocalDate.now();

        for (int i = 1; i <= plazo; i++) {
            LocalDate fechaCuota = fechaInicio.plusMonths(i);
            double interes = saldo * tasa;
            double capital = 0;

            if (i > gracia) {
                capital = (i == plazo) ? saldo : cuota - interes;
            }
            saldo = Math.max(0, saldo - capital);

            cuotas.add(CuotaPrestamo.builder()
                .prestamo(p)
                .numeroCuota(i)
                .fechaPagoProgramada(fechaCuota)
                .capital(new BigDecimal(capital).setScale(2, RoundingMode.HALF_UP))
                .interes(new BigDecimal(interes).setScale(2, RoundingMode.HALF_UP))
                .seguro(BigDecimal.ZERO)
                .mora(BigDecimal.ZERO)
                .montoTotal(new BigDecimal(capital + interes).setScale(2, RoundingMode.HALF_UP))
                .saldoCapital(new BigDecimal(saldo).setScale(2, RoundingMode.HALF_UP))
                .estado(CuotaPrestamo.EstadoCuota.PENDIENTE)
                .build());
        }
        cuotaRepo.saveAll(cuotas);
    }

    public List<CuotaPrestamo> getCuotasPrestamo(Long prestamoId) {
        return cuotaRepo.findByPrestamoIdOrderByNumeroCuota(prestamoId);
    }

    // ===================== TARJETAS =====================

    public List<Tarjeta> getTarjetasCliente(Long clienteId) {
        return tarjetaRepo.findByClienteId(clienteId);
    }

    // ===================== GERENTE / ANALISTA =====================

    public List<Cliente> getTodosLosClientes() {
        return clienteRepo.findAll();
    }

    public List<Cliente> getClientesActivos() {
        return clienteRepo.findByEstado(Cliente.EstadoCliente.ACTIVO);
    }

    public List<Prestamo> getPrestamosMorosos() {
        return prestamoRepo.findByEstado(Prestamo.EstadoPrestamo.MOROSO);
    }

    public java.util.Map<String, Object> getEstadisticasGerente() {
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        List<Prestamo> todos = prestamoRepo.findAll();

        long totalClientes   = clienteRepo.count();
        long clientesActivos = clienteRepo.findByEstado(Cliente.EstadoCliente.ACTIVO).size();
        long pendientes      = todos.stream().filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.PENDIENTE).count();
        long aprobados       = todos.stream().filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.APROBADO).count();
        long desembolsados   = todos.stream().filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.DESEMBOLSADO).count();
        long morosos         = todos.stream().filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.MOROSO).count();
        long cancelados      = todos.stream().filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.CANCELADO).count();

        BigDecimal carteraTotal = todos.stream()
            .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.DESEMBOLSADO || p.getEstado() == Prestamo.EstadoPrestamo.MOROSO)
            .map(Prestamo::getMontoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalClientes",   totalClientes);
        stats.put("clientesActivos", clientesActivos);
        stats.put("pendientes",      pendientes);
        stats.put("aprobados",       aprobados);
        stats.put("desembolsados",   desembolsados);
        stats.put("morosos",         morosos);
        stats.put("cancelados",      cancelados);
        stats.put("carteraTotal",    carteraTotal);
        stats.put("totalPrestamos",  (long) todos.size());

        // Cartera vigente (DESEMBOLSADO) vs vencida (MOROSO)
        BigDecimal carteraVigente = todos.stream()
            .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.DESEMBOLSADO)
            .map(Prestamo::getMontoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal carteraVencida = todos.stream()
            .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.MOROSO)
            .map(Prestamo::getMontoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ratio de mora = carteraVencida / carteraTotal * 100
        BigDecimal ratioMora = BigDecimal.ZERO;
        if (carteraTotal.compareTo(BigDecimal.ZERO) > 0) {
            ratioMora = carteraVencida
                .multiply(new BigDecimal("100"))
                .divide(carteraTotal, 1, RoundingMode.HALF_UP);
        }

        stats.put("carteraVigente", carteraVigente);
        stats.put("carteraVencida", carteraVencida);
        stats.put("ratioMora",      ratioMora);
        return stats;
    }

    public List<Prestamo> getPrestamosActivos() {
        List<Prestamo> result = new java.util.ArrayList<>();
        result.addAll(prestamoRepo.findByEstado(Prestamo.EstadoPrestamo.DESEMBOLSADO));
        result.addAll(prestamoRepo.findByEstado(Prestamo.EstadoPrestamo.MOROSO));
        return result;
    }
}
