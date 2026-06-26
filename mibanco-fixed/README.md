# MiBanco Perú — Sistema Core Bancario
## Proyecto Universitario Spring Boot + MySQL

---

## 🏦 Descripción
Sistema bancario completo inspirado en MiBanco Perú, desarrollado con:
- **Backend:** Spring Boot 3.2 + Spring Security + Spring Data JPA
- **Frontend:** Thymeleaf + Bootstrap 5 + Chart.js
- **Base de datos:** MySQL 8.0
- **Autenticación:** Spring Security (BCrypt)

---

## 📋 Requisitos previos
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- (Opcional) IntelliJ IDEA / Eclipse

---

## ⚙️ Instalación paso a paso

### 1. Clonar / descomprimir el proyecto
```bash
unzip mibanco-peru.zip
cd mibanco-peru
```

### 2. Crear la base de datos MySQL
```bash
mysql -u root -p < mibanco_peru_database.sql
```
O desde MySQL Workbench: `File > Run SQL Script > mibanco_peru_database.sql`

### 3. Configurar credenciales MySQL
Edita `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mibanco_peru
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
```

### 4. Ejecutar la aplicación
```bash
mvn spring-boot:run
```
O desde el IDE: ejecutar `MibancoApplication.java`

### 5. Abrir en el navegador
```
http://localhost:8080
```

---

## 🔐 Credenciales de acceso

| Tipo     | DNI/Usuario | Clave  |
|----------|-------------|--------|
| Cliente  | 47823156    | 123456 |
| Cliente  | 72341890    | 123456 |
| Admin    | admin       | 123456 |

> **Nota:** En la pantalla de login, ingresa el DNI en "Número de documento" y la clave en "Clave de Internet".

---

## 🗂️ Estructura del proyecto
```
mibanco-peru/
├── src/
│   ├── main/
│   │   ├── java/com/mibanco/
│   │   │   ├── MibancoApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   └── WebController.java
│   │   │   ├── model/
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Cuenta.java
│   │   │   │   ├── Transaccion.java
│   │   │   │   ├── Tarjeta.java
│   │   │   │   ├── PrestamoModels.java
│   │   │   │   ├── SecurityModels.java
│   │   │   │   └── OtherModels.java
│   │   │   └── repository/
│   │   │       ├── ClienteRepository.java
│   │   │       └── OtherRepositories.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/css/mibanco.css
│   │       └── templates/
│   │           ├── index.html          (Página principal pública)
│   │           ├── login.html          (Login — réplica Mibanco)
│   │           ├── dashboard.html      (Panel principal)
│   │           ├── cuentas.html        (Mis cuentas)
│   │           ├── movimientos.html    (Historial)
│   │           ├── transferencias.html (Transferencias)
│   │           ├── yapa.html           (Yapa)
│   │           ├── pagos.html          (Pago servicios)
│   │           ├── prestamos.html      (Préstamos + cronograma)
│   │           ├── tarjetas.html       (Tarjetas)
│   │           ├── simulador.html      (Simulador crédito francés)
│   │           └── fragments/
│   │               └── layout.html    (Navbar + footer)
├── mibanco_peru_database.sql           (Script completo BD)
├── pom.xml
└── README.md
```

---

## 🗄️ Base de datos — Tablas (22 tablas)

| Tabla | Descripción |
|-------|-------------|
| `sucursal` | Agencias del banco |
| `cliente` | Personas naturales y empresas |
| `rol` | Roles del sistema |
| `permiso` | Permisos granulares |
| `rol_permiso` | N:M Roles ↔ Permisos |
| `usuario` | Usuarios del sistema |
| `usuario_rol` | N:M Usuarios ↔ Roles |
| `empleado` | Personal del banco |
| `cuenta` | Cuentas bancarias |
| `tarjeta` | Tarjetas débito/crédito |
| `transaccion` | Todas las operaciones |
| `producto_prestamo` | Catálogo de créditos |
| `prestamo` | Préstamos activos |
| `cuota_prestamo` | Cronograma francés |
| `historial_pago` | Pagos realizados |
| `seguro` | Seguros asociados |
| `servicio_pago` | Pagos de servicios |
| `transferencia_yapa` | Operaciones Yapa |
| `deposito_plazo` | Depósitos a plazo fijo |
| `beneficiario` | Contactos frecuentes |
| `mora` | Registro de atrasos |
| `auditoria` | Log del sistema |

---

## 📱 Páginas disponibles

| URL | Descripción |
|-----|-------------|
| `/` | Página principal (pública) |
| `/login` | Login bancario |
| `/dashboard` | Panel principal |
| `/cuentas` | Mis cuentas |
| `/movimientos` | Historial de operaciones |
| `/transferencias` | Transferencias entre cuentas |
| `/yapa` | Envío por Yapa |
| `/pagos` | Pago de servicios |
| `/prestamos` | Mis préstamos + cronograma |
| `/tarjetas` | Gestión de tarjetas |
| `/simulador` | Simulador de crédito |

---

## 🛠️ Tecnologías utilizadas

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Spring Boot | 3.2.0 | Framework principal |
| Spring Security | 6.x | Autenticación |
| Spring Data JPA | 3.x | Persistencia |
| Thymeleaf | 3.x | Templates HTML |
| MySQL | 8.0 | Base de datos |
| Bootstrap | 5.3.2 | UI / CSS |
| Chart.js | 4.4.0 | Gráficos |
| Bootstrap Icons | 1.11.3 | Iconos |
| Lombok | 1.18+ | Reducción boilerplate |

---

## 🎓 Características del proyecto

✅ Réplica visual de MiBanco Perú (homepage + login)  
✅ Dashboard con gráficos de movimientos  
✅ Simulador de cuota francesa con cronograma exportable  
✅ Yapa con teclado numérico interactivo  
✅ Pago de servicios con historial  
✅ Préstamos con cronograma colapsable  
✅ Tarjetas con bloqueo/desbloqueo  
✅ Transferencias con confirmación modal  
✅ 22 tablas con relaciones, constraints e índices  
✅ Triggers de auditoría y validación de saldo  
✅ Stored Procedures: cronograma francés, mora, desembolso  
✅ Views: saldo_clientes, morosos, préstamos_activos  
✅ 12 consultas SQL avanzadas comentadas  

---

## 📞 Soporte
Proyecto desarrollado con fines académicos.
Inspirado en la plataforma real de MiBanco Perú.
