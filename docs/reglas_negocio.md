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

## 2. Módulo de usuarios y empleados

- **BR-04: Acceso al sistema**  
  El sistema será utilizado por empleados del cine mediante usuarios de acceso. Los clientes no iniciarán sesión en el sistema.

- **BR-05: Relación empleado-usuario**  
  Un usuario de acceso debe estar asociado a un empleado. El empleado representa la persona que trabaja en el cine, mientras que el usuario representa sus credenciales para entrar al sistema.

- **BR-06: Roles de usuario**  
  Cada usuario podrá tener un rol asignado, como administrador, cajero o supervisor. El rol determinará las funciones a las que podrá acceder dentro del sistema.

- **BR-07: Registro de ventas por empleado**  
  Toda venta registrada en el sistema debe estar asociada al empleado que realizó la operación.

---

## 3. Módulo de clientes

- **BR-08: Registro de clientes**  
  Los clientes serán registrados para asociar sus compras y movimientos de fidelidad. No tendrán usuario ni contraseña de acceso al sistema.

- **BR-09: Cliente genérico**  
  Si el equipo decide permitir ventas sin cliente registrado, estas podrán asociarse a un cliente genérico llamado “Consumidor final”. Este cliente no deberá acumular puntos de fidelidad.

---

## 4. Módulo de taquilla y control de aforo

- **BR-10: Exclusividad de asiento**  
  Una combinación única de función y butaca solo puede estar asociada a una entrada activa en el sistema. Queda prohibido duplicar la asignación de una misma butaca para una misma función.

- **BR-11: Disponibilidad de butacas**  
  Una butaca asociada a una entrada activa no debe aparecer como disponible para la misma función.

- **BR-12: Butacas en ventas anuladas o devueltas**  
  Si una venta o entrada cambia a estado Anulada o Devuelta, esa entrada no debe considerarse como ocupación activa de la butaca.

- **BR-13: Venta multiticket**  
  Una única transacción de venta puede contener una o varias entradas. Cada entrada debe identificar la función, la butaca y el tipo de entrada correspondiente.

---

## 5. Módulo de precios y tipos de entrada

- **BR-14: Tarifa base de función**  
  Cada función tendrá una tarifa base que servirá como punto de partida para calcular el precio de las entradas.

- **BR-15: Tipo de butaca**  
  Las butacas podrán tener un tipo, como Regular o VIP. Si la butaca es VIP, se podrá aplicar un cargo adicional.

- **BR-16: Tipo de entrada**  
  Cada entrada tendrá un tipo asignado, como Adulto, Niño, Estudiante o Adulto Mayor.

- **BR-17: Cálculo del precio final**  
  El precio final de cada entrada se calculará tomando en cuenta la tarifa base de la función, el tipo de butaca y el tipo de entrada.

---

## 6. Módulo del programa de fidelidad

- **BR-18: Generación de puntos**  
  Cada entrada asociada a un cliente registrado y pagada con un valor mayor a 0 generará un punto en el historial del cliente.

- **BR-19: Entradas gratuitas**  
  Las entradas gratuitas por fidelidad tendrán precio final igual a 0 y no generarán nuevos puntos.

- **BR-20: Cálculo del balance de puntos**  
  El saldo de puntos del cliente se calculará a partir del historial de puntos, sumando los puntos ganados y restando los puntos canjeados.

- **BR-21: Aplicación automática del beneficio de fidelidad**  
  Si un cliente tiene 9 puntos acumulados antes de realizar una nueva compra, el sistema deberá aplicar el beneficio de fidelidad a una entrada, registrándola con descuento del 100%.

- **BR-22: Registro del canje**  
  Cuando se aplique el beneficio de fidelidad, el sistema deberá registrar en el historial de puntos el canje correspondiente.

---

## 7. Módulo de transacciones y control de estados

- **BR-23: Trazabilidad de operaciones**  
  Ningún registro de venta, entrada o movimiento de puntos deberá eliminarse físicamente de la base de datos. Las modificaciones operativas se gestionarán mediante cambios de estado.

- **BR-24: Ciclo de vida de una venta**  
  Una venta se registrará inicialmente con estado Activa. Podrá cambiar a Anulada o Devuelta según corresponda.

- **BR-25: Ciclo de vida de una entrada**  
  Una entrada se registrará inicialmente con estado Activa. Si la venta asociada es anulada o devuelta, sus entradas deberán actualizarse al estado correspondiente.

- **BR-26: Atomicidad transaccional**  
  El flujo completo de una venta debe ejecutarse dentro de una transacción. Si falla la validación de puntos, la validación de butaca, el registro de la venta, el registro de entradas o el registro de puntos, la operación completa deberá revertirse.

---

## Estado del documento

- [x] Reglas de infraestructura definidas.
- [x] Reglas de empleados y usuarios definidas.
- [x] Reglas de clientes definidas.
- [x] Reglas de control de butacas definidas.
- [x] Reglas de precios y tipos de entrada definidas.
- [x] Reglas de fidelidad definidas.
- [x] Reglas de trazabilidad y estados definidas.
- [ ] Ajustes finales según criterios oficiales del profesor.
