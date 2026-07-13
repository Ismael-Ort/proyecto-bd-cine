# Validaciones que serán manejadas en Java

Este documento describe las reglas de negocio que no fueron implementadas directamente como restricciones del modelo físico de la base de datos, sino que serán controladas desde la aplicación Java.

El modelo físico mantiene las restricciones principales de integridad, tales como claves primarias, claves foráneas, restricciones `NOT NULL`, `UNIQUE`, `CHECK`, valores por defecto y relaciones entre tablas. Sin embargo, algunas reglas requieren consultas previas, cálculos o comparación de datos entre varias tablas, por lo que resulta más práctico validarlas desde la lógica del sistema.

---

## 1. Validación de disponibilidad de butacas

### Descripción

Antes de registrar una entrada, el sistema debe verificar que la butaca seleccionada no esté ocupada para la misma función.

En el modelo físico, la tabla `entrada` contiene los campos:
id_funcion
id_butaca
estado

Sin embargo, en la versión sencilla del modelo físico no se colocó una restricción avanzada para impedir automáticamente que se repita una butaca activa en una misma función.

Por eso, antes de insertar una nueva entrada, Java deberá consultar si ya existe una entrada activa con el mismo id_funcion y el mismo id_butaca.

Regla

Una butaca no puede venderse dos veces para la misma función si la entrada anterior está activa.

Consulta de referencia
SELECT COUNT(*) AS cantidad
FROM entrada
WHERE id_funcion = ?
AND id_butaca = ?
AND estado = 'Activa';
Resultado esperado

Si la consulta devuelve un valor mayor que cero, el sistema debe impedir la venta de esa entrada.

Método sugerido en Java
public boolean butacaDisponible(int idFuncion, int idButaca) {
    // Retorna true si no existe una entrada activa
    // para esa función y esa butaca.
}

## 2. Validación de que la butaca pertenezca a la sala de la función
Descripción

La tabla entrada guarda el id_funcion y el id_butaca, pero el modelo físico no compara directamente si la butaca pertenece a la misma sala donde se proyecta la función.

La función pertenece a una sala mediante:

funcion.id_sala

La butaca pertenece a una sala mediante:

butaca.id_sala

Por eso, antes de vender una entrada, Java debe confirmar que ambas salas coincidan.

Regla

Solo se puede vender una butaca que pertenezca a la sala donde se realizará la función.

Consulta de referencia
SELECT COUNT(*) AS cantidad
FROM funcion f
INNER JOIN butaca b ON f.id_sala = b.id_sala
WHERE f.id_funcion = ?
AND b.id_butaca = ?;
Resultado esperado

Si la consulta devuelve cero, significa que la butaca no pertenece a la sala de la función y el sistema debe rechazar la venta.

Método sugerido en Java
public boolean butacaPerteneceASalaFuncion(int idFuncion, int idButaca) {
    // Retorna true si la butaca pertenece a la sala
    // donde se realizará la función.
}

## 3. Validación de choque de horarios entre funciones
Descripción

El modelo físico de la tabla funcion valida que la hora final sea mayor que la hora inicial mediante la restricción:

CHECK (hora_fin > hora_inicio)

Sin embargo, la base de datos no impide por sí sola que se registren dos funciones en la misma sala, en la misma fecha y con horarios que se crucen.

Por eso, antes de registrar o modificar una función, Java deberá verificar que no exista otra función en la misma sala con un horario solapado.

Regla

No pueden existir dos funciones en la misma sala, en la misma fecha, con horarios que se crucen.

Consulta de referencia
SELECT COUNT(*) AS cantidad
FROM funcion
WHERE id_sala = ?
AND fecha_funcion = ?
AND estado <> 'Cancelada'
AND hora_inicio < ?
AND hora_fin > ?;
Explicación de la condición

Para detectar cruce de horario se compara así:

hora_inicio_existente < nueva_hora_fin
AND hora_fin_existente > nueva_hora_inicio

Esto permite detectar funciones que se solapan total o parcialmente.

Resultado esperado

Si la consulta devuelve un valor mayor que cero, significa que ya existe una función en esa sala y horario. El sistema debe impedir el registro o modificación.

Método sugerido en Java
public boolean existeChoqueHorario(int idSala, LocalDate fechaFuncion, LocalTime horaInicio, LocalTime horaFin) {
    // Retorna true si ya existe una función en la misma sala,
    // en la misma fecha y con horario cruzado.
}

## 4. Validación del cálculo de venta
Descripción

La tabla venta contiene los campos:

subtotal
descuento_total
total_pagado

El modelo físico tiene una restricción que valida:

total_pagado = subtotal - descuento_total

Sin embargo, la base de datos no calcula automáticamente el subtotal sumando todas las entradas de la venta.

Por eso, Java debe calcular correctamente los montos antes de insertar la venta.

Regla

El subtotal de una venta debe ser igual a la suma de los precios finales de todas las entradas asociadas a esa venta.

Cálculo esperado
subtotal = suma de precio_final de las entradas
total_pagado = subtotal - descuento_total
Consulta de referencia para verificar una venta
SELECT SUM(precio_final) AS subtotal_calculado
FROM entrada
WHERE id_venta = ?
AND estado = 'Activa';
Método sugerido en Java
public BigDecimal calcularSubtotalVenta(List<Entrada> entradas) {
    // Suma el precio final de todas las entradas
    // antes de guardar la venta.
}

## 5. Validación del precio final de la entrada
Descripción

La tabla entrada contiene los campos:

precio_base
cargo_butaca
descuento
precio_final

El modelo físico valida que:

precio_final = precio_base + cargo_butaca - descuento

Aunque esta restricción existe en la base de datos, Java también debe realizar el cálculo antes del INSERT, para evitar errores al guardar.

Regla

El precio final de una entrada debe calcularse correctamente antes de registrarse.

Cálculo esperado
precio_final = precio_base + cargo_butaca - descuento
Método sugerido en Java
public BigDecimal calcularPrecioFinal(BigDecimal precioBase, BigDecimal cargoButaca, BigDecimal descuento) {
    // Calcula el precio final de la entrada.
}

## 6. Validación de entrada gratis por puntos
Descripción

La tabla historial_puntos guarda los movimientos de puntos del cliente, pero la base de datos no calcula automáticamente cuántos puntos tiene acumulados cada cliente.

Por eso, Java debe calcular los puntos disponibles antes de permitir una entrada gratis.

Regla

Un cliente podrá recibir una entrada gratis cuando tenga suficientes puntos acumulados según la regla de negocio definida por el sistema.

En este proyecto, la regla acordada es:

9 puntos disponibles = 1 entrada gratis
Cálculo esperado
puntos_disponibles = total_puntos_ganados - total_puntos_canjeados
Consulta de referencia
SELECT 
    COALESCE(SUM(puntos_ganados), 0) - COALESCE(SUM(puntos_canjeados), 0) AS puntos_disponibles
FROM historial_puntos
WHERE id_cliente = ?;
Resultado esperado

Si el cliente tiene 9 puntos o más, el sistema puede permitir una entrada gratis. Si tiene menos de 9 puntos, no debe permitir el canje.

Método sugerido en Java
public int calcularPuntosDisponibles(int idCliente) {
    // Retorna los puntos disponibles del cliente.
}
public boolean puedeCanjearEntradaGratis(int idCliente) {
    // Retorna true si el cliente tiene 9 puntos o más.
}

## 7. Registro de puntos ganados
Descripción

Cuando un cliente compra una entrada válida, el sistema debe registrar los puntos ganados en la tabla historial_puntos.

La base de datos permite guardar el historial, pero no genera automáticamente los puntos.

Regla

Después de vender una entrada activa, Java debe registrar el movimiento de puntos correspondiente, si aplica según la regla del negocio.

Ejemplo de inserción
INSERT INTO historial_puntos (
    tipo_movimiento,
    puntos_ganados,
    puntos_canjeados,
    descripcion,
    id_cliente,
    id_venta,
    id_entrada
)
VALUES (
    'GANADO',
    ?,
    0,
    'Puntos ganados por compra de entrada',
    ?,
    NULL,
    ?
);
Método sugerido en Java
public void registrarPuntosGanados(int idCliente, int idEntrada, int puntosGanados) {
    // Inserta en historial_puntos los puntos ganados
    // por la compra de una entrada.
}

## 8. Registro de puntos canjeados
Descripción

Cuando un cliente utiliza puntos para obtener una entrada gratis, el sistema debe registrar los puntos canjeados.

La tabla historial_puntos permite registrar movimientos de tipo CANJEADO.

Regla

Cuando se genera una entrada gratis por puntos, Java debe registrar el canje en historial_puntos.

Ejemplo de inserción
INSERT INTO historial_puntos (
    tipo_movimiento,
    puntos_ganados,
    puntos_canjeados,
    descripcion,
    id_cliente,
    id_venta,
    id_entrada
)
VALUES (
    'CANJEADO',
    0,
    9,
    'Puntos canjeados por entrada gratis',
    ?,
    ?,
    NULL
);
Método sugerido en Java
public void registrarPuntosCanjeados(int idCliente, int idVenta, int puntosCanjeados) {
    // Inserta en historial_puntos los puntos canjeados
    // por una entrada gratis.
}

## 9. Validación de canal de venta
Descripción

La tabla venta permite ventas por dos canales:

TAQUILLA
EN_LINEA

El modelo físico ya valida que si la venta es por taquilla, debe existir un empleado asociado.

Sin embargo, Java también debe controlar esta regla antes de guardar la venta para mostrar mensajes claros al usuario.

Regla

Si la venta es por taquilla, debe existir un empleado responsable.
Si la venta es en línea, puede no tener empleado asociado.

Método sugerido en Java
public boolean validarEmpleadoSegunCanal(String canalVenta, Integer idEmpleado) {
    // Retorna true si el canal de venta y el empleado cumplen la regla.
}

## 10. Validación de usuario según rol
Descripción

La tabla usuario puede estar asociada a un empleado o a un cliente, dependiendo del rol.

Roles de empleados:

ADMIN
CAJERO
SUPERVISOR

Rol de cliente:

CLIENTE

El modelo físico ya valida esta relación mediante un CHECK, pero Java también debe validarlo antes de guardar para evitar errores de base de datos.

Regla

Un usuario con rol ADMIN, CAJERO o SUPERVISOR debe estar asociado a un empleado y no a un cliente.

Un usuario con rol CLIENTE debe estar asociado a un cliente y no a un empleado.

Método sugerido en Java
public boolean validarUsuarioSegunRol(String rol, Integer idEmpleado, Integer idCliente) {
    // Retorna true si el rol coincide con el tipo de persona asociada.
}

## 11. Validación de consumidor final y cliente registrado
Descripción

La tabla cliente permite dos tipos de cliente:

REGISTRADO
CONSUMIDOR_FINAL

El cliente registrado debe tener documento. El consumidor final puede no tenerlo.

Regla

Si el cliente es REGISTRADO, el campo documento debe estar lleno.
Si el cliente es CONSUMIDOR_FINAL, el documento puede estar vacío.

Método sugerido en Java
public boolean validarDocumentoCliente(String tipoCliente, String documento) {
    // Retorna true si el documento cumple con el tipo de cliente.
}

## 12. Validación de estados activos
Descripción

Aunque las tablas tienen campos de estado, Java debe verificar que los registros utilizados en una operación estén activos.

Por ejemplo, no se debería vender una entrada para:

- Una película inactiva.
- Una sala inactiva o en mantenimiento.
- Una butaca inactiva o en mantenimiento.
- Una función cancelada o finalizada.
- Un método de pago inactivo.
- Un tipo de entrada inactivo.
Regla

Antes de realizar una venta, el sistema debe verificar que todos los elementos involucrados estén disponibles y activos.

Método sugerido en Java
public boolean validarElementosActivos(int idFuncion, int idButaca, int idMetodoPago, int idTipoEntrada) {
    // Verifica que la función, butaca, método de pago
    // y tipo de entrada estén en estados válidos.
}

## 13. Manejo de venta como transacción
Descripción

El proceso de venta no consiste en un solo registro. Una venta puede involucrar varias operaciones:

1. Validar cliente.
2. Validar función.
3. Validar butaca disponible.
4. Validar método de pago.
5. Calcular montos.
6. Insertar venta.
7. Insertar una o varias entradas.
8. Registrar puntos ganados o canjeados.

Si alguna parte falla, no debe quedar una venta incompleta en la base de datos.

Regla

El proceso completo de venta debe ejecutarse como una transacción en Java.

Estructura sugerida
try {
    connection.setAutoCommit(false);

    // Insertar venta
    // Insertar entradas
    // Insertar historial de puntos

    connection.commit();

} catch (Exception e) {
    connection.rollback();
}
Resultado esperado

Si todo sale bien, se guarda la venta completa.
Si algo falla, se revierte todo y no quedan datos incompletos.

## 14. Resumen de responsabilidades
Regla	Se controla en BD	Se controla en Java
Claves primarias	Sí	No
Claves foráneas	Sí	No
Campos obligatorios	Sí	También se valida en formularios
Valores únicos	Sí	También se valida antes de guardar
Estados permitidos	Sí	También se valida en formularios
Cálculo de precio final	Sí	Sí
Total de venta	Parcialmente	Sí
Disponibilidad de butaca	No	Sí
Butaca pertenece a la sala de la función	No	Sí
Choque de horarios	No	Sí
Regla de puntos	Parcialmente	Sí
Venta completa como transacción	No	Sí

## 15. Conclusión

El modelo físico define la estructura principal de la base de datos y protege la integridad básica del sistema mediante claves primarias, claves foráneas, restricciones únicas, restricciones de dominio y valores por defecto.

Las reglas más dinámicas del negocio, como la disponibilidad de butacas, la validación de horarios, el cálculo de puntos y el proceso completo de venta, serán manejadas desde Java. Esta decisión permite mantener un modelo físico más sencillo, claro y fácil de implementar, sin perder el control de las reglas importantes del sistema.


Ese archivo deja claro que el modelo no está “flojo”, sino que ustedes decidieron mantener la base más simple y pasar las reglas dinámicas al código Java.
