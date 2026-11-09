IF DB_ID(N'AutoTechDB') IS NULL CREATE DATABASE AutoTechDB;
GO
USE AutoTechDB;
GO

DROP VIEW IF EXISTS vw_OrdenesResumen, vw_StockCritico, vw_CargaMecanicos, vw_SaldosFacturas;
DROP PROCEDURE IF EXISTS sp_ConsumirRepuesto;
DROP PROCEDURE IF EXISTS sp_FinalizarOrden;
DROP PROCEDURE IF EXISTS sp_GenerarFactura;
DROP PROCEDURE IF EXISTS sp_RegistrarPago;
DROP PROCEDURE IF EXISTS sp_HistorialVehiculo;
DROP FUNCTION IF EXISTS dbo.fn_SubtotalServicios;
DROP FUNCTION IF EXISTS dbo.fn_SubtotalRepuestos;
DROP TABLE IF EXISTS Auditoria, Pagos, Facturas, ControlesCalidad, ActividadesTrabajo, CambiosMotor,
    ConsumosRepuesto, MovimientosInventario, PresupuestoLineas, Presupuestos, Diagnosticos,
    OrdenMecanico, DetalleOrden, OrdenesTrabajo, ServicioRepuesto, Motores, Repuestos,
    Proveedores, Servicios, Mecanicos, Vehiculos, Clientes, Configuracion;
GO

CREATE TABLE Configuracion(
    Clave NVARCHAR(40) NOT NULL,
    Valor NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_Configuracion PRIMARY KEY (Clave)
);

CREATE TABLE Clientes(
    IdCliente INT IDENTITY(1,1) NOT NULL,
    Nombre NVARCHAR(100) NOT NULL,
    Identidad NVARCHAR(20) NOT NULL,
    Telefono NVARCHAR(20) NOT NULL,
    Email NVARCHAR(100) NULL,
    Direccion NVARCHAR(200) NULL,
    Activo BIT NOT NULL CONSTRAINT DF_Clientes_Activo DEFAULT 1,
    FechaRegistro DATETIME2 NOT NULL CONSTRAINT DF_Clientes_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_Clientes PRIMARY KEY (IdCliente),
    CONSTRAINT UQ_Clientes_Identidad UNIQUE (Identidad),
    CONSTRAINT CK_Clientes_Nombre CHECK (LEN(LTRIM(RTRIM(Nombre))) > 0)
);

CREATE TABLE Vehiculos(
    IdVehiculo INT IDENTITY(1,1) NOT NULL,
    IdCliente INT NOT NULL,
    Placa NVARCHAR(12) NOT NULL,
    Vin NVARCHAR(17) NOT NULL,
    Marca NVARCHAR(40) NOT NULL,
    Modelo NVARCHAR(40) NOT NULL,
    Anio INT NOT NULL,
    Color NVARCHAR(30) NULL,
    Kilometraje INT NOT NULL,
    Combustible NVARCHAR(15) NOT NULL,
    Activo BIT NOT NULL CONSTRAINT DF_Vehiculos_Activo DEFAULT 1,
    CONSTRAINT PK_Vehiculos PRIMARY KEY (IdVehiculo),
    CONSTRAINT UQ_Vehiculos_Placa UNIQUE (Placa),
    CONSTRAINT UQ_Vehiculos_Vin UNIQUE (Vin),
    CONSTRAINT UQ_Vehiculos_Veh_Cli UNIQUE (IdVehiculo, IdCliente),
    CONSTRAINT FK_Vehiculos_Clientes FOREIGN KEY (IdCliente) REFERENCES Clientes(IdCliente),
    CONSTRAINT CK_Vehiculos_Anio CHECK (Anio BETWEEN 1950 AND 2100),
    CONSTRAINT CK_Vehiculos_Km CHECK (Kilometraje >= 0),
    CONSTRAINT CK_Vehiculos_Comb CHECK (Combustible IN ('Gasolina','Diesel','Hibrido','Electrico','GLP'))
);

CREATE TABLE Mecanicos(
    IdMecanico INT IDENTITY(1,1) NOT NULL,
    Nombre NVARCHAR(100) NOT NULL,
    Telefono NVARCHAR(20) NULL,
    Especialidad NVARCHAR(60) NOT NULL,
    Activo BIT NOT NULL CONSTRAINT DF_Mecanicos_Activo DEFAULT 1,
    Disponible BIT NOT NULL CONSTRAINT DF_Mecanicos_Disp DEFAULT 1,
    CONSTRAINT PK_Mecanicos PRIMARY KEY (IdMecanico)
);

CREATE TABLE Servicios(
    IdServicio INT IDENTITY(1,1) NOT NULL,
    Codigo NVARCHAR(15) NOT NULL,
    Nombre NVARCHAR(100) NOT NULL,
    Descripcion NVARCHAR(300) NULL,
    Nivel TINYINT NOT NULL,
    CostoManoObra DECIMAL(12,2) NOT NULL,
    TiempoEstimadoHoras DECIMAL(6,2) NOT NULL,
    Activo BIT NOT NULL CONSTRAINT DF_Servicios_Activo DEFAULT 1,
    CONSTRAINT PK_Servicios PRIMARY KEY (IdServicio),
    CONSTRAINT UQ_Servicios_Codigo UNIQUE (Codigo),
    CONSTRAINT CK_Servicios_Nivel CHECK (Nivel BETWEEN 1 AND 5),
    CONSTRAINT CK_Servicios_Costo CHECK (CostoManoObra >= 0),
    CONSTRAINT CK_Servicios_Tiempo CHECK (TiempoEstimadoHoras > 0)
);

CREATE TABLE Proveedores(
    IdProveedor INT IDENTITY(1,1) NOT NULL,
    Nombre NVARCHAR(100) NOT NULL,
    Telefono NVARCHAR(20) NULL,
    Email NVARCHAR(100) NULL,
    Activo BIT NOT NULL CONSTRAINT DF_Proveedores_Activo DEFAULT 1,
    CONSTRAINT PK_Proveedores PRIMARY KEY (IdProveedor),
    CONSTRAINT UQ_Proveedores_Nombre UNIQUE (Nombre)
);

CREATE TABLE Repuestos(
    IdRepuesto INT IDENTITY(1,1) NOT NULL,
    Codigo NVARCHAR(15) NOT NULL,
    Nombre NVARCHAR(100) NOT NULL,
    Categoria NVARCHAR(15) NOT NULL,
    IdProveedor INT NOT NULL,
    Stock INT NOT NULL,
    StockMinimo INT NOT NULL,
    CostoUnitario DECIMAL(12,2) NOT NULL,
    PrecioVenta DECIMAL(12,2) NOT NULL,
    Activo BIT NOT NULL CONSTRAINT DF_Repuestos_Activo DEFAULT 1,
    CONSTRAINT PK_Repuestos PRIMARY KEY (IdRepuesto),
    CONSTRAINT UQ_Repuestos_Codigo UNIQUE (Codigo),
    CONSTRAINT FK_Repuestos_Proveedores FOREIGN KEY (IdProveedor) REFERENCES Proveedores(IdProveedor),
    CONSTRAINT CK_Repuestos_Cat CHECK (Categoria IN ('Aceite','Filtro','Bujia','Bateria','Freno','Radiador','Transmision','Otro')),
    CONSTRAINT CK_Repuestos_Stock CHECK (Stock >= 0),
    CONSTRAINT CK_Repuestos_Min CHECK (StockMinimo >= 0),
    CONSTRAINT CK_Repuestos_Costo CHECK (CostoUnitario >= 0),
    CONSTRAINT CK_Repuestos_Precio CHECK (PrecioVenta >= 0)
);

CREATE TABLE ServicioRepuesto(
    IdServicio INT NOT NULL,
    IdRepuesto INT NOT NULL,
    Cantidad INT NOT NULL,
    CONSTRAINT PK_ServicioRepuesto PRIMARY KEY (IdServicio, IdRepuesto),
    CONSTRAINT FK_SerRep_Servicio FOREIGN KEY (IdServicio) REFERENCES Servicios(IdServicio),
    CONSTRAINT FK_SerRep_Repuesto FOREIGN KEY (IdRepuesto) REFERENCES Repuestos(IdRepuesto),
    CONSTRAINT CK_SerRep_Cant CHECK (Cantidad > 0)
);

CREATE TABLE Motores(
    IdMotor INT IDENTITY(1,1) NOT NULL,
    NumeroSerie NVARCHAR(40) NOT NULL,
    Descripcion NVARCHAR(120) NOT NULL,
    Cilindrada INT NOT NULL,
    MarcaCompatible NVARCHAR(40) NULL,
    ModeloCompatible NVARCHAR(40) NULL,
    AnioDesde INT NULL,
    AnioHasta INT NULL,
    Combustible NVARCHAR(15) NULL,
    Costo DECIMAL(12,2) NOT NULL,
    PrecioVenta DECIMAL(12,2) NOT NULL,
    Estado NVARCHAR(12) NOT NULL CONSTRAINT DF_Motores_Estado DEFAULT 'DISPONIBLE',
    IdProveedor INT NULL,
    CONSTRAINT PK_Motores PRIMARY KEY (IdMotor),
    CONSTRAINT UQ_Motores_Serie UNIQUE (NumeroSerie),
    CONSTRAINT FK_Motores_Proveedores FOREIGN KEY (IdProveedor) REFERENCES Proveedores(IdProveedor),
    CONSTRAINT CK_Motores_Estado CHECK (Estado IN ('DISPONIBLE','RESERVADO','INSTALADO','DESCARTADO')),
    CONSTRAINT CK_Motores_Cil CHECK (Cilindrada > 0),
    CONSTRAINT CK_Motores_Anios CHECK (AnioDesde IS NULL OR AnioHasta IS NULL OR AnioHasta >= AnioDesde),
    CONSTRAINT CK_Motores_Costo CHECK (Costo >= 0 AND PrecioVenta >= 0)
);

CREATE TABLE OrdenesTrabajo(
    IdOrden INT IDENTITY(1,1) NOT NULL,
    NumeroOrden AS ('OT-' + RIGHT('000000' + CAST(IdOrden AS VARCHAR(10)), 6)) PERSISTED,
    IdCliente INT NOT NULL,
    IdVehiculo INT NOT NULL,
    FechaIngreso DATETIME2 NOT NULL CONSTRAINT DF_Ordenes_Fecha DEFAULT SYSDATETIME(),
    Kilometraje INT NOT NULL,
    Sintomas NVARCHAR(500) NOT NULL,
    DiagnosticoPreliminar NVARCHAR(500) NULL,
    IdResponsable INT NOT NULL,
    Estado NVARCHAR(25) NOT NULL CONSTRAINT DF_Ordenes_Estado DEFAULT 'RECIBIDA',
    FechaFinalizacion DATETIME2 NULL,
    CONSTRAINT PK_OrdenesTrabajo PRIMARY KEY (IdOrden),
    CONSTRAINT FK_Ordenes_Cliente FOREIGN KEY (IdCliente) REFERENCES Clientes(IdCliente),
    CONSTRAINT FK_Ordenes_VehiculoCliente FOREIGN KEY (IdVehiculo, IdCliente) REFERENCES Vehiculos(IdVehiculo, IdCliente),
    CONSTRAINT FK_Ordenes_Responsable FOREIGN KEY (IdResponsable) REFERENCES Mecanicos(IdMecanico),
    CONSTRAINT CK_Ordenes_Km CHECK (Kilometraje >= 0),
    CONSTRAINT CK_Ordenes_Estado CHECK (Estado IN ('RECIBIDA','EN_DIAGNOSTICO','PENDIENTE_APROBACION','EN_REPARACION',
        'EN_CONTROL_CALIDAD','FINALIZADA','RECHAZADA','CANCELADA'))
);

CREATE TABLE DetalleOrden(
    IdDetalle INT IDENTITY(1,1) NOT NULL,
    IdOrden INT NOT NULL,
    IdServicio INT NOT NULL,
    PrecioManoObra DECIMAL(12,2) NOT NULL,
    Horas DECIMAL(6,2) NOT NULL,
    CONSTRAINT PK_DetalleOrden PRIMARY KEY (IdDetalle),
    CONSTRAINT UQ_DetalleOrden UNIQUE (IdOrden, IdServicio),
    CONSTRAINT FK_Detalle_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden),
    CONSTRAINT FK_Detalle_Servicio FOREIGN KEY (IdServicio) REFERENCES Servicios(IdServicio),
    CONSTRAINT CK_Detalle_Precio CHECK (PrecioManoObra >= 0 AND Horas > 0)
);

CREATE TABLE OrdenMecanico(
    IdOrden INT NOT NULL,
    IdMecanico INT NOT NULL,
    EsPrincipal BIT NOT NULL CONSTRAINT DF_OrdMec_Principal DEFAULT 0,
    CONSTRAINT PK_OrdenMecanico PRIMARY KEY (IdOrden, IdMecanico),
    CONSTRAINT FK_OrdMec_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden),
    CONSTRAINT FK_OrdMec_Mecanico FOREIGN KEY (IdMecanico) REFERENCES Mecanicos(IdMecanico)
);
CREATE UNIQUE INDEX UX_OrdenMecanico_Principal ON OrdenMecanico(IdOrden) WHERE EsPrincipal = 1;

CREATE TABLE Diagnosticos(
    IdDiagnostico INT IDENTITY(1,1) NOT NULL,
    IdOrden INT NOT NULL,
    Descripcion NVARCHAR(1000) NOT NULL,
    Evidencias NVARCHAR(1000) NULL,
    RequiereCambioMotor BIT NOT NULL CONSTRAINT DF_Diag_Motor DEFAULT 0,
    Fecha DATETIME2 NOT NULL CONSTRAINT DF_Diag_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_Diagnosticos PRIMARY KEY (IdDiagnostico),
    CONSTRAINT UQ_Diagnosticos_Orden UNIQUE (IdOrden),
    CONSTRAINT FK_Diag_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden)
);

CREATE TABLE Presupuestos(
    IdPresupuesto INT IDENTITY(1,1) NOT NULL,
    IdOrden INT NOT NULL,
    SubtotalServicios DECIMAL(12,2) NOT NULL,
    SubtotalRepuestos DECIMAL(12,2) NOT NULL,
    Descuento DECIMAL(12,2) NOT NULL CONSTRAINT DF_Pres_Desc DEFAULT 0,
    AutorizadoPor NVARCHAR(100) NULL,
    TasaImpuesto DECIMAL(5,2) NOT NULL,
    Impuesto DECIMAL(12,2) NOT NULL,
    Total DECIMAL(12,2) NOT NULL,
    Estado NVARCHAR(12) NOT NULL CONSTRAINT DF_Pres_Estado DEFAULT 'PENDIENTE',
    Fecha DATETIME2 NOT NULL CONSTRAINT DF_Pres_Fecha DEFAULT SYSDATETIME(),
    FechaRespuesta DATETIME2 NULL,
    CONSTRAINT PK_Presupuestos PRIMARY KEY (IdPresupuesto),
    CONSTRAINT FK_Pres_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden),
    CONSTRAINT CK_Pres_Estado CHECK (Estado IN ('PENDIENTE','APROBADO','RECHAZADO','REEMPLAZADO')),
    CONSTRAINT CK_Pres_Montos CHECK (SubtotalServicios >= 0 AND SubtotalRepuestos >= 0 AND Descuento >= 0
        AND Impuesto >= 0 AND Total >= 0 AND TasaImpuesto BETWEEN 0 AND 100),
    CONSTRAINT CK_Pres_Autoriza CHECK (Descuento = 0 OR AutorizadoPor IS NOT NULL)
);
CREATE UNIQUE INDEX UX_Presupuestos_Vigente ON Presupuestos(IdOrden) WHERE Estado <> 'REEMPLAZADO';

CREATE TABLE PresupuestoLineas(
    IdLinea INT IDENTITY(1,1) NOT NULL,
    IdPresupuesto INT NOT NULL,
    Tipo NVARCHAR(10) NOT NULL,
    Descripcion NVARCHAR(150) NOT NULL,
    Horas DECIMAL(6,2) NULL,
    Cantidad DECIMAL(10,2) NOT NULL,
    PrecioUnitario DECIMAL(12,2) NOT NULL,
    Subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT PK_PresupuestoLineas PRIMARY KEY (IdLinea),
    CONSTRAINT FK_PresLin_Pres FOREIGN KEY (IdPresupuesto) REFERENCES Presupuestos(IdPresupuesto),
    CONSTRAINT CK_PresLin_Tipo CHECK (Tipo IN ('SERVICIO','REPUESTO','MOTOR')),
    CONSTRAINT CK_PresLin_Monto CHECK (Cantidad > 0 AND PrecioUnitario >= 0 AND Subtotal >= 0)
);

CREATE TABLE ConsumosRepuesto(
    IdConsumo INT IDENTITY(1,1) NOT NULL,
    IdOrden INT NOT NULL,
    IdRepuesto INT NOT NULL,
    Cantidad INT NOT NULL,
    PrecioUnitario DECIMAL(12,2) NOT NULL,
    Fecha DATETIME2 NOT NULL CONSTRAINT DF_Cons_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_ConsumosRepuesto PRIMARY KEY (IdConsumo),
    CONSTRAINT FK_Cons_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden),
    CONSTRAINT FK_Cons_Repuesto FOREIGN KEY (IdRepuesto) REFERENCES Repuestos(IdRepuesto),
    CONSTRAINT CK_Cons_Cant CHECK (Cantidad > 0 AND PrecioUnitario >= 0)
);

CREATE TABLE MovimientosInventario(
    IdMovimiento INT IDENTITY(1,1) NOT NULL,
    IdRepuesto INT NOT NULL,
    Tipo NVARCHAR(7) NOT NULL,
    Cantidad INT NOT NULL,
    CostoUnitario DECIMAL(12,2) NULL,
    IdOrden INT NULL,
    Motivo NVARCHAR(150) NULL,
    Token UNIQUEIDENTIFIER NULL,
    Fecha DATETIME2 NOT NULL CONSTRAINT DF_Mov_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_MovimientosInventario PRIMARY KEY (IdMovimiento),
    CONSTRAINT FK_Mov_Repuesto FOREIGN KEY (IdRepuesto) REFERENCES Repuestos(IdRepuesto),
    CONSTRAINT FK_Mov_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden),
    CONSTRAINT CK_Mov_Tipo CHECK (Tipo IN ('ENTRADA','SALIDA')),
    CONSTRAINT CK_Mov_Cant CHECK (Cantidad > 0)
);
CREATE UNIQUE INDEX UX_Mov_Token ON MovimientosInventario(Token) WHERE Token IS NOT NULL;

CREATE TABLE CambiosMotor(
    IdCambio INT IDENTITY(1,1) NOT NULL,
    IdOrden INT NOT NULL,
    IdMotorNuevo INT NOT NULL,
    SerieMotorAnterior NVARCHAR(40) NOT NULL,
    PrecioMotor DECIMAL(12,2) NOT NULL,
    Estado NVARCHAR(12) NOT NULL CONSTRAINT DF_Cambios_Estado DEFAULT 'PLANIFICADO',
    FechaDesmontaje DATETIME2 NULL,
    ObservacionDesmontaje NVARCHAR(300) NULL,
    FechaInstalacion DATETIME2 NULL,
    SerieNuevaConfirmada NVARCHAR(40) NULL,
    FechaPruebas DATETIME2 NULL,
    ResultadoPruebas NVARCHAR(500) NULL,
    PruebasAprobadas BIT NULL,
    DestinoMotorRetirado NVARCHAR(40) NULL,
    GarantiaMeses INT NULL,
    GarantiaHasta DATE NULL,
    CONSTRAINT PK_CambiosMotor PRIMARY KEY (IdCambio),
    CONSTRAINT UQ_Cambios_Orden UNIQUE (IdOrden),
    CONSTRAINT UQ_Cambios_Motor UNIQUE (IdMotorNuevo),
    CONSTRAINT FK_Cambios_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden),
    CONSTRAINT FK_Cambios_Motor FOREIGN KEY (IdMotorNuevo) REFERENCES Motores(IdMotor),
    CONSTRAINT CK_Cambios_Estado CHECK (Estado IN ('PLANIFICADO','DESMONTADO','INSTALADO','PRUEBAS_OK','COMPLETADO')),
    CONSTRAINT CK_Cambios_Precio CHECK (PrecioMotor >= 0),
    CONSTRAINT CK_Cambios_Garantia CHECK (GarantiaMeses IS NULL OR GarantiaMeses > 0),
    CONSTRAINT CK_Cambios_Completo CHECK (Estado <> 'COMPLETADO' OR (DestinoMotorRetirado IS NOT NULL
        AND GarantiaHasta IS NOT NULL AND SerieNuevaConfirmada IS NOT NULL AND PruebasAprobadas = 1))
);

CREATE TABLE ActividadesTrabajo(
    IdActividad INT IDENTITY(1,1) NOT NULL,
    IdOrden INT NOT NULL,
    IdMecanico INT NOT NULL,
    Descripcion NVARCHAR(150) NOT NULL,
    Origen NVARCHAR(10) NOT NULL CONSTRAINT DF_Act_Origen DEFAULT 'SERVICIO',
    Horas DECIMAL(6,2) NOT NULL CONSTRAINT DF_Act_Horas DEFAULT 0,
    Estado NVARCHAR(10) NOT NULL CONSTRAINT DF_Act_Estado DEFAULT 'PENDIENTE',
    FechaCompletada DATETIME2 NULL,
    CONSTRAINT PK_ActividadesTrabajo PRIMARY KEY (IdActividad),
    CONSTRAINT FK_Act_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden),
    CONSTRAINT FK_Act_Mecanico FOREIGN KEY (IdMecanico) REFERENCES Mecanicos(IdMecanico),
    CONSTRAINT CK_Act_Origen CHECK (Origen IN ('SERVICIO','MOTOR')),
    CONSTRAINT CK_Act_Estado CHECK (Estado IN ('PENDIENTE','COMPLETADA')),
    CONSTRAINT CK_Act_Horas CHECK (Horas >= 0)
);

CREATE TABLE ControlesCalidad(
    IdControl INT IDENTITY(1,1) NOT NULL,
    IdOrden INT NOT NULL,
    Item NVARCHAR(120) NOT NULL,
    Obligatorio BIT NOT NULL CONSTRAINT DF_CC_Oblig DEFAULT 1,
    Aprobado BIT NULL,
    Inspector NVARCHAR(100) NULL,
    Fecha DATETIME2 NULL,
    CONSTRAINT PK_ControlesCalidad PRIMARY KEY (IdControl),
    CONSTRAINT UQ_ControlesCalidad UNIQUE (IdOrden, Item),
    CONSTRAINT FK_CC_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden)
);

CREATE TABLE Facturas(
    IdFactura INT IDENTITY(1,1) NOT NULL,
    NumeroFactura AS ('FAC-' + RIGHT('000000' + CAST(IdFactura AS VARCHAR(10)), 6)) PERSISTED,
    IdOrden INT NOT NULL,
    Subtotal DECIMAL(12,2) NOT NULL,
    Descuento DECIMAL(12,2) NOT NULL,
    TasaImpuesto DECIMAL(5,2) NOT NULL,
    Impuesto DECIMAL(12,2) NOT NULL,
    Total DECIMAL(12,2) NOT NULL,
    Fecha DATETIME2 NOT NULL CONSTRAINT DF_Fact_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_Facturas PRIMARY KEY (IdFactura),
    CONSTRAINT UQ_Facturas_Orden UNIQUE (IdOrden),
    CONSTRAINT FK_Fact_Orden FOREIGN KEY (IdOrden) REFERENCES OrdenesTrabajo(IdOrden),
    CONSTRAINT CK_Fact_Montos CHECK (Subtotal >= 0 AND Descuento >= 0 AND Impuesto >= 0 AND Total >= 0)
);

CREATE TABLE Pagos(
    IdPago INT IDENTITY(1,1) NOT NULL,
    IdFactura INT NOT NULL,
    Monto DECIMAL(12,2) NOT NULL,
    Metodo NVARCHAR(15) NOT NULL,
    Referencia NVARCHAR(60) NULL,
    Fecha DATETIME2 NOT NULL CONSTRAINT DF_Pagos_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_Pagos PRIMARY KEY (IdPago),
    CONSTRAINT FK_Pagos_Factura FOREIGN KEY (IdFactura) REFERENCES Facturas(IdFactura),
    CONSTRAINT CK_Pagos_Monto CHECK (Monto > 0),
    CONSTRAINT CK_Pagos_Metodo CHECK (Metodo IN ('Efectivo','Tarjeta','Transferencia'))
);

CREATE TABLE Auditoria(
    IdAuditoria INT IDENTITY(1,1) NOT NULL,
    Tabla NVARCHAR(40) NOT NULL,
    Operacion NVARCHAR(10) NOT NULL,
    IdRegistro INT NOT NULL,
    Detalle NVARCHAR(500) NULL,
    Usuario NVARCHAR(128) NOT NULL,
    Fecha DATETIME2 NOT NULL CONSTRAINT DF_Aud_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_Auditoria PRIMARY KEY (IdAuditoria)
);
GO

CREATE INDEX IX_Vehiculos_Cliente ON Vehiculos(IdCliente);
CREATE INDEX IX_Ordenes_Estado ON OrdenesTrabajo(Estado);
CREATE INDEX IX_Ordenes_Vehiculo ON OrdenesTrabajo(IdVehiculo);
CREATE INDEX IX_Consumos_Orden ON ConsumosRepuesto(IdOrden);
CREATE INDEX IX_Actividades_Orden ON ActividadesTrabajo(IdOrden);
CREATE INDEX IX_Pagos_Factura ON Pagos(IdFactura);
GO

CREATE FUNCTION dbo.fn_SubtotalServicios(@IdOrden INT)
RETURNS DECIMAL(12,2)
AS
BEGIN
    RETURN (SELECT ISNULL(SUM(PrecioManoObra), 0) FROM DetalleOrden WHERE IdOrden = @IdOrden);
END;
GO

CREATE FUNCTION dbo.fn_SubtotalRepuestos(@IdOrden INT)
RETURNS DECIMAL(12,2)
AS
BEGIN
    RETURN (SELECT ISNULL(SUM(Cantidad * PrecioUnitario), 0) FROM ConsumosRepuesto WHERE IdOrden = @IdOrden)
         + (SELECT ISNULL(SUM(PrecioMotor), 0) FROM CambiosMotor WHERE IdOrden = @IdOrden);
END;
GO

CREATE VIEW vw_OrdenesResumen
AS
SELECT o.IdOrden, o.NumeroOrden, o.FechaIngreso, o.Estado, c.Nombre AS Cliente, v.Placa,
       CONCAT(v.Marca, ' ', v.Modelo, ' ', v.Anio) AS Vehiculo, m.Nombre AS Responsable,
       dbo.fn_SubtotalServicios(o.IdOrden) AS TotalServicios,
       dbo.fn_SubtotalRepuestos(o.IdOrden) AS TotalRepuestos
FROM OrdenesTrabajo o
INNER JOIN Clientes c ON c.IdCliente = o.IdCliente
INNER JOIN Vehiculos v ON v.IdVehiculo = o.IdVehiculo
INNER JOIN Mecanicos m ON m.IdMecanico = o.IdResponsable;
GO

CREATE VIEW vw_StockCritico
AS
SELECT r.IdRepuesto, r.Codigo, r.Nombre, r.Categoria, r.Stock, r.StockMinimo,
       r.StockMinimo - r.Stock AS Faltante, ISNULL(p.Nombre, 'Sin proveedor') AS Proveedor
FROM Repuestos r
LEFT JOIN Proveedores p ON p.IdProveedor = r.IdProveedor
WHERE r.Activo = 1 AND r.Stock <= r.StockMinimo;
GO

CREATE VIEW vw_CargaMecanicos
AS
SELECT m.IdMecanico, m.Nombre, m.Especialidad, m.Activo, m.Disponible,
       COUNT(o.IdOrden) AS OrdenesActivas
FROM Mecanicos m
LEFT JOIN OrdenMecanico om ON om.IdMecanico = m.IdMecanico
LEFT JOIN OrdenesTrabajo o ON o.IdOrden = om.IdOrden
     AND o.Estado IN ('EN_DIAGNOSTICO','EN_REPARACION','EN_CONTROL_CALIDAD')
GROUP BY m.IdMecanico, m.Nombre, m.Especialidad, m.Activo, m.Disponible;
GO

CREATE VIEW vw_SaldosFacturas
AS
SELECT f.IdFactura, f.NumeroFactura, o.NumeroOrden, c.Nombre AS Cliente, f.Fecha, f.Total,
       ISNULL(SUM(p.Monto), 0) AS Pagado, f.Total - ISNULL(SUM(p.Monto), 0) AS Saldo
FROM Facturas f
INNER JOIN OrdenesTrabajo o ON o.IdOrden = f.IdOrden
INNER JOIN Clientes c ON c.IdCliente = o.IdCliente
LEFT JOIN Pagos p ON p.IdFactura = f.IdFactura
GROUP BY f.IdFactura, f.NumeroFactura, o.NumeroOrden, c.Nombre, f.Fecha, f.Total;
GO

CREATE TRIGGER trg_Ordenes_Auditoria ON OrdenesTrabajo
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Auditoria(Tabla, Operacion, IdRegistro, Detalle, Usuario)
    SELECT 'OrdenesTrabajo', 'INSERT', i.IdOrden, CONCAT('Orden creada en estado ', i.Estado), SUSER_SNAME()
    FROM inserted i
    WHERE NOT EXISTS (SELECT 1 FROM deleted);

    INSERT INTO Auditoria(Tabla, Operacion, IdRegistro, Detalle, Usuario)
    SELECT 'OrdenesTrabajo', 'UPDATE', i.IdOrden, CONCAT('Estado: ', d.Estado, ' -> ', i.Estado), SUSER_SNAME()
    FROM inserted i
    INNER JOIN deleted d ON d.IdOrden = i.IdOrden
    WHERE i.Estado <> d.Estado;
END;
GO

CREATE TRIGGER trg_Repuestos_Auditoria ON Repuestos
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Auditoria(Tabla, Operacion, IdRegistro, Detalle, Usuario)
    SELECT 'Repuestos', 'UPDATE', i.IdRepuesto,
           CONCAT('Precio venta: ', d.PrecioVenta, ' -> ', i.PrecioVenta, ' | Costo: ', d.CostoUnitario, ' -> ', i.CostoUnitario),
           SUSER_SNAME()
    FROM inserted i
    INNER JOIN deleted d ON d.IdRepuesto = i.IdRepuesto
    WHERE i.PrecioVenta <> d.PrecioVenta OR i.CostoUnitario <> d.CostoUnitario;
END;
GO

CREATE TRIGGER trg_Servicios_Auditoria ON Servicios
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Auditoria(Tabla, Operacion, IdRegistro, Detalle, Usuario)
    SELECT 'Servicios', 'UPDATE', i.IdServicio,
           CONCAT('Costo mano de obra: ', d.CostoManoObra, ' -> ', i.CostoManoObra),
           SUSER_SNAME()
    FROM inserted i
    INNER JOIN deleted d ON d.IdServicio = i.IdServicio
    WHERE i.CostoManoObra <> d.CostoManoObra;
END;
GO

CREATE PROCEDURE sp_ConsumirRepuesto
    @IdOrden INT,
    @IdRepuesto INT,
    @Cantidad INT,
    @Token UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @propia BIT = CASE WHEN @@TRANCOUNT = 0 THEN 1 ELSE 0 END;
    BEGIN TRY
        IF @propia = 1 BEGIN TRAN;
        IF @Cantidad IS NULL OR @Cantidad <= 0 THROW 50001, 'La cantidad debe ser mayor que cero.', 1;
        IF EXISTS (SELECT 1 FROM MovimientosInventario WHERE Token = @Token)
            THROW 50002, 'Operacion duplicada: este consumo ya fue registrado.', 1;

        DECLARE @estado NVARCHAR(25);
        SELECT @estado = Estado FROM OrdenesTrabajo WITH (UPDLOCK, ROWLOCK) WHERE IdOrden = @IdOrden;
        IF @estado IS NULL THROW 50003, 'La orden no existe.', 1;
        IF @estado <> 'EN_REPARACION'
            THROW 50004, 'Solo se pueden consumir repuestos en ordenes EN_REPARACION.', 1;

        DECLARE @stock INT, @precio DECIMAL(12,2), @activo BIT, @nombre NVARCHAR(100);
        SELECT @stock = Stock, @precio = PrecioVenta, @activo = Activo, @nombre = Nombre
        FROM Repuestos WITH (UPDLOCK, HOLDLOCK) WHERE IdRepuesto = @IdRepuesto;
        IF @stock IS NULL THROW 50005, 'El repuesto no existe.', 1;
        IF @activo = 0 THROW 50006, 'El repuesto esta inactivo.', 1;
        IF @stock < @Cantidad
        BEGIN
            DECLARE @msg NVARCHAR(200) = CONCAT('Stock insuficiente de ', @nombre, ': disponible ', @stock, ', solicitado ', @Cantidad, '.');
            THROW 50007, @msg, 1;
        END

        UPDATE Repuestos SET Stock = Stock - @Cantidad WHERE IdRepuesto = @IdRepuesto;
        INSERT INTO ConsumosRepuesto(IdOrden, IdRepuesto, Cantidad, PrecioUnitario)
        VALUES (@IdOrden, @IdRepuesto, @Cantidad, @precio);
        INSERT INTO MovimientosInventario(IdRepuesto, Tipo, Cantidad, IdOrden, Motivo, Token)
        VALUES (@IdRepuesto, 'SALIDA', @Cantidad, @IdOrden, 'Consumo en orden de trabajo', @Token);

        IF @propia = 1 COMMIT;
    END TRY
    BEGIN CATCH
        IF @propia = 1 AND @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE sp_FinalizarOrden
    @IdOrden INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @propia BIT = CASE WHEN @@TRANCOUNT = 0 THEN 1 ELSE 0 END;
    BEGIN TRY
        IF @propia = 1 BEGIN TRAN;
        DECLARE @estado NVARCHAR(25);
        SELECT @estado = Estado FROM OrdenesTrabajo WITH (UPDLOCK, ROWLOCK) WHERE IdOrden = @IdOrden;
        IF @estado IS NULL THROW 50010, 'La orden no existe.', 1;
        IF @estado <> 'EN_CONTROL_CALIDAD'
            THROW 50011, 'La orden debe estar EN_CONTROL_CALIDAD para finalizar.', 1;

        IF NOT EXISTS (SELECT 1 FROM ActividadesTrabajo WHERE IdOrden = @IdOrden)
            THROW 50012, 'La orden no tiene actividades registradas.', 1;
        IF EXISTS (SELECT 1 FROM ActividadesTrabajo WHERE IdOrden = @IdOrden AND Estado = 'PENDIENTE')
            THROW 50013, 'No se puede finalizar: hay actividades pendientes.', 1;

        IF EXISTS (
            SELECT 1
            FROM (SELECT sr.IdRepuesto, SUM(sr.Cantidad) AS Requerido
                  FROM DetalleOrden d
                  INNER JOIN ServicioRepuesto sr ON sr.IdServicio = d.IdServicio
                  WHERE d.IdOrden = @IdOrden
                  GROUP BY sr.IdRepuesto) r
            WHERE r.Requerido > ISNULL((SELECT SUM(c.Cantidad) FROM ConsumosRepuesto c
                                        WHERE c.IdOrden = @IdOrden AND c.IdRepuesto = r.IdRepuesto), 0))
            THROW 50014, 'No se puede finalizar: hay repuestos requeridos sin consumo registrado.', 1;

        IF NOT EXISTS (SELECT 1 FROM ControlesCalidad WHERE IdOrden = @IdOrden)
            THROW 50015, 'No se puede finalizar: no hay controles de calidad.', 1;
        IF EXISTS (SELECT 1 FROM ControlesCalidad
                   WHERE IdOrden = @IdOrden AND Obligatorio = 1 AND (Aprobado IS NULL OR Aprobado = 0))
            THROW 50016, 'No se puede finalizar: hay controles de calidad obligatorios sin aprobar.', 1;

        IF EXISTS (SELECT 1 FROM CambiosMotor WHERE IdOrden = @IdOrden AND Estado <> 'COMPLETADO')
            THROW 50017, 'No se puede finalizar: el cambio de motor no esta completado.', 1;

        UPDATE OrdenesTrabajo SET Estado = 'FINALIZADA', FechaFinalizacion = SYSDATETIME() WHERE IdOrden = @IdOrden;
        IF @propia = 1 COMMIT;
    END TRY
    BEGIN CATCH
        IF @propia = 1 AND @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE sp_GenerarFactura
    @IdOrden INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @propia BIT = CASE WHEN @@TRANCOUNT = 0 THEN 1 ELSE 0 END;
    BEGIN TRY
        IF @propia = 1 BEGIN TRAN;
        DECLARE @estado NVARCHAR(25);
        SELECT @estado = Estado FROM OrdenesTrabajo WITH (UPDLOCK, ROWLOCK) WHERE IdOrden = @IdOrden;
        IF @estado IS NULL THROW 50020, 'La orden no existe.', 1;
        IF @estado <> 'FINALIZADA' THROW 50021, 'Solo se factura una orden FINALIZADA.', 1;
        IF EXISTS (SELECT 1 FROM Facturas WHERE IdOrden = @IdOrden) THROW 50022, 'La orden ya fue facturada.', 1;

        DECLARE @sub DECIMAL(12,2) = dbo.fn_SubtotalServicios(@IdOrden) + dbo.fn_SubtotalRepuestos(@IdOrden);
        DECLARE @desc DECIMAL(12,2) = 0;
        DECLARE @tasa DECIMAL(5,2) = CAST((SELECT Valor FROM Configuracion WHERE Clave = 'TASA_ISV') AS DECIMAL(5,2));
        SELECT TOP 1 @desc = Descuento, @tasa = TasaImpuesto FROM Presupuestos WHERE IdOrden = @IdOrden AND Estado = 'APROBADO';
        IF @desc > @sub SET @desc = @sub;
        DECLARE @imp DECIMAL(12,2) = ROUND((@sub - @desc) * @tasa / 100, 2);

        INSERT INTO Facturas(IdOrden, Subtotal, Descuento, TasaImpuesto, Impuesto, Total)
        VALUES (@IdOrden, @sub, @desc, @tasa, @imp, @sub - @desc + @imp);
        DECLARE @id INT = SCOPE_IDENTITY();
        IF @propia = 1 COMMIT;
        SELECT @id AS IdFactura;
    END TRY
    BEGIN CATCH
        IF @propia = 1 AND @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE sp_RegistrarPago
    @IdFactura INT,
    @Monto DECIMAL(12,2),
    @Metodo NVARCHAR(15),
    @Referencia NVARCHAR(60) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @propia BIT = CASE WHEN @@TRANCOUNT = 0 THEN 1 ELSE 0 END;
    BEGIN TRY
        IF @propia = 1 BEGIN TRAN;
        IF @Monto IS NULL OR @Monto <= 0 THROW 50030, 'El monto debe ser mayor que cero.', 1;
        DECLARE @total DECIMAL(12,2);
        SELECT @total = Total FROM Facturas WITH (UPDLOCK, HOLDLOCK) WHERE IdFactura = @IdFactura;
        IF @total IS NULL THROW 50031, 'La factura no existe.', 1;
        DECLARE @saldo DECIMAL(12,2) = @total - (SELECT ISNULL(SUM(Monto), 0) FROM Pagos WHERE IdFactura = @IdFactura);
        IF @Monto > @saldo
        BEGIN
            DECLARE @msg NVARCHAR(200) = CONCAT('Sobrepago no permitido: el saldo pendiente es ', @saldo, '.');
            THROW 50032, @msg, 1;
        END
        INSERT INTO Pagos(IdFactura, Monto, Metodo, Referencia) VALUES (@IdFactura, @Monto, @Metodo, @Referencia);
        IF @propia = 1 COMMIT;
    END TRY
    BEGIN CATCH
        IF @propia = 1 AND @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE sp_HistorialVehiculo
    @IdVehiculo INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT o.NumeroOrden AS Orden, o.FechaIngreso AS Fecha, o.Kilometraje AS Km, o.Estado,
           (SELECT STRING_AGG(s.Nombre, ', ') FROM DetalleOrden d
            INNER JOIN Servicios s ON s.IdServicio = d.IdServicio WHERE d.IdOrden = o.IdOrden) AS Servicios,
           (SELECT STRING_AGG(m.Nombre, ', ') FROM OrdenMecanico om
            INNER JOIN Mecanicos m ON m.IdMecanico = om.IdMecanico WHERE om.IdOrden = o.IdOrden) AS Mecanicos,
           (SELECT STRING_AGG(CONCAT(r.Nombre, ' x', c.Cantidad), ', ') FROM ConsumosRepuesto c
            INNER JOIN Repuestos r ON r.IdRepuesto = c.IdRepuesto WHERE c.IdOrden = o.IdOrden) AS Repuestos,
           ISNULL(f.Total, dbo.fn_SubtotalServicios(o.IdOrden) + dbo.fn_SubtotalRepuestos(o.IdOrden)) AS Costo
    FROM OrdenesTrabajo o
    LEFT JOIN Facturas f ON f.IdOrden = o.IdOrden
    WHERE o.IdVehiculo = @IdVehiculo
    ORDER BY o.FechaIngreso DESC;
END;
GO

INSERT INTO Configuracion(Clave, Valor) VALUES
 ('TASA_ISV', '15'), ('GARANTIA_MOTOR_MESES', '12'), ('CARGA_MAX_MECANICO', '3');

INSERT INTO Proveedores(Nombre, Telefono, Email) VALUES
 ('Repuestos Centroamericana', '2550-1100', 'ventas@repcentro.hn'),
 ('AutoParts San Pedro', '2557-4400', 'pedidos@autopartssps.hn'),
 ('Motores y Transmisiones del Norte', '2234-9900', 'contacto@motoresnorte.hn');

INSERT INTO Mecanicos(Nombre, Telefono, Especialidad, Activo, Disponible) VALUES
 ('Luis Ortega', '9988-1001', 'Motores y transmision', 1, 1),
 ('Marvin Calix', '9988-1002', 'Mecanica general', 1, 1),
 ('Daniel Rivas', '9988-1003', 'Sistema electrico', 1, 1),
 ('Jorge Funes', '9988-1004', 'Frenos y suspension', 0, 0);

INSERT INTO Servicios(Codigo, Nombre, Descripcion, Nivel, CostoManoObra, TiempoEstimadoHoras) VALUES
 ('SRV-101', 'Cambio de aceite y filtro', 'Cambio de aceite de motor y filtro de aceite', 1, 350, 0.5),
 ('SRV-102', 'Cambio de filtro de aire', 'Sustitucion del filtro de aire del motor', 1, 150, 0.3),
 ('SRV-103', 'Cambio de bujias', 'Sustitucion del juego de bujias', 1, 450, 0.8),
 ('SRV-104', 'Revision de luces', 'Revision y reemplazo de luces', 1, 100, 0.3),
 ('SRV-201', 'Afinamiento mayor', 'Afinamiento completo del motor', 2, 1200, 2.5),
 ('SRV-202', 'Limpieza de inyectores', 'Limpieza y calibracion de inyectores', 2, 1500, 2),
 ('SRV-203', 'Cambio de pastillas de freno', 'Sustitucion de pastillas delanteras', 2, 900, 1.5),
 ('SRV-301', 'Cambio de radiador', 'Sustitucion del radiador y llenado de refrigerante', 3, 2500, 4),
 ('SRV-302', 'Cambio de alternador', 'Sustitucion del alternador', 3, 2200, 3),
 ('SRV-303', 'Cambio de embrague', 'Sustitucion del kit de embrague', 3, 4500, 6),
 ('SRV-401', 'Reparacion de transmision', 'Desarmado y reparacion de transmision', 4, 8000, 12),
 ('SRV-402', 'Reparacion de culata', 'Rectificado y reparacion de culata', 4, 9500, 16),
 ('SRV-403', 'Diagnostico de sistema electrico', 'Diagnostico con scanner del sistema electrico', 4, 2800, 5),
 ('SRV-501', 'Sustitucion de motor', 'Desmontaje e instalacion de motor completo', 5, 18000, 32),
 ('SRV-502', 'Reconstruccion de motor', 'Reconstruccion completa del motor', 5, 25000, 48);

INSERT INTO Repuestos(Codigo, Nombre, Categoria, IdProveedor, Stock, StockMinimo, CostoUnitario, PrecioVenta) VALUES
 ('REP-001', 'Aceite sintetico 5W-30 (litro)', 'Aceite', 1, 80, 20, 95, 140),
 ('REP-002', 'Aceite diesel 15W-40 (litro)', 'Aceite', 1, 60, 20, 85, 125),
 ('REP-003', 'Filtro de aceite universal', 'Filtro', 2, 40, 10, 90, 150),
 ('REP-004', 'Filtro de aire Hilux', 'Filtro', 2, 15, 5, 180, 290),
 ('REP-005', 'Bujia de iridio', 'Bujia', 2, 48, 12, 140, 220),
 ('REP-006', 'Bateria 12V 650CCA', 'Bateria', 3, 8, 3, 2300, 3200),
 ('REP-007', 'Pastillas de freno delanteras', 'Freno', 3, 12, 4, 650, 980),
 ('REP-008', 'Radiador Hilux 2005-2015', 'Radiador', 3, 3, 2, 3100, 4200),
 ('REP-009', 'Kit de embrague', 'Transmision', 3, 2, 2, 3600, 4900),
 ('REP-010', 'Refrigerante (galon)', 'Otro', 1, 25, 8, 160, 250),
 ('REP-011', 'Aceite de transmision ATF (litro)', 'Aceite', 1, 30, 10, 130, 190),
 ('REP-012', 'Empaque de culata', 'Otro', 2, 6, 3, 520, 780);

INSERT INTO ServicioRepuesto(IdServicio, IdRepuesto, Cantidad) VALUES
 (1, 1, 4), (1, 3, 1), (2, 4, 1), (3, 5, 4), (7, 7, 1), (8, 8, 1), (8, 10, 2),
 (10, 9, 1), (12, 12, 1), (14, 2, 8), (14, 10, 4);

INSERT INTO Motores(NumeroSerie, Descripcion, Cilindrada, MarcaCompatible, ModeloCompatible, AnioDesde, AnioHasta, Combustible, Costo, PrecioVenta, Estado, IdProveedor) VALUES
 ('2KD-FTV-A100234', 'Motor 2.5L Diesel 2KD-FTV remanufacturado', 2500, 'Toyota', 'Hilux', 2012, 2015, 'Diesel', 38000, 52000, 'DISPONIBLE', 3),
 ('2KD-FTV-A100999', 'Motor 2.5L Diesel 2KD-FTV usado', 2500, 'Toyota', 'Hilux', 2012, 2015, 'Diesel', 30000, 44000, 'INSTALADO', 3),
 ('YD25-B200117', 'Motor 2.5L Diesel YD25 Nissan Frontier', 2500, 'Nissan', 'Frontier', 2010, 2014, 'Diesel', 34000, 47000, 'DISPONIBLE', 3),
 ('1KD-FTV-C300456', 'Motor 3.0L Diesel 1KD-FTV (ficha incompleta)', 3000, NULL, NULL, NULL, NULL, NULL, 42000, 58000, 'DISPONIBLE', 3),
 ('1KD-FTV-C300777', 'Motor 3.0L Diesel 1KD-FTV Hilux 2016-2020', 3000, 'Toyota', 'Hilux', 2016, 2020, 'Diesel', 45000, 61000, 'DISPONIBLE', 3);

INSERT INTO Clientes(Nombre, Identidad, Telefono, Email, Direccion) VALUES
 ('Roberto Mejia', '0501-1985-01234', '9999-1234', 'rmejia@correo.hn', 'Col. Los Alamos, Puerto Cortes');

INSERT INTO Vehiculos(IdCliente, Placa, Vin, Marca, Modelo, Anio, Color, Kilometraje, Combustible) VALUES
 (1, 'HAA1234', 'MR0FZ29G6F1234567', 'Toyota', 'Hilux', 2015, 'Blanco', 168500, 'Diesel'),
 (1, 'HBC5678', 'JTDKB20U183456789', 'Toyota', 'Corolla', 2018, 'Gris', 72300, 'Gasolina');
GO
