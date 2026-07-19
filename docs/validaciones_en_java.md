# Validaciones que serán manejadas en Java

Este documento describe las validaciones que serán implementadas desde la aplicación Java y no directamente mediante restricciones del modelo físico de la base de datos.

El modelo físico garantiza la integridad mediante claves primarias, claves foráneas, restricciones `NOT NULL`, `UNIQUE`, `CHECK` y valores por defecto. Sin embargo, existen reglas que requieren consultas, cálculos o validaciones entre varias tablas, por lo que serán controladas desde la lógica de negocio de la aplicación.

---

## 1. Validación de disponibilidad de butacas

**Regla:** Antes de registrar una entrada, el sistema debe verificar que la combinación `(id_funcion, id_butaca)` no esté asociada a una entrada con estado **Reservada**, **Pagada** o **Utilizada**.

Consulta de referencia:

```sql
SELECT COUNT(*)
FROM entrada
WHERE id_funcion = ?
  AND id_butaca = ?
  AND estado IN ('RESERVADA','PAGADA','UTILIZADA');
```

Método sugerido:

```java
public boolean butacaDisponible(int idFuncion, int idButaca)
```

---

## 2. Validación de pertenencia de la butaca

Java debe comprobar que la butaca pertenezca a la sala donde se realizará la función.

```java
public boolean butacaPerteneceASalaFuncion(int idFuncion, int idButaca)
```

---

## 3. Validación de choque de horarios

Antes de registrar una función se debe verificar que no exista otra función en la misma sala cuyo horario se solape.

```java
public boolean existeChoqueHorario(...)
```

---

## 4. Cálculo del subtotal de la venta

Java calculará:

- subtotal = suma de los precios finales de las entradas.
- total_pagado = subtotal - descuento_total.

```java
public BigDecimal calcularSubtotalVenta(List<Entrada> entradas)
```

---

## 5. Cálculo del precio final

El modelo final ya no utiliza cargo por butaca.

Precio final:

precio_base - descuento

```java
public BigDecimal calcularPrecioFinal(BigDecimal precioBase,
                                      BigDecimal descuento)
```

---

## 6. Validación del programa de fidelidad

Java calculará el saldo de puntos consultando `historial_puntos`.

Tipo de movimiento:

- ACUMULACIÓN
- CANJE

Cuando el cliente posea 9 puntos disponibles podrá recibir una entrada gratuita.

```java
public int calcularPuntosDisponibles(int idCliente)

public boolean puedeCanjearEntradaGratis(int idCliente)
```

---

## 7. Registro de movimientos de puntos

Después de una compra válida Java registrará un movimiento de tipo **ACUMULACIÓN**.

Cuando exista un canje registrará un movimiento de tipo **CANJE**.

---

## 8. Validación del canal de venta

- TAQUILLA → requiere empleado.
- EN_LINEA → empleado opcional.

```java
public boolean validarEmpleadoSegunCanal(...)
```

---

## 9. Validación del usuario

Los únicos roles del sistema son:

- ADMINISTRADOR
- CAJERO
- CLIENTE

Java verificará que:

- Administrador y Cajero estén asociados a un empleado.
- Cliente esté asociado a un cliente.

```java
public boolean validarUsuarioSegunRol(...)
```

---

## 10. Validación de estados

Antes de vender una entrada Java verificará que:

- película activa;
- sala activa;
- función válida;
- butaca activa;
- método de pago activo;
- tipo de entrada activo.

---

## 11. Venta como transacción

Todo el proceso deberá ejecutarse dentro de una transacción.

```java
connection.setAutoCommit(false);

// venta
// entradas
// historial de puntos

connection.commit();
```

Ante cualquier error:

```java
connection.rollback();
```

---

## 12. Resumen de responsabilidades

| Regla | Base de datos | Java |
|-------|---------------|------|
| Claves primarias | ✔ | |
| Claves foráneas | ✔ | |
| UNIQUE | ✔ | |
| CHECK | ✔ | |
| Disponibilidad de butaca | | ✔ |
| Choque de horarios | | ✔ |
| Cálculo de puntos | | ✔ |
| Venta transaccional | | ✔ |
| Cálculo de montos | Parcial | ✔ |

---

## Conclusión

El modelo físico protege la integridad estructural de la base de datos, mientras que Java implementa las reglas dinámicas del negocio, como la disponibilidad de butacas, el cálculo de montos, la validación del programa de fidelidad y el manejo transaccional de las ventas. Esta separación mantiene el diseño de la base de datos limpio, consistente y fácil de mantener.
