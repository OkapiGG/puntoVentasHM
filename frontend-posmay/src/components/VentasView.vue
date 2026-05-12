<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { apiGet, apiPost } from '../services/api'
import { carritoStore, decrementarItem, incrementarItem, limpiarCarrito } from '../store/carrito'
import { ROLES } from '../utils/permisos'

const metodo = ref('EFECTIVO')
const efectivoRecibido = ref('')
const cargando = ref(false)
const error = ref('')
const exito = ref('')
const ticketActual = ref(null)
const mostrandoTicket = ref(false)
const historialVentas = ref([])
const usuarioActual = ref(null)
const sesionCaja = ref(null)
const resumenHoy = ref(null)
const ordenesActivas = ref([])
const colaDomicilios = ref([])
const repartidores = ref([])
const cotizacion = ref(null)
const tipoOrden = ref('MOSTRADOR')
const domicilio = ref({
  nombreCliente: '',
  direccion: '',
  telefono: '',
  costoEnvio: '0'
})
const asignacionesPendientes = ref({})
const filtroActivas = ref({
  busqueda: '',
  estado: 'TODOS',
  pago: 'TODOS'
})
const filtroVentas = ref({
  busqueda: '',
  tipo: 'TODOS',
  metodo: 'TODOS',
  estado: 'TODOS'
})

const total = computed(() => carritoStore.totalImporte.value)
const costoEnvioActual = computed(() => {
  if (tipoOrden.value !== 'DOMICILIO') return 0
  const costo = Number(domicilio.value.costoEnvio || 0)
  return Number.isFinite(costo) && costo > 0 ? costo : 0
})
const totalCobro = computed(() => Number(cotizacion.value?.total ?? (total.value + costoEnvioActual.value)))
const totalDescuentoPromo = computed(() => Number(cotizacion.value?.totalDescuentoPromocional || 0))
const cambioCalculado = computed(() => {
  const efectivo = Number(efectivoRecibido.value || 0)
  return efectivo > totalCobro.value ? efectivo - totalCobro.value : 0
})
const rolActual = computed(() => usuarioActual.value?.rol || '')
const esRepartidor = computed(() => rolActual.value === ROLES.REPARTIDOR)
const puedeAsignarRepartidor = computed(() => [ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO].includes(rolActual.value))

const money = (valor) => Number(valor || 0).toFixed(2)
const textoBusqueda = (valor) => String(valor || '').trim().toLowerCase()
const badgeClass = (estado) => ({
  PREPARANDO: 'warning',
  LISTO: 'ready',
  EN_RUTA: 'info',
  ENTREGADO: 'success',
  CANCELADO: 'danger'
}[estado] || 'default')

const cargarUsuario = () => {
  const usuarioGuardado = sessionStorage.getItem('usuarioActual')
  usuarioActual.value = usuarioGuardado ? JSON.parse(usuarioGuardado) : null
}

const cargarHistorial = async () => {
  if (!usuarioActual.value?.idUsuario || esRepartidor.value) {
    historialVentas.value = []
    return
  }
  try {
    historialVentas.value = await apiGet(`/ventas/usuarios/${usuarioActual.value.idUsuario}/historial`)
  } catch {
    historialVentas.value = []
  }
}

const cargarSesionCaja = async () => {
  if (!usuarioActual.value?.idUsuario || esRepartidor.value) {
    sesionCaja.value = null
    return
  }
  try {
    sesionCaja.value = await apiGet(`/caja/usuarios/${usuarioActual.value.idUsuario}/sesion-actual`)
  } catch {
    sesionCaja.value = null
  }
}

const cargarResumenHoy = async () => {
  if (!usuarioActual.value?.idUsuario || esRepartidor.value) {
    resumenHoy.value = null
    return
  }
  try {
    resumenHoy.value = await apiGet(`/ventas/usuarios/${usuarioActual.value.idUsuario}/resumen-hoy`)
  } catch {
    resumenHoy.value = null
  }
}

const cargarOrdenesActivas = async () => {
  if (!usuarioActual.value?.idUsuario || esRepartidor.value) {
    ordenesActivas.value = []
    return
  }
  try {
    ordenesActivas.value = await apiGet(`/ventas/usuarios/${usuarioActual.value.idUsuario}/activas`)
  } catch {
    ordenesActivas.value = []
  }
}

const sincronizarAsignaciones = (ordenes = []) => {
  const siguientes = {}
  ordenes.forEach((orden) => {
    siguientes[orden.idOrden] = orden.domicilio?.idRepartidor ? String(orden.domicilio.idRepartidor) : ''
  })
  asignacionesPendientes.value = siguientes
}

const cargarColaDomicilios = async () => {
  if (!usuarioActual.value?.idNegocio) {
    colaDomicilios.value = []
    sincronizarAsignaciones()
    return
  }
  try {
    colaDomicilios.value = await apiGet(`/ventas/negocios/${usuarioActual.value.idNegocio}/domicilios-activos`)
    sincronizarAsignaciones(colaDomicilios.value)
  } catch {
    colaDomicilios.value = []
    sincronizarAsignaciones()
  }
}

const cargarRepartidores = async () => {
  if (!usuarioActual.value?.idNegocio || !puedeAsignarRepartidor.value) {
    repartidores.value = []
    return
  }
  try {
    repartidores.value = await apiGet(`/ventas/negocios/${usuarioActual.value.idNegocio}/repartidores`)
  } catch {
    repartidores.value = []
  }
}

const refrescarVentas = async () => {
  await Promise.all([
    cargarSesionCaja(),
    cargarResumenHoy(),
    cargarOrdenesActivas(),
    cargarHistorial(),
    cargarColaDomicilios(),
    cargarRepartidores()
  ])
}

const cotizarOrdenActual = async () => {
  if (!usuarioActual.value?.idUsuario || !carritoStore.items.value.length) {
    cotizacion.value = null
    return
  }
  try {
    cotizacion.value = await apiPost('/ventas/ordenes/cotizacion', construirPayloadOrden())
  } catch {
    cotizacion.value = null
  }
}

const abrirTicket = async (idOrden) => {
  ticketActual.value = await apiGet(`/ventas/ordenes/${idOrden}/ticket`)
  mostrandoTicket.value = true
}

const resetDomicilio = () => {
  domicilio.value = {
    nombreCliente: '',
    direccion: '',
    telefono: '',
    costoEnvio: '0'
  }
}

const construirPayloadItems = () =>
  carritoStore.items.value.map(item => ({
    idProducto: item.idProducto,
    cantidad: item.cantidad,
    modificadores: item.modificadores.map(modificador => ({
      idModificador: modificador.idModificador
    }))
  }))

const validarCajaAbierta = () => {
  if (esRepartidor.value) {
    return false
  }
  if (!sesionCaja.value || sesionCaja.value.estado !== 'ABIERTA') {
    error.value = 'No hay una sesion de caja abierta. Abre caja antes de operar ventas.'
    return false
  }
  return true
}

const construirPayloadOrden = () => ({
  idUsuario: usuarioActual.value.idUsuario,
  tipoOrden: tipoOrden.value,
  domicilio: tipoOrden.value === 'DOMICILIO'
    ? {
        nombreCliente: domicilio.value.nombreCliente,
        direccion: domicilio.value.direccion,
        telefono: domicilio.value.telefono,
        costoEnvio: Number(domicilio.value.costoEnvio || 0)
      }
    : null,
  items: construirPayloadItems()
})

const generarPedido = async () => {
  error.value = ''
  exito.value = ''
  if (!usuarioActual.value) {
    error.value = 'No hay un usuario autenticado'
    return
  }
  if (!carritoStore.items.value.length) {
    error.value = 'Agrega productos al carrito antes de generar el pedido'
    return
  }
  if (!validarCajaAbierta()) {
    return
  }

  cargando.value = true
  try {
    const orden = await apiPost('/ventas/ordenes', construirPayloadOrden())
    exito.value = `Pedido ${orden.folio} generado`
    limpiarCarrito()
    resetDomicilio()
    tipoOrden.value = 'MOSTRADOR'
    cotizacion.value = null
    await refrescarVentas()
  } catch (err) {
    error.value = err.message || 'No fue posible generar el pedido'
  } finally {
    cargando.value = false
  }
}

const cobrarOrden = async (idOrden) => {
  await apiPost(`/ventas/ordenes/${idOrden}/pago`, {
    metodo: metodo.value,
    efectivoRecibido: metodo.value === 'EFECTIVO' ? Number(efectivoRecibido.value || 0) : null
  })
  efectivoRecibido.value = ''
  await abrirTicket(idOrden)
  await refrescarVentas()
}

const cobrarCarritoAhora = async () => {
  error.value = ''
  exito.value = ''
  if (!usuarioActual.value) {
    error.value = 'No hay un usuario autenticado'
    return
  }
  if (!carritoStore.items.value.length) {
    error.value = 'Agrega productos al carrito antes de cobrar'
    return
  }
  if (!validarCajaAbierta()) {
    return
  }

  cargando.value = true
  try {
    const orden = await apiPost('/ventas/ordenes', construirPayloadOrden())
    await cobrarOrden(orden.idOrden)
    exito.value = `Venta cobrada correctamente. ${orden.folio}`
    limpiarCarrito()
    resetDomicilio()
    tipoOrden.value = 'MOSTRADOR'
    cotizacion.value = null
  } catch (err) {
    error.value = err.message || 'No fue posible procesar la venta'
  } finally {
    cargando.value = false
  }
}

const cobrarPedidoActivo = async (orden) => {
  error.value = ''
  exito.value = ''
  if (!validarCajaAbierta()) {
    return
  }
  cargando.value = true
  try {
    await cobrarOrden(orden.idOrden)
    exito.value = `Pedido ${orden.folio} cobrado correctamente`
  } catch (err) {
    error.value = err.message || 'No fue posible cobrar el pedido'
  } finally {
    cargando.value = false
  }
}

const cancelarPedidoActivo = async (orden) => {
  const motivo = window.prompt(`Motivo de cancelación para ${orden.folio}:`)
  if (!motivo) return
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    await apiPost(`/ventas/ordenes/${orden.idOrden}/cancelacion`, { motivo })
    exito.value = `Pedido ${orden.folio} cancelado`
    await refrescarVentas()
  } catch (err) {
    error.value = err.message || 'No fue posible cancelar el pedido'
  } finally {
    cargando.value = false
  }
}

const actualizarEstadoOrden = async (orden, estado) => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    await apiPost(`/ventas/ordenes/${orden.idOrden}/estado`, { estado })
    exito.value = `Pedido ${orden.folio} actualizado a ${estado}`
    await refrescarVentas()
  } catch (err) {
    error.value = err.message || 'No fue posible actualizar el estado del pedido'
  } finally {
    cargando.value = false
  }
}

const guardarAsignacionRepartidor = async (orden) => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    await apiPost(`/ventas/ordenes/${orden.idOrden}/domicilio/asignacion`, {
      idRepartidor: asignacionesPendientes.value[orden.idOrden] ? Number(asignacionesPendientes.value[orden.idOrden]) : null
    })
    exito.value = `Asignación actualizada para ${orden.folio}`
    await refrescarVentas()
  } catch (err) {
    error.value = err.message || 'No fue posible actualizar el repartidor'
  } finally {
    cargando.value = false
  }
}

const limpiarAsignacion = async (orden) => {
  asignacionesPendientes.value[orden.idOrden] = ''
  await guardarAsignacionRepartidor(orden)
}

const obtenerLineaCotizacion = (item) =>
  cotizacion.value?.items?.find(linea =>
    linea.idProducto === item.idProducto
    && linea.cantidad === item.cantidad
    && JSON.stringify((linea.modificadores || []).map(mod => mod.idModificador).sort())
      === JSON.stringify((item.modificadores || []).map(mod => mod.idModificador).sort())
  )

const imprimirTicket = () => {
  mostrandoTicket.value = true
  window.print()
}

const coincideBusquedaOrden = (orden, busqueda) => {
  if (!busqueda) return true
  const texto = [
    orden.folio,
    orden.tipoOrden,
    orden.estado,
    orden.pagada ? 'pagada' : 'pendiente',
    orden.domicilio?.nombreCliente,
    orden.domicilio?.telefono,
    orden.domicilio?.direccion,
    orden.domicilio?.nombreRepartidor
  ].filter(Boolean).join(' ').toLowerCase()
  return texto.includes(busqueda)
}

const filtrarOrdenesActivas = (ordenes) => {
  const busqueda = textoBusqueda(filtroActivas.value.busqueda)
  return ordenes.filter((orden) => {
    if (!coincideBusquedaOrden(orden, busqueda)) return false
    if (filtroActivas.value.estado !== 'TODOS' && orden.estado !== filtroActivas.value.estado) return false
    if (filtroActivas.value.pago === 'PAGADAS' && !orden.pagada) return false
    if (filtroActivas.value.pago === 'PENDIENTES' && orden.pagada) return false
    return true
  })
}

const pedidosMostrador = computed(() =>
  filtrarOrdenesActivas(ordenesActivas.value.filter((orden) => orden.tipoOrden === 'MOSTRADOR'))
)

const pedidosDomicilio = computed(() =>
  filtrarOrdenesActivas(colaDomicilios.value)
)

const filtrarVentas = (ventas) => {
  const busqueda = textoBusqueda(filtroVentas.value.busqueda)
  return ventas.filter((venta) => {
    const texto = [
      venta.folio,
      venta.tipoOrden,
      venta.metodoPago,
      venta.estado
    ].filter(Boolean).join(' ').toLowerCase()
    if (busqueda && !texto.includes(busqueda)) return false
    if (filtroVentas.value.tipo !== 'TODOS' && venta.tipoOrden !== filtroVentas.value.tipo) return false
    if (filtroVentas.value.metodo !== 'TODOS' && venta.metodoPago !== filtroVentas.value.metodo) return false
    if (filtroVentas.value.estado !== 'TODOS' && venta.estado !== filtroVentas.value.estado) return false
    return true
  })
}

const ventasDiaFiltradas = computed(() => filtrarVentas(resumenHoy.value?.ventasPagadas || []))
const historialFiltrado = computed(() => filtrarVentas(historialVentas.value))

const siguienteAccionEstado = (orden) => {
  if (orden.tipoOrden === 'MOSTRADOR') {
    if (orden.estado === 'PREPARANDO') return { label: 'Marcar listo', estado: 'LISTO' }
    if (orden.estado === 'LISTO' && orden.pagada) return { label: 'Entregar', estado: 'ENTREGADO' }
    return null
  }

  if (orden.estado === 'PREPARANDO') return { label: 'Marcar listo', estado: 'LISTO' }
  if (orden.estado === 'LISTO') return { label: 'Enviar', estado: 'EN_RUTA' }
  if (orden.estado === 'EN_RUTA' && orden.pagada) return { label: 'Entregado', estado: 'ENTREGADO' }
  return null
}

onMounted(async () => {
  cargarUsuario()
  await refrescarVentas()
  await cotizarOrdenActual()
})

watch(
  () => JSON.stringify({
    tipoOrden: tipoOrden.value,
    domicilio: domicilio.value,
    items: carritoStore.items.value.map(item => ({
      clave: item.clave,
      cantidad: item.cantidad
    }))
  }),
  () => {
    cotizarOrdenActual()
  }
)
</script>

<template>
  <section class="ventas-layout">
    <div class="ventas-card no-print">
      <div class="ventas-header">
        <div>
          <h1>VENTAS</h1>
          <p v-if="!esRepartidor">Operación diaria, pedidos activos y ventas cerradas.</p>
          <p v-else>Pedidos de domicilio asignados al repartidor.</p>
        </div>
        <div v-if="!esRepartidor" class="ventas-total">
          <span>TOTAL ACTUAL</span>
          <strong>${{ totalCobro.toFixed(2) }}</strong>
          <small v-if="tipoOrden === 'DOMICILIO'">Incluye envío: ${{ money(costoEnvioActual) }}</small>
        </div>
      </div>

      <div class="summary-grid" v-if="resumenHoy">
        <div class="summary-card"><span>Operación del día</span><strong>{{ resumenHoy.cantidadOperaciones }}</strong></div>
        <div class="summary-card"><span>Ventas cobradas</span><strong>${{ money(resumenHoy.totalVentas) }}</strong></div>
        <div class="summary-card"><span>Pedidos activos</span><strong>{{ resumenHoy.cantidadActivas }}</strong></div>
        <div class="summary-card"><span>Cancelados</span><strong>{{ resumenHoy.cantidadCanceladas }}</strong></div>
      </div>

      <p v-if="error" class="status error">{{ error }}</p>
      <p v-if="exito" class="status success">{{ exito }}</p>
      <p v-if="!esRepartidor && (!sesionCaja || sesionCaja.estado !== 'ABIERTA')" class="status warning">
        No hay caja abierta. Ve al módulo CAJA para abrir una sesión antes de cobrar o generar pedidos.
      </p>

      <div class="ventas-grid" :class="{ single: esRepartidor }">
        <div v-if="!esRepartidor" class="ventas-main">
          <div class="ventas-mode">
            <button class="mode-btn" :class="{ active: tipoOrden === 'MOSTRADOR' }" @click="tipoOrden = 'MOSTRADOR'">MOSTRADOR</button>
            <button class="mode-btn" :class="{ active: tipoOrden === 'DOMICILIO' }" @click="tipoOrden = 'DOMICILIO'">DOMICILIO</button>
          </div>

          <div v-if="tipoOrden === 'DOMICILIO'" class="domicilio-form">
            <label class="field">
              <span>Cliente</span>
              <input v-model="domicilio.nombreCliente" type="text" />
            </label>
            <label class="field">
              <span>Teléfono</span>
              <input v-model="domicilio.telefono" type="text" />
            </label>
            <label class="field full">
              <span>Dirección</span>
              <input v-model="domicilio.direccion" type="text" />
            </label>
            <label class="field">
              <span>Costo de envío</span>
              <input v-model="domicilio.costoEnvio" type="number" min="0" step="0.01" placeholder="0.00" />
            </label>
          </div>

          <div v-if="!carritoStore.items.value.length" class="empty-state">No hay productos en el carrito.</div>

          <div v-else class="ventas-body">
            <div class="items-panel">
              <div v-for="item in carritoStore.items.value" :key="item.clave" class="item-row">
                <div class="item-info">
                  <strong>{{ item.nombre }}</strong>
                  <span>${{ Number(item.precioUnitarioFinal).toFixed(2) }} c/u</span>
                  <ul v-if="item.modificadores.length" class="mods-inline">
                    <li v-for="modificador in item.modificadores" :key="modificador.idModificador">
                      {{ modificador.nombre }} (+${{ Number(modificador.precioExtra).toFixed(2) }})
                    </li>
                  </ul>
                  <ul v-if="obtenerLineaCotizacion(item)?.promociones?.length" class="mods-inline promo-list">
                    <li v-for="promocion in obtenerLineaCotizacion(item).promociones" :key="`${item.clave}-${promocion.nombre}`">
                      Promo: {{ promocion.nombre }} (-${{ Number(promocion.descuento).toFixed(2) }})
                    </li>
                  </ul>
                </div>
                <div class="item-actions">
                  <button class="qty-btn" @click="decrementarItem(item.clave)">-</button>
                  <span class="qty">{{ item.cantidad }}</span>
                  <button class="qty-btn" @click="incrementarItem(item.clave)">+</button>
                </div>
                <div class="item-subtotal">${{ (Number(item.precioUnitarioFinal) * item.cantidad).toFixed(2) }}</div>
              </div>
            </div>

            <div class="checkout-panel">
              <label class="field">
                <span>Método de pago</span>
                <select v-model="metodo">
                  <option value="EFECTIVO">EFECTIVO</option>
                  <option value="TARJETA">TARJETA</option>
                </select>
              </label>
              <label v-if="metodo === 'EFECTIVO'" class="field">
                <span>Efectivo recibido</span>
                <input v-model="efectivoRecibido" type="number" min="0" step="0.01" placeholder="0.00" />
              </label>
              <div class="resume-line"><span>Artículos</span><strong>{{ carritoStore.totalItems.value }}</strong></div>
              <div v-if="tipoOrden === 'DOMICILIO'" class="resume-line"><span>Envío</span><strong>${{ money(costoEnvioActual) }}</strong></div>
              <div v-if="totalDescuentoPromo > 0" class="resume-line"><span>Promociones</span><strong>-${{ money(totalDescuentoPromo) }}</strong></div>
              <div v-if="metodo === 'EFECTIVO'" class="resume-line">
                <span>Cambio</span>
                <strong>${{ cambioCalculado.toFixed(2) }}</strong>
              </div>
              <div class="resume-line total-line"><span>Total</span><strong>${{ totalCobro.toFixed(2) }}</strong></div>
              <button class="btn-cobrar secondary" :disabled="cargando" @click="generarPedido">{{ cargando ? 'PROCESANDO...' : 'GENERAR PEDIDO' }}</button>
              <button class="btn-cobrar" :disabled="cargando" @click="cobrarCarritoAhora">{{ cargando ? 'PROCESANDO...' : 'COBRAR AHORA' }}</button>
            </div>
          </div>
        </div>

        <aside class="side-panel">
          <div class="panel-box no-pad">
            <div class="panel-head with-border">
              <h2>Filtros operativos</h2>
            </div>
            <div class="filters-grid">
              <label class="field">
                <span>Búsqueda</span>
                <input v-model="filtroActivas.busqueda" type="text" placeholder="Folio, cliente, repartidor..." />
              </label>
              <label class="field">
                <span>Estado</span>
                <select v-model="filtroActivas.estado">
                  <option value="TODOS">Todos</option>
                  <option value="PREPARANDO">Preparando</option>
                  <option value="LISTO">Listo</option>
                  <option value="EN_RUTA">En ruta</option>
                </select>
              </label>
              <label class="field">
                <span>Cobro</span>
                <select v-model="filtroActivas.pago">
                  <option value="TODOS">Todos</option>
                  <option value="PAGADAS">Pagadas</option>
                  <option value="PENDIENTES">Pendientes</option>
                </select>
              </label>
            </div>
          </div>

          <div class="panel-box">
            <div class="panel-head">
              <h2>{{ esRepartidor ? 'Pedidos asignados' : 'Pedidos domicilio' }}</h2>
              <span>{{ pedidosDomicilio.length }}</span>
            </div>
            <div v-if="!pedidosDomicilio.length" class="historial-empty">Sin pedidos de domicilio activos.</div>
            <div v-else class="historial-list">
              <div v-for="orden in pedidosDomicilio" :key="`dom-${orden.idOrden}`" class="active-order">
                <div class="order-stack">
                  <div class="order-line">
                    <strong>{{ orden.folio }}</strong>
                    <span class="status-pill" :class="badgeClass(orden.estado)">{{ orden.estado }}</span>
                    <span class="payment-pill" :class="orden.pagada ? 'paid' : 'pending'">{{ orden.pagada ? 'PAGADA' : 'PENDIENTE DE COBRO' }}</span>
                  </div>
                  <span>{{ new Date(orden.fecha).toLocaleString() }}</span>
                  <span>{{ orden.domicilio?.nombreCliente }}</span>
                  <span>{{ orden.domicilio?.telefono }}</span>
                  <span>{{ orden.domicilio?.direccion }}</span>
                  <span>Envío: ${{ money(orden.domicilio?.costoEnvio) }}</span>
                  <span>Repartidor actual: {{ orden.domicilio?.nombreRepartidor || 'Sin asignar' }}</span>
                </div>
                <div class="active-meta">
                  <strong>${{ money(orden.total) }}</strong>
                  <div class="active-actions">
                    <template v-if="puedeAsignarRepartidor">
                      <select v-model="asignacionesPendientes[orden.idOrden]" class="mini-select">
                        <option value="">Sin asignar</option>
                        <option v-for="repartidor in repartidores" :key="repartidor.idUsuario" :value="String(repartidor.idUsuario)">{{ repartidor.nombre }}</option>
                      </select>
                      <button class="mini-btn" @click="guardarAsignacionRepartidor(orden)">
                        {{ orden.domicilio?.idRepartidor ? 'Cambiar repartidor' : 'Asignar repartidor' }}
                      </button>
                      <button v-if="orden.domicilio?.idRepartidor" class="mini-btn ghost" @click="limpiarAsignacion(orden)">Quitar</button>
                    </template>
                    <button
                      v-if="siguienteAccionEstado(orden)"
                      class="mini-btn"
                      @click="actualizarEstadoOrden(orden, siguienteAccionEstado(orden).estado)"
                    >
                      {{ siguienteAccionEstado(orden).label }}
                    </button>
                    <button v-if="!esRepartidor && !orden.pagada" class="mini-btn" @click="cobrarPedidoActivo(orden)">Cobrar</button>
                    <button v-if="!esRepartidor" class="mini-btn danger" @click="cancelarPedidoActivo(orden)">Cancelar</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <template v-if="!esRepartidor">
            <div class="panel-box">
              <div class="panel-head">
                <h2>Pedidos mostrador</h2>
                <span>{{ pedidosMostrador.length }}</span>
              </div>
              <div v-if="!pedidosMostrador.length" class="historial-empty">Sin pedidos de mostrador activos.</div>
              <div v-else class="historial-list">
                <div v-for="orden in pedidosMostrador" :key="orden.idOrden" class="active-order">
                  <div class="order-stack">
                    <div class="order-line">
                      <strong>{{ orden.folio }}</strong>
                      <span class="status-pill" :class="badgeClass(orden.estado)">{{ orden.estado }}</span>
                      <span class="payment-pill" :class="orden.pagada ? 'paid' : 'pending'">{{ orden.pagada ? 'PAGADA' : 'PENDIENTE DE COBRO' }}</span>
                    </div>
                    <span>{{ new Date(orden.fecha).toLocaleString() }}</span>
                    <span>{{ orden.cantidadItems }} artículos</span>
                  </div>
                  <div class="active-meta">
                    <strong>${{ money(orden.total) }}</strong>
                    <div class="active-actions">
                      <button
                        v-if="siguienteAccionEstado(orden)"
                        class="mini-btn"
                        @click="actualizarEstadoOrden(orden, siguienteAccionEstado(orden).estado)"
                      >
                        {{ siguienteAccionEstado(orden).label }}
                      </button>
                      <button v-if="!orden.pagada" class="mini-btn" @click="cobrarPedidoActivo(orden)">Cobrar</button>
                      <button class="mini-btn danger" @click="cancelarPedidoActivo(orden)">Cancelar</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="panel-box no-pad">
              <div class="panel-head with-border">
                <h2>Filtros ventas</h2>
              </div>
              <div class="filters-grid">
                <label class="field">
                  <span>Búsqueda</span>
                  <input v-model="filtroVentas.busqueda" type="text" placeholder="Folio o método..." />
                </label>
                <label class="field">
                  <span>Tipo</span>
                  <select v-model="filtroVentas.tipo">
                    <option value="TODOS">Todos</option>
                    <option value="MOSTRADOR">Mostrador</option>
                    <option value="DOMICILIO">Domicilio</option>
                  </select>
                </label>
                <label class="field">
                  <span>Método</span>
                  <select v-model="filtroVentas.metodo">
                    <option value="TODOS">Todos</option>
                    <option value="EFECTIVO">Efectivo</option>
                    <option value="TARJETA">Tarjeta</option>
                  </select>
                </label>
                <label class="field">
                  <span>Estado</span>
                  <select v-model="filtroVentas.estado">
                    <option value="TODOS">Todos</option>
                    <option value="PREPARANDO">Preparando</option>
                    <option value="LISTO">Listo</option>
                    <option value="EN_RUTA">En ruta</option>
                    <option value="ENTREGADO">Entregado</option>
                  </select>
                </label>
              </div>
            </div>

            <div class="panel-box">
              <div class="panel-head"><h2>Ventas del día</h2></div>
              <div v-if="!ventasDiaFiltradas.length" class="historial-empty">Sin ventas pagadas hoy con ese filtro.</div>
              <div v-else class="historial-list">
                <button v-for="venta in ventasDiaFiltradas" :key="venta.idOrden" class="historial-item" @click="abrirTicket(venta.idOrden)">
                  <div class="order-stack">
                    <div class="order-line">
                      <strong>{{ venta.folio }}</strong>
                      <span class="status-pill" :class="badgeClass(venta.estado)">{{ venta.estado }}</span>
                    </div>
                    <span>{{ venta.tipoOrden }}</span>
                    <span>{{ new Date(venta.fecha).toLocaleTimeString() }}</span>
                  </div>
                  <div class="historial-meta">
                    <span>{{ venta.metodoPago }}</span>
                    <strong>${{ Number(venta.total).toFixed(2) }}</strong>
                  </div>
                </button>
              </div>
            </div>

            <div class="panel-box">
              <div class="panel-head"><h2>Historial</h2></div>
              <div v-if="!historialFiltrado.length" class="historial-empty">Sin ventas previas con ese filtro.</div>
              <div v-else class="historial-list">
                <button v-for="venta in historialFiltrado" :key="`hist-${venta.idOrden}`" class="historial-item" @click="abrirTicket(venta.idOrden)">
                  <div class="order-stack">
                    <div class="order-line">
                      <strong>{{ venta.folio }}</strong>
                      <span class="status-pill" :class="badgeClass(venta.estado)">{{ venta.estado }}</span>
                    </div>
                    <span>{{ venta.tipoOrden }}</span>
                    <span>{{ new Date(venta.fecha).toLocaleString() }}</span>
                  </div>
                  <div class="historial-meta">
                    <span>{{ venta.metodoPago }}</span>
                    <strong>${{ Number(venta.total).toFixed(2) }}</strong>
                  </div>
                </button>
              </div>
            </div>
          </template>
        </aside>
      </div>
    </div>

    <div v-if="ticketActual && mostrandoTicket" class="modal-overlay no-print" @click.self="mostrandoTicket = false">
      <div class="ticket-modal">
        <div class="ticket-paper print-area">
          <div class="ticket-head">
            <h2>{{ ticketActual.negocio }}</h2>
            <p>{{ ticketActual.folio }}</p>
            <p>{{ ticketActual.tipoOrden }}</p>
            <p>{{ ticketActual.cajero }}</p>
            <p>{{ new Date(ticketActual.fecha).toLocaleString() }}</p>
          </div>
          <div v-if="ticketActual.domicilio" class="ticket-domicilio">
            <p>{{ ticketActual.domicilio.nombreCliente }}</p>
            <p>{{ ticketActual.domicilio.telefono }}</p>
            <p>{{ ticketActual.domicilio.direccion }}</p>
            <p>Estado: {{ ticketActual.domicilio.estadoEntrega }}</p>
            <p>Envío: ${{ money(ticketActual.domicilio.costoEnvio) }}</p>
            <p v-if="ticketActual.domicilio.nombreRepartidor">Repartidor: {{ ticketActual.domicilio.nombreRepartidor }}</p>
          </div>
          <div class="ticket-divider"></div>
          <div v-for="item in ticketActual.items" :key="`${ticketActual.idOrden}-${item.idProducto}-${item.nombre}`" class="ticket-item">
            <div class="ticket-item-main">
              <span>{{ item.cantidad }} x {{ item.nombre }}</span>
              <strong>${{ Number(item.subtotal).toFixed(2) }}</strong>
            </div>
            <div v-for="modificador in item.modificadores" :key="modificador.idModificador" class="ticket-mod">
              <span>+ {{ modificador.nombre }}</span>
              <span>${{ Number(modificador.precioExtra).toFixed(2) }}</span>
            </div>
            <div v-for="promocion in item.promociones || []" :key="`promo-${promocion.nombre}`" class="ticket-mod promo-line">
              <span>- {{ promocion.nombre }}</span>
              <span>-${{ Number(promocion.descuento).toFixed(2) }}</span>
            </div>
          </div>
          <div v-if="ticketActual.domicilio?.costoEnvio" class="ticket-divider"></div>
          <div v-if="ticketActual.domicilio?.costoEnvio" class="ticket-mod ticket-shipping">
            <span>Envío</span>
            <span>${{ money(ticketActual.domicilio.costoEnvio) }}</span>
          </div>
          <div class="ticket-divider"></div>
          <div class="ticket-totals">
            <div v-if="Number(ticketActual.totalDescuentoPromocional || 0) > 0"><span>Promos</span><strong>-${{ Number(ticketActual.totalDescuentoPromocional).toFixed(2) }}</strong></div>
            <div><span>Total</span><strong>${{ Number(ticketActual.total).toFixed(2) }}</strong></div>
            <div><span>Método</span><strong>{{ ticketActual.metodoPago }}</strong></div>
            <div v-if="ticketActual.efectivoRecibido !== null"><span>Efectivo</span><strong>${{ Number(ticketActual.efectivoRecibido).toFixed(2) }}</strong></div>
            <div v-if="ticketActual.cambio !== null"><span>Cambio</span><strong>${{ Number(ticketActual.cambio).toFixed(2) }}</strong></div>
          </div>
        </div>
        <div class="ticket-modal-actions">
          <button class="btn-ticket" @click="mostrandoTicket = false">Cerrar</button>
          <button class="btn-ticket primary" @click="imprimirTicket">Imprimir</button>
        </div>
      </div>
    </div>

    <div v-if="ticketActual" class="print-only">
      <div class="ticket-paper print-area">
        <div class="ticket-head">
          <h2>{{ ticketActual.negocio }}</h2>
          <p>{{ ticketActual.folio }}</p>
          <p>{{ ticketActual.tipoOrden }}</p>
          <p>{{ ticketActual.cajero }}</p>
          <p>{{ new Date(ticketActual.fecha).toLocaleString() }}</p>
        </div>
        <div v-if="ticketActual.domicilio" class="ticket-domicilio">
          <p>{{ ticketActual.domicilio.nombreCliente }}</p>
          <p>{{ ticketActual.domicilio.telefono }}</p>
          <p>{{ ticketActual.domicilio.direccion }}</p>
          <p>Estado: {{ ticketActual.domicilio.estadoEntrega }}</p>
          <p>Envío: ${{ money(ticketActual.domicilio.costoEnvio) }}</p>
          <p v-if="ticketActual.domicilio.nombreRepartidor">Repartidor: {{ ticketActual.domicilio.nombreRepartidor }}</p>
        </div>
        <div class="ticket-divider"></div>
        <div v-for="item in ticketActual.items" :key="`print-${ticketActual.idOrden}-${item.idProducto}-${item.nombre}`" class="ticket-item">
          <div class="ticket-item-main">
            <span>{{ item.cantidad }} x {{ item.nombre }}</span>
            <strong>${{ Number(item.subtotal).toFixed(2) }}</strong>
          </div>
          <div v-for="modificador in item.modificadores" :key="`print-mod-${modificador.idModificador}`" class="ticket-mod">
            <span>+ {{ modificador.nombre }}</span>
            <span>${{ Number(modificador.precioExtra).toFixed(2) }}</span>
          </div>
          <div v-for="promocion in item.promociones || []" :key="`print-promo-${promocion.nombre}`" class="ticket-mod promo-line">
            <span>- {{ promocion.nombre }}</span>
            <span>-${{ Number(promocion.descuento).toFixed(2) }}</span>
          </div>
        </div>
        <div v-if="ticketActual.domicilio?.costoEnvio" class="ticket-divider"></div>
        <div v-if="ticketActual.domicilio?.costoEnvio" class="ticket-mod ticket-shipping">
          <span>Envío</span>
          <span>${{ money(ticketActual.domicilio.costoEnvio) }}</span>
        </div>
        <div class="ticket-divider"></div>
        <div class="ticket-totals">
          <div v-if="Number(ticketActual.totalDescuentoPromocional || 0) > 0"><span>Promos</span><strong>-${{ Number(ticketActual.totalDescuentoPromocional).toFixed(2) }}</strong></div>
          <div><span>Total</span><strong>${{ Number(ticketActual.total).toFixed(2) }}</strong></div>
          <div><span>Método</span><strong>{{ ticketActual.metodoPago }}</strong></div>
          <div v-if="ticketActual.efectivoRecibido !== null"><span>Efectivo</span><strong>${{ Number(ticketActual.efectivoRecibido).toFixed(2) }}</strong></div>
          <div v-if="ticketActual.cambio !== null"><span>Cambio</span><strong>${{ Number(ticketActual.cambio).toFixed(2) }}</strong></div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.ventas-layout { width:100%; display:flex; justify-content:center; }
.ventas-card { width:98%; max-width:1600px; background:#fff; border:4px solid #000; border-radius:30px; padding:2rem; box-shadow:12px 12px 0 rgba(0,0,0,.1); }
.ventas-header { display:flex; justify-content:space-between; align-items:flex-start; gap:1rem; margin-bottom:1rem; }
.ventas-header h1 { margin:0; font-family:var(--font-main); font-size:2.8rem; color:var(--rojo-may); }
.ventas-header p { margin:.2rem 0 0; font-weight:bold; }
.ventas-total { text-align:right; display:flex; flex-direction:column; gap:.2rem; }
.ventas-total span,.ventas-total small { display:block; font-weight:bold; }
.ventas-total strong { font-size:2rem; }
.summary-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:.9rem; margin-bottom:1rem; }
.summary-card { border:3px solid #000; border-radius:18px; padding:1rem; background:#fff5d6; }
.summary-card span { display:block; font-weight:bold; }
.summary-card strong { font-size:1.4rem; }
.status { margin:0 0 1rem; font-weight:bold; }
.status.error { color:#b71c1c; }
.status.success { color:#1b5e20; }
.status.warning { color:#9a3412; }
.ventas-grid { display:grid; grid-template-columns:1.25fr .9fr; gap:1.5rem; }
.ventas-grid.single { grid-template-columns:1fr; }
.ventas-mode { display:flex; gap:.75rem; margin-bottom:1rem; }
.mode-btn { border:3px solid #000; border-radius:12px; padding:.8rem 1rem; background:#fff; font-weight:800; cursor:pointer; }
.mode-btn.active { background:#fff1b8; }
.domicilio-form { display:grid; grid-template-columns:1fr 1fr; gap:1rem; margin-bottom:1rem; }
.domicilio-form .full { grid-column:1 / -1; }
.empty-state,.historial-empty { padding:2rem; border:3px dashed #000; border-radius:20px; text-align:center; font-weight:bold; }
.ventas-body { display:grid; grid-template-columns:1.4fr .8fr; gap:1.5rem; }
.items-panel,.checkout-panel,.panel-box { border:3px solid #000; border-radius:20px; padding:1rem; }
.panel-box.no-pad { padding:0; overflow:hidden; }
.panel-head.with-border { padding:1rem 1rem 0; }
.items-panel { display:flex; flex-direction:column; gap:.8rem; }
.item-row { display:grid; grid-template-columns:1fr auto auto; gap:1rem; align-items:center; border-bottom:2px solid #f0f0f0; padding-bottom:.8rem; }
.item-info span { display:block; color:#555; font-size:.9rem; }
.mods-inline { margin:.45rem 0 0; padding-left:1rem; color:#444; font-size:.82rem; }
.item-actions { display:flex; align-items:center; gap:.8rem; }
.qty-btn { width:34px; height:34px; border-radius:50%; border:2px solid var(--rojo-may); background:white; color:var(--rojo-may); font-size:1.2rem; font-weight:900; cursor:pointer; }
.qty { min-width:24px; text-align:center; font-weight:bold; }
.item-subtotal { font-weight:bold; }
.checkout-panel,.side-panel { display:flex; flex-direction:column; gap:1rem; }
.field { display:flex; flex-direction:column; gap:.4rem; min-width:0; }
.field span { font-weight:bold; }
.field input,.field select { padding:.8rem 1rem; border:2px solid #000; border-radius:12px; width:100%; box-sizing:border-box; }
.filters-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:1rem; padding:1rem; }
.resume-line { display:flex; justify-content:space-between; align-items:center; font-weight:bold; }
.total-line { font-size:1.2rem; }
.btn-cobrar { padding:1rem; border:3px solid #000; border-radius:14px; background:var(--rojo-may); color:white; font-family:var(--font-main); font-size:1.5rem; cursor:pointer; }
.btn-cobrar.secondary { background:#fff1b8; color:#111; }
.btn-cobrar:disabled { opacity:.7; cursor:wait; }
.panel-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem; gap:.75rem; }
.panel-head h2 { margin:0; }
.historial-list { display:flex; flex-direction:column; gap:.75rem; }
.historial-item,.active-order { width:100%; display:flex; justify-content:space-between; gap:1rem; align-items:flex-start; text-align:left; padding:.9rem; border:2px solid #000; border-radius:14px; background:white; }
.historial-item { cursor:pointer; }
.historial-item span,.active-order span { display:block; font-size:.85rem; color:#555; }
.order-stack { display:flex; flex-direction:column; gap:.22rem; min-width:0; }
.order-line { display:flex; flex-wrap:wrap; gap:.45rem; align-items:center; }
.historial-meta,.active-meta { text-align:right; min-width:190px; }
.active-actions { display:flex; flex-wrap:wrap; gap:.5rem; margin-top:.75rem; justify-content:flex-end; }
.btn-ticket,.mini-btn,.mini-select { border:2px solid #000; border-radius:12px; padding:.7rem .9rem; background:white; font-weight:bold; cursor:pointer; }
.mini-btn.ghost { background:#f4f4f4; }
.mini-btn.danger,.btn-ticket.primary { background:#d32f2f; color:white; }
.mini-select { max-width:220px; }
.status-pill,.payment-pill { display:inline-flex; align-items:center; justify-content:center; border-radius:999px; padding:.22rem .65rem; border:2px solid #000; font-size:.72rem !important; font-weight:900; color:#111 !important; background:#fff; }
.status-pill.warning { background:#fde68a; }
.status-pill.ready { background:#bbf7d0; }
.status-pill.info { background:#bfdbfe; }
.status-pill.success { background:#86efac; }
.status-pill.danger { background:#fca5a5; }
.payment-pill.paid { background:#dcfce7; }
.payment-pill.pending { background:#fee2e2; }
.ticket-modal-actions { display:flex; gap:.75rem; margin-top:1rem; flex-wrap:wrap; }
.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,.45); display:flex; align-items:center; justify-content:center; z-index:1300; }
.ticket-modal { width:min(420px,92vw); background:white; border:4px solid #000; border-radius:24px; padding:1rem; }
.ticket-paper { width:100%; max-width:320px; margin:0 auto; background:white; color:#000; border:2px dashed #000; padding:1rem; font-family:monospace; }
.ticket-head { text-align:center; }
.ticket-head h2 { margin:0; font-size:1.1rem; }
.ticket-head p,.ticket-domicilio p { margin:.2rem 0; }
.ticket-divider { border-top:1px dashed #000; margin:.8rem 0; }
.ticket-item { margin-bottom:.65rem; }
.ticket-item-main,.ticket-mod,.ticket-totals div { display:flex; justify-content:space-between; gap:1rem; }
.ticket-mod { font-size:.9rem; padding-left:.8rem; }
.ticket-shipping { padding-left:0; }
.ticket-totals { display:flex; flex-direction:column; gap:.35rem; }
.print-only { display:none; }
@media (max-width:1280px) {
  .summary-grid,.filters-grid { grid-template-columns:repeat(2,1fr); }
  .ventas-grid,.ventas-body,.domicilio-form { grid-template-columns:1fr; }
}
@media (max-width:760px) {
  .summary-grid,.filters-grid { grid-template-columns:1fr; }
  .item-row,.historial-item,.active-order { grid-template-columns:1fr; }
  .historial-item,.active-order,.ventas-header { flex-direction:column; }
  .historial-meta,.active-meta { min-width:0; width:100%; text-align:left; }
  .active-actions { justify-content:flex-start; }
}
@media print {
  .no-print { display:none !important; }
  .print-only { display:block; }
  .print-area { border:none; box-shadow:none; max-width:100%; width:280px; }
}
</style>
