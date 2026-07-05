\# Reglas de Negocio (BR) - Sistema de Gestión de Cine



Este documento define las restricciones lógicas y de integridad que la base de datos y la aplicación deben cumplir obligatoriamente para garantizar el correcto funcionamiento del sistema.



\---



\## 1. Módulo de Infraestructura y Cartelera



\* \*\*BR-01 (Mapeo de Butacas):\*\* Cada sala posee una configuración fija de butacas físicas. No se pueden programar funciones en una sala si esta no tiene butacas registradas previamente en el sistema.

\* \*\*BR-02 (Integridad de la Programación):\*\* Una función requiere obligatoriamente la asignación de una película activa, una sala física, una fecha y una hora específica.

\* \*\*BR-03 (Restricción Horaria):\*\* No se puede programar más de una función simultáneamente en la misma sala si los horarios de proyección se solapan, considerando la duración de la película más un margen de tiempo estimado para la limpieza de la sala.



\---



\## 2. Módulo de Taquilla y Control de Aforo



\* \*\*BR-04 (Exclusividad de Asiento - Regla de Oro):\*\* Una combinación única de `Función` y `Butaca` solo puede estar asociada a \*\*una sola entrada activa\*\* en el sistema. Queda estrictamente prohibido duplicar la asignación de un asiento para una misma función.

\* \*\*BR-05 (Precios Dinámicos):\*\* El precio final de cada entrada individual se calcula a partir de la \*Tarifa Base\* de la función, modificada por las siguientes variables:

&#x20;   \* El \*\*Tipo de Asiento:\*\* Se puede aplicar un cargo extra si la butaca es catalogada como 'VIP'.

&#x20;   \* El \*\*Tipo de Entrada:\*\* Se aplican los descuentos porcentuales correspondientes según el perfil seleccionado ('Niño', 'Estudiante', 'Adulto Mayor').

\* \*\*BR-06 (Venta Multiticket):\*\* Una única transacción de venta puede contener múltiples entradas para la misma o diferentes funciones, pero cada entrada debe individualizar la butaca exacta que ocupará el espectador.



\---



\## 3. Módulo del Programa de Fidelidad e Historial de Puntos



\* \*\*BR-07 (Generación de Puntos):\*\* Cada entrada asociada a un cliente registrado que haya sido \*\*pagada con un valor mayor a $0.00\*\* sumará exactamente un (1) punto a su historial.

\* \*\*BR-08 (Cálculo del Balance Actual):\*\* El saldo de puntos acumulados de un cliente nunca se almacena como un valor estático variable en la tabla de clientes; se debe calcular en tiempo real sumando los `puntos\_ganados` y restando los `puntos\_canjeados` registrados en la tabla de historial.

\* \*\*BR-09 (Aplicación Automática - Caso María):\*\* Si al momento de procesar una venta el cálculo de puntos del cliente da exactamente \*\*9 puntos\*\*, el sistema activará de forma obligatoria el beneficio en la siguiente entrada:

&#x20;   \* Aplicará un descuento del 100% (Precio final de la entrada = $0.00).

&#x20;   \* Generará un registro en el Historial de Puntos con valor de 9 `puntos\_canjeados`.

&#x20;   \* Esta entrada gratuita tendrá un costo de cero y \*\*no acumulará\*\* nuevos puntos para el cliente.



\---



\## 4. Módulo de Transacciones y Control de Estados



\* \*\*BR-10 (Trazabilidad Absoluta):\*\* Ningún registro de venta, entrada o movimiento de puntos puede ser eliminado físicamente de la base de datos (`DELETE`). Toda modificación operativa se gestionará estrictamente mediante cambios de estado.

\* \*\*BR-11 (Ciclo de Vida de una Venta):\*\* Una venta y sus entradas asociadas se registrarán inicialmente con el estado \*\*'Activa'\*\*. Podrán cambiar a \*\*'Anulada'\*\* o \*\*'Devuelta'\*\* únicamente por motivos administrativos o solicitudes válidas de clientes.

\* \*\*BR-12 (Liberación de Inventario):\*\* En el momento exacto en que una venta pase a estado 'Anulada' o 'Devuelta', las restricciones de ocupación sobre las butacas vinculadas quedan sin efecto, volviendo a aparecer inmediatamente como \*\*'Disponibles'\*\* para futuras consultas de taquilla.

\* \*\*BR-13 (Atomicidad Transaccional):\*\* El flujo completo de venta (Verificar puntos -> Validar butaca libre -> Registrar cabecera de factura -> Registrar entradas individuales -> Actualizar historial de puntos) debe ocurrir dentro de un bloque transaccional protegido. Si cualquiera de estos pasos falla, la operación completa debe ser revertida automáticamente (`ROLLBACK`).

