# AutoTech Manager

Sistema integral de gestión y control de servicios automotrices (Programación Avanzada II, Segundo Parcial).
Java 11 + Swing + JDBC + Microsoft SQL Server, con arquitectura por capas.

## Requisitos

- JDK 11 o superior
- Maven (o NetBeans, que ya lo incluye)
- SQL Server 2017 o superior (se usa `STRING_AGG`)

## Puesta en marcha

1. Abra `sql/AutoTechDB.sql` en SSMS y ejecútelo completo. Crea la base `AutoTechDB`, las tablas, restricciones, funciones, vistas, procedimientos, triggers y datos de prueba.
2. Edite `src/main/resources/config.properties` con su servidor, usuario y clave. También puede copiar ese archivo junto al `.jar` y se usará en lugar del interno.
3. Compile y ejecute:
   - NetBeans: abrir el proyecto, clic derecho, Run. Clase principal `autotech.Main`.
   - Maven: `mvn package` y luego `java -jar target/AutoTechManager-1.0.jar`.

## Estructura del código

| Paquete | Responsabilidad |
|---|---|
| `autotech.modelo` | Entidades y reglas propias del dominio (`Persona` y `Servicio` abstractas, herencia por nivel de servicio, `EstadoOrden`, `Motor.validarCompatibilidad`) |
| `autotech.dao` | Persistencia JDBC con consultas parametrizadas. Interfaz `CrudDAO<T>` y clase abstracta `BaseDAO` |
| `autotech.negocio` | Reglas de negocio, validaciones y transacciones |
| `autotech.hilos` | `ProcesadorOrdenHilo`: procesa las actividades de una orden en segundo plano |
| `autotech.excepciones` | `NegocioException`, `ValidacionException`, `DatosException` |
| `autotech.vista` | Interfaz gráfica Swing (8 pestañas) |
| `autotech.conexion` | Conexión y helper de transacciones |
| `sql/` | Script completo de base de datos |
| `docs/` | Diagrama ER, diagramas UML y su código fuente (`gen_diagramas.py`, archivos `.dot`) |

## Flujo de demostración: reparación básica (cambio de aceite)

1. **Órdenes**: elegir cliente Roberto Mejía y el vehículo, escribir síntomas, responsable Luis Ortega y crear la orden.
2. Cambiar estado a *En diagnóstico*. En la pestaña **Servicios** agregar `SRV-101`. En **Diagnóstico y presupuesto** guardar un diagnóstico.
3. Cambiar estado a *Pendiente de aprobación* (se genera el presupuesto), pulsar **Cliente aprueba** y avanzar a *En reparación*.
4. Pestaña **Repuestos**: consumir `REP-001` x4 y `REP-003` x1 (los que exige el servicio).
5. Pestaña **Actividades / Proceso**: **Procesar orden con hilo**. Al terminar la orden pasa sola a control de calidad.
6. Pestaña **Control de calidad**: escribir el inspector y aprobar cada control. Luego **Finalizar y entregar**.
7. **Facturación y pagos**: generar la factura, registrar un abono y luego el saldo. Probar un sobrepago (se bloquea).
8. **Reportes**: ejecutar los reportes y exportar uno a CSV.

## Flujo obligatorio: sustitución de motor (Toyota Hilux 2015)

1. Crear la orden para el vehículo `HAA1234` (Hilux 2015 diésel, ya registrado). Pasar a *En diagnóstico*.
2. En **Servicios** agregar `SRV-501 Sustitución de motor` (nivel 5) y asignar un mecánico adicional si se desea.
3. En **Diagnóstico y presupuesto** registrar el diagnóstico con evidencias técnicas y marcar *Requiere sustitución de motor*.
4. Pestaña **Cambio de motor**: elegir la orden, seleccionar un motor y pulsar **Validar compatibilidad**. Pruebe los motores de ejemplo:
   - `2KD-FTV-A100234`: compatible y disponible.
   - `2KD-FTV-A100999`: bloqueado, ya instalado en otro vehículo.
   - `YD25-B200117`: bloqueado, marca y modelo incompatibles.
   - `1KD-FTV-C300456`: bloqueado, ficha sin datos obligatorios.
   - `1KD-FTV-C300777`: bloqueado, año fuera del rango.
5. Escribir la serie del motor anterior y **Reservar motor para la orden**.
6. En **Órdenes**: pasar a *Pendiente de aprobación* (el presupuesto incluye mano de obra, repuestos y el motor), **Cliente aprueba** y avanzar a *En reparación*.
7. Consumir los repuestos requeridos (`REP-002` x8 y `REP-010` x4).
8. **Procesar con hilo** la orden (completa las actividades de servicio).
9. En **Cambio de motor**: registrar desmontaje, instalación (escribir la serie del motor reservado), pruebas con destino del motor retirado.
10. En **Órdenes**: pasar a *En control de calidad*, aprobar todos los controles.
11. En **Cambio de motor**: **Generar garantía**. En **Órdenes**: **Finalizar y entregar**.
12. Facturar y registrar pagos.

## Reglas de negocio principales

- Flujo de estados: Recibida, En diagnóstico, Pendiente de aprobación, En reparación, En control de calidad, Finalizada y entregada; además Rechazada y Cancelada.
- No se finaliza con actividades pendientes, repuestos requeridos sin consumo o controles de calidad obligatorios sin aprobar (`sp_FinalizarOrden`).
- Los precios de servicios y repuestos se copian a la orden al usarlos: un cambio de precio en el catálogo no altera órdenes anteriores.
- Consumo de inventario atómico (`sp_ConsumirRepuesto`) con bloqueos `UPDLOCK/HOLDLOCK`, `CHECK (Stock >= 0)` y un token único por operación para evitar descuentos duplicados.
- No se asignan mecánicos inactivos, no disponibles o con la carga máxima configurada.
- Los pagos se validan contra el saldo de la factura (`sp_RegistrarPago`): no se permiten sobrepagos.
- Un motor se bloquea si no está disponible, le faltan datos obligatorios o no es compatible, y se informa la causa.
- Los impuestos y la garantía de motor son configurables en la tabla `Configuracion`.

## Objetos de SQL Server

- Procedimientos: `sp_ConsumirRepuesto`, `sp_FinalizarOrden`, `sp_GenerarFactura`, `sp_RegistrarPago`, `sp_HistorialVehiculo`
- Vistas: `vw_OrdenesResumen`, `vw_StockCritico`, `vw_CargaMecanicos`, `vw_SaldosFacturas`
- Triggers de auditoría: `trg_Ordenes_Auditoria`, `trg_Repuestos_Auditoria`, `trg_Servicios_Auditoria`
- Funciones: `fn_SubtotalServicios`, `fn_SubtotalRepuestos`
