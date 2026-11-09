import subprocess, html

T = {
 "Clientes": [("IdCliente","PK"),("Nombre",""),("Identidad","UQ"),("Telefono",""),("Email",""),("Activo","")],
 "Vehiculos": [("IdVehiculo","PK"),("IdCliente","FK"),("Placa","UQ"),("Vin","UQ"),("Marca",""),("Modelo",""),("Anio",""),("Kilometraje",""),("Combustible","")],
 "Mecanicos": [("IdMecanico","PK"),("Nombre",""),("Especialidad",""),("Activo",""),("Disponible","")],
 "Servicios": [("IdServicio","PK"),("Codigo","UQ"),("Nombre",""),("Nivel","1-5"),("CostoManoObra",""),("TiempoEstimadoHoras","")],
 "Proveedores": [("IdProveedor","PK"),("Nombre","UQ"),("Telefono",""),("Email","")],
 "Repuestos": [("IdRepuesto","PK"),("Codigo","UQ"),("Nombre",""),("Categoria",""),("IdProveedor","FK"),("Stock",""),("StockMinimo",""),("CostoUnitario",""),("PrecioVenta","")],
 "ServicioRepuesto": [("IdServicio","PK,FK"),("IdRepuesto","PK,FK"),("Cantidad","")],
 "Motores": [("IdMotor","PK"),("NumeroSerie","UQ"),("Descripcion",""),("MarcaCompatible",""),("ModeloCompatible",""),("AnioDesde",""),("AnioHasta",""),("Combustible",""),("PrecioVenta",""),("Estado",""),("IdProveedor","FK")],
 "OrdenesTrabajo": [("IdOrden","PK"),("NumeroOrden","calc"),("IdCliente","FK"),("IdVehiculo","FK"),("IdResponsable","FK"),("FechaIngreso",""),("Kilometraje",""),("Sintomas",""),("Estado","")],
 "DetalleOrden": [("IdDetalle","PK"),("IdOrden","FK"),("IdServicio","FK"),("PrecioManoObra",""),("Horas","")],
 "OrdenMecanico": [("IdOrden","PK,FK"),("IdMecanico","PK,FK"),("EsPrincipal","")],
 "Diagnosticos": [("IdDiagnostico","PK"),("IdOrden","FK,UQ"),("Descripcion",""),("Evidencias",""),("RequiereCambioMotor","")],
 "Presupuestos": [("IdPresupuesto","PK"),("IdOrden","FK"),("SubtotalServicios",""),("SubtotalRepuestos",""),("Descuento",""),("TasaImpuesto",""),("Impuesto",""),("Total",""),("Estado","")],
 "PresupuestoLineas": [("IdLinea","PK"),("IdPresupuesto","FK"),("Tipo",""),("Descripcion",""),("Horas",""),("Cantidad",""),("PrecioUnitario",""),("Subtotal","")],
 "ConsumosRepuesto": [("IdConsumo","PK"),("IdOrden","FK"),("IdRepuesto","FK"),("Cantidad",""),("PrecioUnitario","")],
 "MovimientosInventario": [("IdMovimiento","PK"),("IdRepuesto","FK"),("IdOrden","FK"),("Tipo",""),("Cantidad",""),("Token","UQ")],
 "CambiosMotor": [("IdCambio","PK"),("IdOrden","FK,UQ"),("IdMotorNuevo","FK,UQ"),("SerieMotorAnterior",""),("Estado",""),("SerieNuevaConfirmada",""),("DestinoMotorRetirado",""),("GarantiaHasta","")],
 "ActividadesTrabajo": [("IdActividad","PK"),("IdOrden","FK"),("IdMecanico","FK"),("Descripcion",""),("Origen",""),("Horas",""),("Estado","")],
 "ControlesCalidad": [("IdControl","PK"),("IdOrden","FK"),("Item",""),("Obligatorio",""),("Aprobado",""),("Inspector","")],
 "Facturas": [("IdFactura","PK"),("NumeroFactura","calc"),("IdOrden","FK,UQ"),("Subtotal",""),("Descuento",""),("Impuesto",""),("Total","")],
 "Pagos": [("IdPago","PK"),("IdFactura","FK"),("Monto",""),("Metodo",""),("Referencia","")],
 "Auditoria": [("IdAuditoria","PK"),("Tabla",""),("Operacion",""),("IdRegistro",""),("Detalle",""),("Usuario","")],
 "Configuracion": [("Clave","PK"),("Valor","")],
}
E = [("Clientes","Vehiculos"),("Clientes","OrdenesTrabajo"),("Vehiculos","OrdenesTrabajo"),("Mecanicos","OrdenesTrabajo"),
 ("Proveedores","Repuestos"),("Proveedores","Motores"),("Servicios","ServicioRepuesto"),("Repuestos","ServicioRepuesto"),
 ("OrdenesTrabajo","DetalleOrden"),("Servicios","DetalleOrden"),("OrdenesTrabajo","OrdenMecanico"),("Mecanicos","OrdenMecanico"),
 ("OrdenesTrabajo","Diagnosticos"),("OrdenesTrabajo","Presupuestos"),("Presupuestos","PresupuestoLineas"),
 ("OrdenesTrabajo","ConsumosRepuesto"),("Repuestos","ConsumosRepuesto"),("Repuestos","MovimientosInventario"),
 ("OrdenesTrabajo","MovimientosInventario"),("OrdenesTrabajo","CambiosMotor"),("Motores","CambiosMotor"),
 ("OrdenesTrabajo","ActividadesTrabajo"),("Mecanicos","ActividadesTrabajo"),("OrdenesTrabajo","ControlesCalidad"),
 ("OrdenesTrabajo","Facturas"),("Facturas","Pagos")]
COL = {"Clientes":"#dbeafe","Vehiculos":"#dbeafe","Mecanicos":"#dcfce7","Servicios":"#fef9c3","ServicioRepuesto":"#fef9c3",
 "Proveedores":"#fde68a","Repuestos":"#fde68a","MovimientosInventario":"#fde68a","ConsumosRepuesto":"#fde68a","Motores":"#fed7aa","CambiosMotor":"#fed7aa",
 "OrdenesTrabajo":"#fecaca","DetalleOrden":"#fecaca","OrdenMecanico":"#fecaca","Diagnosticos":"#fecaca","Presupuestos":"#fecaca","PresupuestoLineas":"#fecaca",
 "ActividadesTrabajo":"#fecaca","ControlesCalidad":"#fecaca","Facturas":"#e9d5ff","Pagos":"#e9d5ff","Auditoria":"#e5e7eb","Configuracion":"#e5e7eb"}
g = ['digraph ER {','graph [rankdir=TB, splines=true, nodesep=0.25, ranksep=0.7, fontname="DejaVu Sans", bgcolor="white", pad=0.3];',
     'node [shape=plain, fontname="DejaVu Sans", fontsize=10]; edge [color="#475569", arrowtail=tee, arrowhead=crow, dir=both, arrowsize=0.8];']
for n, cols in T.items():
    rows = "".join('<TR><TD ALIGN="LEFT">%s</TD><TD ALIGN="LEFT">%s</TD></TR>' % (html.escape(c), ('<FONT COLOR="#b91c1c" POINT-SIZE="8">%s</FONT>' % html.escape(f)) if f else ' ') for c, f in cols)
    g.append('"%s" [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="white"><TR><TD COLSPAN="2" BGCOLOR="%s"><B>%s</B></TD></TR>%s</TABLE>>];' % (n, COL[n], COL[n].replace('#','#'), rows) if False else
             '"%s" [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="white"><TR><TD COLSPAN="2" BGCOLOR="%s"><B>%s</B></TD></TR>%s</TABLE>>];' % (n, COL[n], n, rows))
for p, c in E:
    g.append('"%s" -> "%s";' % (p, c))
g.append('}')
open('docs/ER_AutoTechDB.dot','w').write("\n".join(g))
subprocess.run(['dot','-Tpng','-Gdpi=90','docs/ER_AutoTechDB.dot','-o','docs/ER_AutoTechDB.png'],check=True)
subprocess.run(['dot','-Tsvg','docs/ER_AutoTechDB.dot','-o','docs/ER_AutoTechDB.svg'],check=True)

def cls(name, attrs=(), methods=(), color="#f1f5f9", stereo=None):
    head = ('<I>&lt;&lt;%s&gt;&gt;</I><BR/>' % stereo if stereo else '') + '<B>%s</B>' % html.escape(name)
    a = "".join('<TR><TD ALIGN="LEFT">%s</TD></TR>' % html.escape(x) for x in attrs) or '<TR><TD> </TD></TR>'
    m = "".join('<TR><TD ALIGN="LEFT">%s</TD></TR>' % html.escape(x) for x in methods) or '<TR><TD> </TD></TR>'
    return ('"%s" [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="%s"><TR><TD BGCOLOR="#cbd5e1">%s</TD></TR>'
            '<HR/>%s<HR/>%s</TABLE>>];') % (name, color, head, a, m)

u = ['digraph UML1 {','graph [rankdir=BT, nodesep=0.4, ranksep=0.7, fontname="DejaVu Sans", pad=0.3];','node [shape=plain, fontname="DejaVu Sans", fontsize=10];','edge [fontname="DejaVu Sans", fontsize=9];']
u += [cls("Persona",["- id: int","- nombre: String","- telefono: String","- activo: boolean"],["+ getRol(): String"],stereo="abstract"),
      cls("Cliente",["- identidad: String","- email: String","- direccion: String"],["+ getRol(): String"]),
      cls("Mecanico",["- especialidad: String","- disponible: boolean"],["+ getRol(): String"]),
      cls("Servicio",["- id: int","- codigo: String","- nombre: String","- costoManoObra: BigDecimal","- tiempoEstimadoHoras: BigDecimal","- activo: boolean"],["+ getNivel(): int","+ getCategoria(): String","+ controlesObligatorios(): List<String>"],stereo="abstract"),
      cls("ServicioBasico",[],["+ getNivel(): int"]), cls("ServicioPreventivo",[],["+ getNivel(): int","+ controlesObligatorios()"]),
      cls("ServicioIntermedio",[],["+ getNivel(): int","+ controlesObligatorios()"]), cls("ServicioAvanzado",[],["+ getNivel(): int","+ controlesObligatorios()"]),
      cls("ServicioMayor",[],["+ getNivel(): int","+ controlesObligatorios()"]),
      cls("ServicioFactory",[],["+ crear(nivel: int): Servicio"]),
      cls("Vehiculo",["- id: int","- idCliente: int","- placa: String","- vin: String","- marca: String","- modelo: String","- anio: int","- kilometraje: int","- combustible: String"],["+ descripcion(): String"]),
      cls("Repuesto",["- id: int","- codigo: String","- nombre: String","- categoria: String","- stock: int","- stockMinimo: int","- costoUnitario: BigDecimal","- precioVenta: BigDecimal"],["+ necesitaReposicion(): boolean"]),
      cls("Motor",["- id: int","- numeroSerie: String","- marcaCompatible: String","- modeloCompatible: String","- anioDesde: Integer","- anioHasta: Integer","- combustible: String","- estado: String"],["+ validarCompatibilidad(v: Vehiculo): List<String>"]),
      cls("CambioMotor",["- idOrden: int","- idMotorNuevo: int","- serieAnterior: String","- estado: String","- pruebasAprobadas: Boolean"]),
      cls("OrdenTrabajo",["- id: int","- numero: String","- idCliente: int","- idVehiculo: int","- kilometraje: int","- sintomas: String","- idResponsable: int","- estado: EstadoOrden"]),
      cls("EstadoOrden",["RECIBIDA","EN_DIAGNOSTICO","PENDIENTE_APROBACION","EN_REPARACION","EN_CONTROL_CALIDAD","FINALIZADA","RECHAZADA","CANCELADA"],["+ siguientes(): Set<EstadoOrden>","+ puedeCambiarA(e): boolean"],stereo="enum"),
      cls("Presupuesto",["- subtotalServicios: BigDecimal","- subtotalRepuestos: BigDecimal","- descuento: BigDecimal","- impuesto: BigDecimal","- total: BigDecimal","- estado: String"])]
for c in ["Cliente","Mecanico"]: u.append('"%s" -> "Persona" [arrowhead=empty];' % c)
for c in ["ServicioBasico","ServicioPreventivo","ServicioIntermedio","ServicioAvanzado","ServicioMayor"]: u.append('"%s" -> "Servicio" [arrowhead=empty];' % c)
u += ['"ServicioFactory" -> "Servicio" [style=dashed, arrowhead=vee, label="crea"];',
      '"Vehiculo" -> "Cliente" [arrowhead=odiamond, label="pertenece a  *..1"];',
      '"OrdenTrabajo" -> "EstadoOrden" [arrowhead=vee, label="estado"];',
      '"OrdenTrabajo" -> "Vehiculo" [arrowhead=vee, label="1"];','"OrdenTrabajo" -> "Mecanico" [arrowhead=vee, label="responsable"];',
      '"Presupuesto" -> "OrdenTrabajo" [arrowhead=vee, label="1..*"];','"CambioMotor" -> "OrdenTrabajo" [arrowhead=vee, label="0..1"];',
      '"CambioMotor" -> "Motor" [arrowhead=vee, label="motor nuevo"];','"Motor" -> "Vehiculo" [style=dashed, arrowhead=vee, label="valida contra"];','}']
open('docs/UML_Modelo.dot','w').write("\n".join(u))
subprocess.run(['dot','-Tpng','-Gdpi=90','docs/UML_Modelo.dot','-o','docs/UML_Modelo.png'],check=True)
subprocess.run(['dot','-Tsvg','docs/UML_Modelo.dot','-o','docs/UML_Modelo.svg'],check=True)

v = ['digraph UML2 {','graph [rankdir=BT, nodesep=0.35, ranksep=0.8, fontname="DejaVu Sans", pad=0.3];','node [shape=plain, fontname="DejaVu Sans", fontsize=10];','edge [fontname="DejaVu Sans", fontsize=9];']
v += [cls("CrudDAO<T>",[],["+ insertar(T): int","+ actualizar(T)","+ buscar(id): T","+ listar(): List<T>","+ cambiarActivo(id, activo)"],stereo="interface"),
      cls("BaseDAO",["- compartida: Connection"],["# consultar(sql, mapeador, params)","# ejecutar(sql, params)","# insertar(sql, params): int","# tabla(sql, params): TablaDatos"],stereo="abstract"),
      cls("ClienteDAO",[],[]),cls("VehiculoDAO",[],[]),cls("MecanicoDAO",[],[]),cls("ServicioDAO",[],[]),cls("RepuestoDAO",[],[]),cls("MotorDAO",[],[]),
      cls("OrdenDAO",[],["+ finalizar(id) [sp_FinalizarOrden]"]),cls("PresupuestoDAO",[],[]),cls("FacturacionDAO",[],["+ generarFactura(id)","+ registrarPago(...)"]),cls("ConsultaDAO",[],["+ listados y combos"]),
      cls("Conexion",[],["+ obtener(): Connection","+ transaccion(Function)","+ ejecutarEnTransaccion(Consumer)"]),
      cls("ClienteService",[],["+ registrar / modificar / cambiarActivo"],color="#dcfce7"),cls("VehiculoService",[],["+ registrar / modificar"],color="#dcfce7"),
      cls("OrdenService",[],["+ crear(OrdenTrabajo)","+ cambiarEstado(id, estado)","+ finalizar(id)"],color="#dcfce7"),
      cls("PresupuestoService",[],["+ generar(...)","+ responder(id, aprobado)"],color="#dcfce7"),
      cls("InventarioService",[],["+ registrarEntrada(...)","+ consumir(...) [transaccion]"],color="#dcfce7"),
      cls("MotorService",[],["+ planificar(...)","+ registrarDesmontaje / Instalacion / Pruebas","+ generarGarantia(...)"],color="#dcfce7"),
      cls("FacturacionService",[],["+ facturar(idOrden)","+ pagar(...)"],color="#dcfce7"),cls("ReporteService",[],["+ ejecutar(nombre)","+ exportarCsv(...)"],color="#dcfce7"),
      cls("ProcesadorOrdenHilo",["- idOrden: int","- cancelado: boolean"],["+ run()","+ cancelar()"],color="#fef9c3",stereo="Thread"),
      cls("NegocioException",[],[],color="#fecaca"),cls("ValidacionException",[],[],color="#fecaca"),cls("DatosException",[],["+ de(SQLException)"],color="#fecaca"),
      cls("VentanaPrincipal",[],[],color="#dbeafe"),cls("PnlOrdenes",[],["+ refrescar()"],color="#dbeafe"),cls("PnlMotor",[],["+ refrescar()"],color="#dbeafe"),
      cls("PnlDetalleOrden",["# idOrden: int"],["+ cargar(id)","# refrescarDetalle()"],color="#dbeafe",stereo="abstract"),
      cls("PnlProceso",[],["- procesar()"],color="#dbeafe")]
for d in ["ClienteDAO","VehiculoDAO","MecanicoDAO","ServicioDAO","RepuestoDAO","MotorDAO"]:
    v.append('"%s" -> "CrudDAO<T>" [style=dashed, arrowhead=empty];' % d)
for d in ["ClienteDAO","VehiculoDAO","MecanicoDAO","ServicioDAO","RepuestoDAO","MotorDAO","OrdenDAO","PresupuestoDAO","FacturacionDAO","ConsultaDAO"]:
    v.append('"%s" -> "BaseDAO" [arrowhead=empty];' % d)
v.append('"BaseDAO" -> "Conexion" [style=dashed, arrowhead=vee, label="usa"];')
for s,d in [("ClienteService","ClienteDAO"),("VehiculoService","VehiculoDAO"),("OrdenService","OrdenDAO"),("PresupuestoService","PresupuestoDAO"),("InventarioService","RepuestoDAO"),("MotorService","MotorDAO"),("FacturacionService","FacturacionDAO"),("ReporteService","ConsultaDAO")]:
    v.append('"%s" -> "%s" [arrowhead=vee];' % (s,d))
v += ['"ProcesadorOrdenHilo" -> "OrdenService" [arrowhead=vee];','"PnlProceso" -> "ProcesadorOrdenHilo" [arrowhead=vee, label="inicia"];',
      '"PnlOrdenes" -> "OrdenService" [arrowhead=vee];','"PnlMotor" -> "MotorService" [arrowhead=vee];',
      '"PnlProceso" -> "PnlDetalleOrden" [arrowhead=empty];','"PnlOrdenes" -> "PnlDetalleOrden" [arrowhead=odiamond, label="6 pestañas"];',
      '"VentanaPrincipal" -> "PnlOrdenes" [arrowhead=odiamond];','"VentanaPrincipal" -> "PnlMotor" [arrowhead=odiamond];',
      '"ValidacionException" -> "NegocioException" [arrowhead=empty];']
v.append('}')
open('docs/UML_Capas.dot','w').write("\n".join(v))
subprocess.run(['dot','-Tpng','-Gdpi=80','docs/UML_Capas.dot','-o','docs/UML_Capas.png'],check=True)
subprocess.run(['dot','-Tsvg','docs/UML_Capas.dot','-o','docs/UML_Capas.svg'],check=True)
print("ok")
