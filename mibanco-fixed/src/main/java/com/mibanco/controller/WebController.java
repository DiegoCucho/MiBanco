package com.mibanco.controller;

import com.mibanco.model.*;
import com.mibanco.service.BancoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import java.util.Map;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private BancoService bancoService;

    // ── Helper: usuario actual ──────────────────────────────
    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario u) return u;
        return null;
    }

    private Cliente getClienteActual() {
        Usuario u = getUsuarioActual();
        return u != null ? u.getCliente() : null;
    }

    private boolean esAdmin() {
        Usuario u = getUsuarioActual();
        if (u == null) return false;
        return u.getRoles().stream().anyMatch(r -> r.getNombre().equals("ADMIN") || r.getNombre().equals("ANALISTA"));
    }

    private void addCommonAttrs(Model model) {
        Usuario u = getUsuarioActual();
        if (u != null && u.getCliente() != null)
            model.addAttribute("nombreCliente", u.getCliente().getNombreDisplay());
        else if (u != null)
            model.addAttribute("nombreCliente", u.getUsername());
        model.addAttribute("esAdmin", esAdmin());
    }

    // ── Páginas básicas ─────────────────────────────────────
    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout, Model model) {
        if (error != null) model.addAttribute("error", "Documento o clave incorrectos.");
        if (logout != null) model.addAttribute("logout", "Sesión cerrada correctamente.");
        return "login";
    }

    // ── Redirect post-login según rol ───────────────────────
    @GetMapping("/login/redirect")
    public String loginRedirect() {
        if (esAdmin()) return "redirect:/gerente";
        return "redirect:/dashboard";
    }

    // ── Dashboard ───────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        addCommonAttrs(model);
        Cliente cliente = getClienteActual();
        if (cliente != null) {
            List<Cuenta> cuentas = bancoService.getCuentasCliente(cliente.getId());
            List<Transaccion> movimientos = bancoService.getMovimientosCliente(cliente.getId());
            List<Prestamo> prestamos = bancoService.getPrestamosCliente(cliente.getId());
            model.addAttribute("cuentas", cuentas);
            model.addAttribute("saldoTotal", bancoService.getSaldoTotal(cliente.getId()));
            model.addAttribute("movimientos", movimientos.stream().limit(5).toList());
            model.addAttribute("prestamos", prestamos);
            model.addAttribute("numCuentas", cuentas.size());
            model.addAttribute("numPrestamos", prestamos.stream().filter(p ->
                p.getEstado() == Prestamo.EstadoPrestamo.DESEMBOLSADO ||
                p.getEstado() == Prestamo.EstadoPrestamo.APROBADO).count());
        }
        return "dashboard";
    }

    // ── Cuentas ─────────────────────────────────────────────
    @GetMapping("/cuentas")
    public String cuentas(Model model) {
        addCommonAttrs(model);
        Cliente cliente = getClienteActual();
        if (cliente != null) {
            model.addAttribute("cuentas", bancoService.getCuentasCliente(cliente.getId()));
            model.addAttribute("tarjetas", bancoService.getTarjetasCliente(cliente.getId()));
        }
        return "cuentas";
    }

    // ── Movimientos ─────────────────────────────────────────
    @GetMapping("/movimientos")
    public String movimientos(Model model) {
        addCommonAttrs(model);
        Cliente cliente = getClienteActual();
        if (cliente != null) {
            model.addAttribute("movimientos", bancoService.getMovimientosCliente(cliente.getId()));
            model.addAttribute("cuentas", bancoService.getCuentasCliente(cliente.getId()));
        }
        return "movimientos";
    }

    // ── Transferencias ──────────────────────────────────────
    @GetMapping("/transferencias")
    public String transferencias(Model model) {
        addCommonAttrs(model);
        Cliente cliente = getClienteActual();
        if (cliente != null) {
            model.addAttribute("cuentas", bancoService.getCuentasCliente(cliente.getId()));
            model.addAttribute("movimientos", bancoService.getMovimientosCliente(cliente.getId())
                .stream().filter(t -> t.getTipo() == Transaccion.TipoTransaccion.TRANSFERENCIA).toList());
        }
        return "transferencias";
    }

    @PostMapping("/transferencias/realizar")
    public String realizarTransferencia(@RequestParam String numeroCuentaDest,
                                        @RequestParam BigDecimal monto,
                                        @RequestParam(required = false) String descripcion,
                                        RedirectAttributes ra) {
        try {
            Cliente cliente = getClienteActual();
            if (cliente == null) return "redirect:/login";
            bancoService.realizarTransferencia(cliente, numeroCuentaDest, monto, descripcion);
            ra.addFlashAttribute("exito", "Transferencia realizada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transferencias";
    }

    // ── Yapa ────────────────────────────────────────────────
    @GetMapping("/yapa")
    public String yapa(Model model) {
        addCommonAttrs(model);
        Cliente cliente = getClienteActual();
        if (cliente != null) {
            model.addAttribute("yapas", bancoService.getYapasCliente(cliente.getId()));
            model.addAttribute("cuentas", bancoService.getCuentasCliente(cliente.getId()));
            model.addAttribute("saldoDisponible", bancoService.getSaldoTotal(cliente.getId()));
        }
        return "yapa";
    }

    @PostMapping("/yapa/enviar")
    public String enviarYapa(@RequestParam String numeroDestino,
                             @RequestParam BigDecimal monto,
                             @RequestParam(required = false) String descripcion,
                             RedirectAttributes ra) {
        try {
            Cliente cliente = getClienteActual();
            if (cliente == null) return "redirect:/login";
            TransferenciaYapa yapa = bancoService.enviarYapa(cliente, numeroDestino, monto, descripcion);
            ra.addFlashAttribute("exito", "Yapa de S/ " + monto + " enviada correctamente. Ref: " + yapa.getReferencia());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/yapa";
    }

    // ── Pagos ───────────────────────────────────────────────
    @GetMapping("/pagos")
    public String pagos(Model model) {
        addCommonAttrs(model);
        Cliente cliente = getClienteActual();
        if (cliente != null)
            model.addAttribute("cuentas", bancoService.getCuentasCliente(cliente.getId()));
        return "pagos";
    }

    // ── Préstamos (cliente) ─────────────────────────────────
    @GetMapping("/prestamos")
    public String prestamos(Model model) {
        addCommonAttrs(model);
        Cliente cliente = getClienteActual();
        if (cliente != null) {
            model.addAttribute("prestamos", bancoService.getPrestamosCliente(cliente.getId()));
            model.addAttribute("productos", bancoService.getProductosPrestamo());
            model.addAttribute("cuentas", bancoService.getCuentasCliente(cliente.getId()));
        }
        return "prestamos";
    }

    @PostMapping("/prestamos/solicitar")
    public String solicitarPrestamo(@RequestParam Long productoId,
                                    @RequestParam BigDecimal monto,
                                    @RequestParam Integer plazoMeses,
                                    @RequestParam(defaultValue = "0") Integer periodoGracia,
                                    RedirectAttributes ra) {
        try {
            Cliente cliente = getClienteActual();
            if (cliente == null) return "redirect:/login";
            Prestamo p = bancoService.solicitarPrestamo(cliente, productoId, monto, plazoMeses, periodoGracia);
            ra.addFlashAttribute("exito", "Solicitud de préstamo enviada correctamente. #" + p.getId() + " — Estado: PENDIENTE DE REVISIÓN");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/prestamos";
    }

    @GetMapping("/prestamos/{id}/cuotas")
    public String cuotasPrestamo(@PathVariable Long id, Model model) {
        addCommonAttrs(model);
        model.addAttribute("cuotas", bancoService.getCuotasPrestamo(id));
        return "cuotas";
    }

    // ── Panel Gerente / Analista ────────────────────────────
    @GetMapping("/gerente")
    public String gerenteDashboard(Model model) {
        if (!esAdmin()) return "redirect:/dashboard";
        addCommonAttrs(model);
        java.util.Map<String, Object> stats = bancoService.getEstadisticasGerente();
        model.addAttribute("stats", stats);
        model.addAttribute("pendientes",  bancoService.getPrestamosPendientes());
        model.addAttribute("morosos",     bancoService.getPrestamosMorosos());
        model.addAttribute("activos",     bancoService.getPrestamosActivos());
        model.addAttribute("clientes",    bancoService.getTodosLosClientes());
        model.addAttribute("todosPresta", bancoService.getTodosLosPrestamos());
        return "gerente";
    }

    // ── Panel Admin ─────────────────────────────────────────
    @GetMapping("/admin/prestamos")
    public String adminPrestamos(Model model) {
        if (!esAdmin()) return "redirect:/dashboard";
        addCommonAttrs(model);
        model.addAttribute("prestamos", bancoService.getTodosLosPrestamos());
        model.addAttribute("pendientes", bancoService.getPrestamosPendientes().size());
        return "admin-prestamos";
    }

    @PostMapping("/admin/prestamos/{id}/aprobar")
    public String aprobarPrestamo(@PathVariable Long id, RedirectAttributes ra) {
        if (!esAdmin()) return "redirect:/dashboard";
        try {
            bancoService.aprobarPrestamo(id);
            ra.addFlashAttribute("exito", "Préstamo #" + id + " aprobado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/prestamos";
    }

    @PostMapping("/admin/prestamos/{id}/rechazar")
    public String rechazarPrestamo(@PathVariable Long id, RedirectAttributes ra) {
        if (!esAdmin()) return "redirect:/dashboard";
        try {
            bancoService.rechazarPrestamo(id);
            ra.addFlashAttribute("exito", "Préstamo #" + id + " rechazado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/prestamos";
    }

    @PostMapping("/admin/prestamos/{id}/desembolsar")
    public String desembolsarPrestamo(@PathVariable Long id, RedirectAttributes ra) {
        if (!esAdmin()) return "redirect:/dashboard";
        try {
            bancoService.desembolsarPrestamo(id);
            ra.addFlashAttribute("exito", "Préstamo #" + id + " desembolsado. Saldo acreditado en cuenta del cliente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/prestamos";
    }

    // ── Tarjetas / Simulador ─────────────────────────────────
    @GetMapping("/tarjetas")
    public String tarjetas(Model model) {
        addCommonAttrs(model);
        Cliente cliente = getClienteActual();
        if (cliente != null)
            model.addAttribute("tarjetas", bancoService.getTarjetasCliente(cliente.getId()));
        return "tarjetas";
    }

    @GetMapping("/simulador")
    public String simulador(Model model) {
        addCommonAttrs(model);
        model.addAttribute("productos", bancoService.getProductosPrestamo());
        return "simulador";
    }

    // ── Verificar destinatario Yapa (AJAX) ───────────────────
    @GetMapping("/yapa/verificar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarDestinatarioYapa(
            @RequestParam String numero) {
        try {
            var clienteOpt = bancoService.verificarDestinatarioYapa(numero);
            if (clienteOpt.isPresent()) {
                Cliente c = clienteOpt.get();
                return ResponseEntity.ok(Map.of(
                    "encontrado", true,
                    "nombre", c.getNombreDisplay(),
                    "estado", c.getEstado().name()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "encontrado", false,
                    "mensaje", "Numero no registrado en Mibanco"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("encontrado", false, "mensaje", e.getMessage()));
        }
    }
}
