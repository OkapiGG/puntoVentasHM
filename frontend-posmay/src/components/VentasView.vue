<script setup>
import { computed, onMounted, ref } from 'vue'
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
const tipoOrden = ref('MOSTRADOR')
const domicilio = ref({
  nombreCliente: '',
  direccion: '',
  telefono: ''
})

const total = computed(() => carritoStore.totalImporte.value)
const cambioCalculado = computed(() => {
  const efectivo = Number(efectivoRecibido.value || 0)
  return efectivo > total.value ? efectivo - total.value : 0
})
const rolActual = computed(() => usuarioActual.value?.rol || '')
const esRepartidor = computed(() => rolActual.value === ROLES.REPARTIDOR)
const puedeAsignarRepartidor = computed(() => [ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO].includes(rolActual.value))

const money = (valor) => Number(valor || 0).toFixed(2)

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

const cargarColaDomicilios = async () => {
  if (!usuarioActual.value?.idNegocio) {
    colaDomicilios.value = []
    return
  }
  try {
    colaDomicilios.value = await apiGet(`/ventas/negocios/${usuarioActual.value.idNegocio}/domicilios-activos`)
  } catch {
    colaDomicilios.value = []
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

const abrirTicket = async (idOrden) => {
  ticketActual.value = await apiGet(`/ventas/ordenes/${idOrden}/ticket`)
  mostrandoTicket.value = true
}

const resetDomicilio = () => {
  domicilio.value = {
    nombreCliente: '',
    direccion: '',
    telefono: ''
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
        telefono: domicilio.value.telefono
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

const actualizarEstadoDomicilio = async (orden, estadoEntrega) => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    await apiPost(`/ventas/ordenes/${orden.idOrden}/domicilio/estado`, { estadoEntrega })
    exito.value = `Pedido ${orden.folio} actualizado a ${estadoEntrega}`
    await refrescarVentas()
  } catch (err) {
    error.value = err.message || 'No fue posible actualizar el estado de domicilio'
  } finally {
    cargando.value = false
  }
}

const asignarRepartidor = async (orden, idRepartidor) => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    await apiPost(`/ventas/ordenes/${orden.idOrden}/domicilio/asignacion`, {
      idRepartidor: idRepartidor ? Number(idRepartidor) : null
    })
    exito.value = `Pedido ${orden.folio} asignado`
    await refrescarVentas()
  } catch (err) {
    error.value = err.message || 'No fue posible asignar repartidor'
  } finally {
    cargando.value = false
  }
}

const imprimirTicket = () => {
  mostrandoTicket.value = true
  window.print()
}

onMounted(async () => {
  cargarUsuario()
  await refrescarVentas()
})
</script>

<template>
  <section class="ventas-layout">
    <div class="ventas-card no-print">
      <div class="ventas-header">
        <div>
          <h1>VENTAS</h1>
          <p v-if="!esRepartidor">Mostrador, domicilio y cola de reparto.</p>
          <p v-else>Pedidos asignados al repartidor.</p>
        </div>
        <div v-if="!esRepartidor" class="ventas-total">
          <span>TOTAL CARRITO</span>
          <strong>${{ total.toFixed(2) }}</strong>
        </div>
      </div>

      <div class="summary-grid" v-if="resumenHoy">
        <div class="summary-card"><span>Ventas del día</span><strong>${{ money(resumenHoy.totalVentas) }}</strong></div>
        <div class="summary-card"><span>Tickets pagados</span><strong>{{ resumenHoy.cantidadVentas }}</strong></div>
        <div class="summary-card"><span>Pedidos activos</span><strong>{{ resumenHoy.cantidadActivas }}</strong></div>
        <div class="summary-card"><span>Canceladas</span><strong>{{ resumenHoy.cantidadCanceladas }}</strong></div>
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
              <div v-if="metodo === 'EFECTIVO'" class="resume-line">
                <span>Cambio</span>
                <strong>${{ cambioCalculado.toFixed(2) }}</strong>
              </div>
              <div class="resume-line"><span>Artículos</span><strong>{{ carritoStore.totalItems.value }}</strong></div>
              <div class="resume-line total-line"><span>Total</span><strong>${{ total.toFixed(2) }}</strong></div>
              <button class="btn-cobrar secondary" :disabled="cargando" @click="generarPedido">{{ cargando ? 'PROCESANDO...' : 'GENERAR PEDIDO' }}</button>
              <button class="btn-cobrar" :disabled="cargando" @click="cobrarCarritoAhora">{{ cargando ? 'PROCESANDO...' : 'COBRAR AHORA' }}</button>
            </div>
          </div>
        </div>

        <aside class="side-panel">
          <div class="panel-box">
            <div class="panel-head">
              <h2>Cola domicilio</h2>
              <span>{{ colaDomicilios.length }}</span>
            </div>
            <div v-if="!colaDomicilios.length" class="historial-empty">Sin pedidos de domicilio.</div>
            <div v-else class="historial-list">
              <div v-for="orden in colaDomicilios" :key="`dom-${orden.idOrden}`" class="active-order">
                <div>
                  <strong>{{ orden.folio }}</strong>
                  <span>{{ new Date(orden.fecha).toLocaleString() }}</span>
                  <span>{{ orden.domicilio?.nombreCliente }}</span>
                  <span>{{ orden.domicilio?.telefono }}</span>
                  <span>{{ orden.domicilio?.direccion }}</span>
                  <span>Entrega: {{ orden.domicilio?.estadoEntrega }}</span>
                  <span>Repartidor: {{ orden.domicilio?.nombreRepartidor || 'Sin asignar' }}</span>
                </div>
                <div class="active-meta">
                  <strong>${{ money(orden.total) }}</strong>
                  <div class="active-actions">
                    <select
                      v-if="puedeAsignarRepartidor"
                      class="mini-select"
                      :value="orden.domicilio?.idRepartidor || ''"
                      @change="asignarRepartidor(orden, $event.target.value)"
                    >
                      <option value="">Sin asignar</option>
                      <option v-for="repartidor in repartidores" :key="repartidor.idUsuario" :value="repartidor.idUsuario">{{ repartidor.nombre }}</option>
                    </select>
                    <button v-if="orden.domicilio && orden.domicilio.estadoEntrega !== 'EN_RUTA'" class="mini-btn" @click="actualizarEstadoDomicilio(orden, 'EN_RUTA')">En ruta</button>
                    <button v-if="orden.domicilio && orden.domicilio.estadoEntrega !== 'ENTREGADO'" class="mini-btn" @click="actualizarEstadoDomicilio(orden, 'ENTREGADO')">Entregado</button>
                    <button v-if="!esRepartidor" class="mini-btn" @click="cobrarPedidoActivo(orden)">Cobrar</button>
                    <button v-if="!esRepartidor" class="mini-btn danger" @click="cancelarPedidoActivo(orden)">Cancelar</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <template v-if="!esRepartidor">
            <div class="panel-box">
              <div class="panel-head">
                <h2>Pedidos activos</h2>
                <span>{{ ordenesActivas.length }}</span>
              </div>
              <div v-if="!ordenesActivas.length" class="historial-empty">Sin pedidos activos.</div>
              <div v-else class="historial-list">
                <div v-for="orden in ordenesActivas" :key="orden.idOrden" class="active-order">
                  <div>
                    <strong>{{ orden.folio }}</strong>
                    <span>{{ orden.tipoOrden }}</span>
                    <span>{{ new Date(orden.fecha).toLocaleString() }}</span>
                    <span>{{ orden.cantidadItems }} artículos</span>
                  </div>
                  <div class="active-meta">
                    <strong>${{ money(orden.total) }}</strong>
                    <div class="active-actions">
                      <button class="mini-btn" @click="cobrarPedidoActivo(orden)">Cobrar</button>
                      <button class="mini-btn danger" @click="cancelarPedidoActivo(orden)">Cancelar</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="panel-box">
              <div class="panel-head"><h2>Ventas del día</h2></div>
              <div v-if="!resumenHoy?.ventasPagadas?.length" class="historial-empty">Sin ventas pagadas hoy.</div>
              <div v-else class="historial-list">
                <button v-for="venta in resumenHoy.ventasPagadas" :key="venta.idOrden" class="historial-item" @click="abrirTicket(venta.idOrden)">
                  <div>
                    <strong>{{ venta.folio }}</strong>
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
              <div v-if="!historialVentas.length" class="historial-empty">Sin ventas previas.</div>
              <div v-else class="historial-list">
                <button v-for="venta in historialVentas" :key="`hist-${venta.idOrden}`" class="historial-item" @click="abrirTicket(venta.idOrden)">
                  <div>
                    <strong>{{ venta.folio }}</strong>
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
            <p>{{ ticketActual.domicilio.estadoEntrega }}</p>
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
          </div>
          <div class="ticket-divider"></div>
          <div class="ticket-totals">
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
          <p>{{ ticketActual.domicilio.estadoEntrega }}</p>
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
        </div>
        <div class="ticket-divider"></div>
        <div class="ticket-totals">
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
.ventas-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:1rem; }
.ventas-header h1 { margin:0; font-family:var(--font-main); font-size:2.8rem; color:var(--rojo-may); }
.ventas-header p { margin:.2rem 0 0; font-weight:bold; }
.ventas-total { text-align:right; }
.ventas-total span { display:block; font-weight:bold; }
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
.items-panel { display:flex; flex-direction:column; gap:.8rem; }
.item-row { display:grid; grid-template-columns:1fr auto auto; gap:1rem; align-items:center; border-bottom:2px solid #f0f0f0; padding-bottom:.8rem; }
.item-info span { display:block; color:#555; font-size:.9rem; }
.mods-inline { margin:.45rem 0 0; padding-left:1rem; color:#444; font-size:.82rem; }
.item-actions { display:flex; align-items:center; gap:.8rem; }
.qty-btn { width:34px; height:34px; border-radius:50%; border:2px solid var(--rojo-may); background:white; color:var(--rojo-may); font-size:1.2rem; font-weight:900; cursor:pointer; }
.qty { min-width:24px; text-align:center; font-weight:bold; }
.item-subtotal { font-weight:bold; }
.checkout-panel,.side-panel { display:flex; flex-direction:column; gap:1rem; }
.field { display:flex; flex-direction:column; gap:.4rem; }
.field span { font-weight:bold; }
.field input,.field select { padding:.8rem 1rem; border:2px solid #000; border-radius:12px; }
.resume-line { display:flex; justify-content:space-between; align-items:center; font-weight:bold; }
.total-line { font-size:1.2rem; }
.btn-cobrar { padding:1rem; border:3px solid #000; border-radius:14px; background:var(--rojo-may); color:white; font-family:var(--font-main); font-size:1.5rem; cursor:pointer; }
.btn-cobrar.secondary { background:#fff1b8; color:#111; }
.btn-cobrar:disabled { opacity:.7; cursor:wait; }
.panel-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem; }
.panel-head h2 { margin:0; }
.historial-list { display:flex; flex-direction:column; gap:.75rem; }
.historial-item,.active-order { width:100%; display:flex; justify-content:space-between; gap:1rem; align-items:center; text-align:left; padding:.9rem; border:2px solid #000; border-radius:14px; background:white; }
.historial-item { cursor:pointer; }
.historial-item span,.active-order span { display:block; font-size:.85rem; color:#555; }
.historial-meta,.active-meta { text-align:right; }
.active-actions { display:flex; flex-wrap:wrap; gap:.5rem; margin-top:.75rem; justify-content:flex-end; }
.btn-ticket,.mini-btn,.mini-select { border:2px solid #000; border-radius:12px; padding:.7rem .9rem; background:white; font-weight:bold; cursor:pointer; }
.mini-btn.danger,.btn-ticket.primary { background:#d32f2f; color:white; }
.mini-select { max-width:180px; }
.ticket-summary { margin-top:1.5rem; padding:1rem; border:3px solid #000; border-radius:20px; background:#fff9e6; }
.ticket-summary h2 { margin:0 0 .5rem; }
.ticket-summary p { margin:.2rem 0; font-weight:bold; }
.ticket-actions,.ticket-modal-actions { display:flex; gap:.75rem; margin-top:1rem; flex-wrap:wrap; }
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
.ticket-totals { display:flex; flex-direction:column; gap:.35rem; }
.print-only { display:none; }
@media (max-width:1280px) {
  .summary-grid { grid-template-columns:repeat(2,1fr); }
  .ventas-grid,.ventas-body,.domicilio-form { grid-template-columns:1fr; }
}
@media print {
  .no-print { display:none !important; }
  .print-only { display:block; }
  .print-area { border:none; box-shadow:none; max-width:100%; width:280px; }
}
</style>
