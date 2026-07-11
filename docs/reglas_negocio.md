# Reglas de Negocio - Sistema de Gestión de Cine

Este documento define las restricciones lógicas y de integridad que la base de datos y la aplicación deben cumplir para garantizar el correcto funcionamiento del sistema.

---

## 1. Módulo de infraestructura y cartelera

- **BR-01: Mapeo de butacas**  
  Cada sala posee una configuración fija de butacas físicas. No se pueden programar funciones en una sala si esta no tiene butacas registradas previamente en el sistema.

- **BR-02: Integridad de la programación**  
  Una función requiere obligatoriamente la asignación de una película activa, una sala física, una fecha y una hora específica.

- **BR-03: Restricción horaria**  
  No se puede programar más de una función simultáneamente en la misma sala si los horarios de proyección se solapan, considerando la duración de la película más un margen de tiempo estimado para la limpieza de la sala.

---

## 2. Módulo de usuarios, clientes y empleados

- **BR-04: Usuario general del sistema**  
  El sistema manejará una entidad general llamada Usuario, la cual permitirá el acceso tanto de empleados como de clientes.

- **BR-05: Roles de usuario**  
  Cada usuario tendrá un rol asignado para determinar sus permisos dentro del sistema. Los roles iniciales serán ADMIN, CAJERO, SUPERVISOR y CLIENTE.

- **BR-06: Usuario de empleado**  
  Un empleado podrá tener un usuario para acceder al sistema interno del cine. Según su rol, podrá gestionar ventas, funciones, reportes, usuarios u otros módulos administrativos.

- **BR-07: Usuario de cliente**  
  Un cliente podrá tener un usuario para acceder al sistema en línea, consultar funciones, seleccionar butacas disponibles y realizar compras.

- **BR-08: Relación usuario-empleado**  
  Un usuario con rol ADMIN, CAJERO o SUPERVISOR deberá estar asociado a un empleado del cine.

- **BR-09: Relación usuario-cliente**  
  Un usuario con rol CLIENTE deberá estar asociado a un cliente registrado en el sistema.

- **BR-10: Registro de clientes**  
  Los clientes serán registrados para asociar sus compras, entradas y movimientos de fidelidad.

---

## 3. Módulo de canales de venta

- **BR-11: Canales de venta**  
  El sistema permitirá dos canales principales de venta: TAQUILLA y EN_LINEA.

- **BR-12: Venta por taquilla**  
  En una venta por taquilla, la operación será registrada por un empleado autorizado del cine.

- **BR-13: Venta en línea**  
  En una venta en línea, la operación será realizada por un cliente mediante su usuario de acceso.

- **BR-14: Canal de venta obligatorio**  
  Toda venta deberá registrar el canal por el cual fue realizada, indicando si fue una venta por TAQUILLA o una venta EN_LINEA.

- **BR-15: Relación empleado-venta**  
  Una venta por taquilla debe estar asociada al empleado que la registró. Una venta en línea puede no tener empleado asociado.

- **BR-16: Relación cliente-venta**  
  Toda venta debe estar asociada a un cliente, ya sea que la compra se realice por taquilla o en línea.

---

## 4. Módulo de taquilla, compra en línea y control de aforo

- **BR-17: Exclusividad de asiento**  
  Una combinación única de función y butaca solo puede estar asociada a una entrada activa en el sistema. Queda prohibido duplicar la asignación de una misma butaca para una misma función.

- **BR-18: Disponibilidad de butacas**  
  Una butaca asociada a una entrada activa no debe aparecer como disponible para la misma función.

- **BR-19: Selección de butaca**  
  Tanto en una venta por taquilla como en una venta en línea, la butaca seleccionada debe estar disponible antes de registrarse la entrada.

- **BR-20: Butacas en ventas anuladas o devueltas**  
  Si una venta o entrada cambia a estado Anulada o Devuelta, esa entrada no debe considerarse como ocupación activa de la butaca.

- **BR-21: Venta multiticket**  
  Una única transacción de venta puede contener una o varias entradas. Cada entrada debe identificar la función, la butaca y el tipo de entrada correspondiente.

---

## 5. Módulo de precios y tipos de entrada

- **BR-22: Tarifa base de función**  
  Cada función tendrá una tarifa base que servirá como punto de partida para calcular el precio de las entradas.

- **BR-23: Tipo de butaca**  
  Las butacas podrán tener un tipo, como Regular o VIP. Si la butaca es VIP, se podrá aplicar un cargo adicional.

- **BR-24: Tipo de entrada**  
  Cada entrada tendrá un tipo asignado, como Adulto, Niño, Estudiante o Adulto Mayor.

- **BR-25: Cálculo del precio final**  
  El precio final de cada entrada se calculará tomando en cuenta la tarifa base de la función, el tipo de butaca y el tipo de entrada.

---

## 6. Módulo del programa de fidelidad

- **BR-26: Generación de puntos**  
  Cada entrada asociada a un cliente registrado y pagada con un valor mayor a 0 generará un punto en el historial del cliente.

- **BR-27: Entradas gratuitas**  
  Las entradas gratuitas por fidelidad tendrán precio final igual a 0 y no generarán nuevos puntos.

- **BR-28: Cálculo del balance de puntos**  
  El saldo de puntos del cliente se calculará a partir del historial de puntos, sumando los puntos ganados y restando los puntos canjeados.

- **BR-29: Aplicación automática del beneficio de fidelidad**  
  Si un cliente tiene 9 puntos acumulados antes de realizar una nueva compra, el sistema deberá aplicar el beneficio de fidelidad a una entrada, registrándola con descuento del 100%.

- **BR-30: Registro del canje**  
  Cuando se aplique el beneficio de fidelidad, el sistema deberá registrar en el historial de puntos el canje correspondiente.

- **BR-31: Fidelidad en ambos canales**  
  El programa de fidelidad aplicará tanto para ventas por taquilla como para ventas en línea, siempre que la venta esté asociada a un cliente registrado.

---

## 7. Módulo de transacciones y control de estados

- **BR-32: Trazabilidad de operaciones**  
  Ningún registro de venta, entrada o movimiento de puntos deberá eliminarse físicamente de la base de datos. Las modificaciones operativas se gestionarán mediante cambios de estado.

- **BR-33: Ciclo de vida de una venta**  
  Una venta se registrará inicialmente con estado Activa. Podrá cambiar a Anulada o Devuelta según corresponda.

- **BR-34: Ciclo de vida de una entrada**  
  Una entrada se registrará inicialmente con estado Activa. Si la venta asociada es anulada o devuelta, sus entradas deberán actualizarse al estado correspondiente.

- **BR-35: Atomicidad transaccional**  
  El flujo completo de una venta debe ejecutarse dentro de una transacción. Si falla la validación de puntos, la validación de butaca, el registro de la venta, el registro de entradas o el registro de puntos, la operación completa deberá revertirse.

- **BR-36: Consistencia entre canal y usuario**  
  Si una venta tiene canal TAQUILLA, debe estar asociada a un empleado. Si una venta tiene canal EN_LINEA, debe estar asociada a un cliente que realizó la compra mediante su usuario.

---

## Estado del documento

- [x] Reglas de infraestructura definidas.
- [x] Reglas de usuarios, clientes y empleados actualizadas.
- [x] Reglas de canales de venta definidas.
- [x] Reglas de control de butacas definidas.
- [x] Reglas de precios y tipos de entrada definidas.
- [x] Reglas de fidelidad definidas.
- [x] Reglas de trazabilidad y estados definidas.
- [x] Ajuste del diseño para permitir ventas por taquilla y ventas en línea.
- [ ] Verificar alineación con requerimientos.md tras el ajuste de canales de venta.
