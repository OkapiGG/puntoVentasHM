<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { apiGet, apiPost } from '../services/api'

const usuarioActual = ref(null)
const sesionActual = ref(null)
const corteSeleccionado = ref(null)
const historialSesiones = ref([])
const resumenPeriodo = ref(null)
const resumenUsuarios = ref([])
const bitacora = ref([])
const cargando = ref(false)
const cargandoHistorial = ref(false)
const error = ref('')
const exito = ref('')
const periodo = ref('DIARIO')

const fondoInicial = ref('200')
const tipoMovimiento = ref('INGRESO_MANUAL')
const montoMovimiento = ref('')
const motivoMovimiento = ref('')
const montoDeclarado = ref('')

const haySesionAbierta = computed(() => sesionActual.value?.estado === 'ABIERTA')
const corteVisible = computed(() => corteSeleccionado.value || sesionActual.value)
const tituloPeriodo = computed(() => ({
  DIARIO: 'Diario',
  SEMANAL: 'Semanal',
  MENSUAL: 'Mensual'
}[periodo.value] || 'Diario'))

const money = (valor) => Number(valor || 0).toFixed(2)
const formatoFecha = (valor) => (valor ? new Date(valor).toLocaleString() : 'Sin registro')
const estadoClase = (estado) => estado === 'ABIERTA' ? 'abierta' : 'cerrada'

const cargarUsuario = () => {
  const guardado = sessionStorage.getItem('usuarioActual')
  usuarioActual.value = guardado ? JSON.parse(guardado) : null
}

const cargarSesionActual = async () => {
  if (!usuarioActual.value?.idUsuario) {
    sesionActual.value = null
    return
  }

  try {
    sesionActual.value = await apiGet(`/caja/usuarios/${usuarioActual.value.idUsuario}/sesion-actual`)
    if (!corteSeleccionado.value && sesionActual.value) {
      corteSeleccionado.value = sesionActual.value
    }
  } catch {
    sesionActual.value = null
  }
}

const cargarHistorial = async () => {
  if (!usuarioActual.value?.idUsuario) {
    historialSesiones.value = []
    return
  }

  cargandoHistorial.value = true
  try {
    historialSesiones.value = await apiGet(`/caja/usuarios/${usuarioActual.value.idUsuario}/historial`)
  } catch {
    historialSesiones.value = []
  } finally {
    cargandoHistorial.value = false
  }
}

const cargarCorte = async (idSesionCaja) => {
  try {
    corteSeleccionado.value = await apiGet(`/caja/sesiones/${idSesionCaja}/corte`)
  } catch (err) {
    error.value = err.message || 'No fue posible cargar el corte'
  }
}

const cargarResumenes = async () => {
  if (!usuarioActual.value?.idNegocio) {
    resumenPeriodo.value = null
    resumenUsuarios.value = []
    bitacora.value = []
    return
  }

  const query = `?periodo=${periodo.value}`
  try {
    const [periodoData, usuariosData, bitacoraData] = await Promise.all([
      apiGet(`/caja/negocios/${usuarioActual.value.idNegocio}/resumen-periodo${query}`),
      apiGet(`/caja/negocios/${usuarioActual.value.idNegocio}/resumen-usuarios${query}`),
      apiGet(`/caja/negocios/${usuarioActual.value.idNegocio}/bitacora${query}`)
    ])
    resumenPeriodo.value = periodoData
    resumenUsuarios.value = usuariosData
    bitacora.value = bitacoraData
  } catch {
    resumenPeriodo.value = null
    resumenUsuarios.value = []
    bitacora.value = []
  }
}

const refrescarCaja = async (idPreferido = null) => {
  await Promise.all([cargarSesionActual(), cargarHistorial(), cargarResumenes()])

  if (idPreferido) {
    await cargarCorte(idPreferido)
    return
  }

  if (sesionActual.value) {
    corteSeleccionado.value = sesionActual.value
    return
  }

  if (historialSesiones.value.length) {
    await cargarCorte(historialSesiones.value[0].idSesionCaja)
    return
  }

  corteSeleccionado.value = null
}

const abrirCaja = async () => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    const respuesta = await apiPost(`/caja/usuarios/${usuarioActual.value.idUsuario}/apertura`, {
      fondoInicial: Number(fondoInicial.value || 0)
    })
    exito.value = 'Caja abierta correctamente'
    await refrescarCaja(respuesta.idSesionCaja)
  } catch (err) {
    error.value = err.message || 'No fue posible abrir la caja'
  } finally {
    cargando.value = false
  }
}

const registrarMovimiento = async () => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    const respuesta = await apiPost(`/caja/usuarios/${usuarioActual.value.idUsuario}/movimientos`, {
      tipo: tipoMovimiento.value,
      monto: Number(montoMovimiento.value || 0),
      motivo: motivoMovimiento.value
    })
    montoMovimiento.value = ''
    motivoMovimiento.value = ''
    exito.value = 'Movimiento registrado'
    await refrescarCaja(respuesta.idSesionCaja)
  } catch (err) {
    error.value = err.message || 'No fue posible registrar el movimiento'
  } finally {
    cargando.value = false
  }
}

const cerrarCaja = async () => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    const respuesta = await apiPost(`/caja/usuarios/${usuarioActual.value.idUsuario}/cierre`, {
      montoDeclarado: montoDeclarado.value === '' ? null : Number(montoDeclarado.value)
    })
    montoDeclarado.value = ''
    exito.value = 'Caja cerrada correctamente'
    await refrescarCaja(respuesta.idSesionCaja)
  } catch (err) {
    error.value = err.message || 'No fue posible cerrar la caja'
  } finally {
    cargando.value = false
  }
}

const reimprimirCorte = () => {
  if (!corteVisible.value) return
  window.print()
}

watch(periodo, async () => {
  await cargarResumenes()
})

onMounted(async () => {
  cargarUsuario()
  await refrescarCaja()
})
</script>

<template>
  <section class="caja-layout">
    <div class="caja-card">
      <div class="caja-header">
        <div>
          <h1>CAJA</h1>
          <p>Corte por sesión, consolidado por periodo y bitácora formal.</p>
        </div>
        <div class="header-actions">
          <label class="periodo-select">
            <span>Periodo</span>
            <select v-model="periodo">
              <option value="DIARIO">Diario</option>
              <option value="SEMANAL">Semanal</option>
              <option value="MENSUAL">Mensual</option>
            </select>
          </label>
          <div v-if="sesionActual" class="estado-badge" :class="estadoClase(sesionActual.estado)">
            {{ sesionActual.estado }}
          </div>
        </div>
      </div>

      <p v-if="error" class="status error">{{ error }}</p>
      <p v-if="exito" class="status success">{{ exito }}</p>

      <div v-if="resumenPeriodo" class="summary-grid">
        <div class="summary-card"><span>Corte {{ tituloPeriodo }}</span><strong>{{ resumenPeriodo.sesiones }}</strong></div>
        <div class="summary-card"><span>Ventas cobradas</span><strong>${{ money(resumenPeriodo.totalVentas) }}</strong></div>
        <div class="summary-card"><span>Efectivo en caja</span><strong>${{ money(resumenPeriodo.efectivoEnCaja) }}</strong></div>
        <div class="summary-card"><span>Retiros seguridad</span><strong>${{ money(resumenPeriodo.retirosSeguridad) }}</strong></div>
      </div>

      <div class="caja-grid">
        <div class="panel">
          <div class="panel-head">
            <h2>Corte seleccionado</h2>
            <div class="panel-head-actions">
              <span v-if="corteVisible">Sesión #{{ corteVisible.idSesionCaja }}</span>
              <button v-if="corteVisible" class="mini-btn" @click="reimprimirCorte">Reimprimir corte</button>
            </div>
          </div>

          <div v-if="!corteVisible" class="empty-state">
            No hay sesiones registradas todavía.
          </div>

          <div v-else class="resumen">
            <div class="resumen-line"><span>Cajero</span><strong>{{ corteVisible.nombreUsuario }}</strong></div>
            <div class="resumen-line"><span>Estado</span><strong>{{ corteVisible.estado }}</strong></div>
            <div class="resumen-line"><span>Fondo inicial</span><strong>${{ money(corteVisible.fondoInicial) }}</strong></div>
            <div class="resumen-line"><span>Apertura</span><strong>{{ formatoFecha(corteVisible.apertura) }}</strong></div>
            <div v-if="corteVisible.cierre" class="resumen-line"><span>Cierre</span><strong>{{ formatoFecha(corteVisible.cierre) }}</strong></div>
            <div class="resumen-line"><span>Ventas cobradas</span><strong>{{ corteVisible.cantidadVentas }}</strong></div>
            <div class="resumen-line"><span>Cancelaciones</span><strong>{{ corteVisible.cantidadCancelaciones }}</strong></div>
            <div class="resumen-line"><span>Movimientos</span><strong>{{ corteVisible.cantidadMovimientos }}</strong></div>
            <div class="resumen-line"><span>Total ventas</span><strong>${{ money(corteVisible.totalVentas) }}</strong></div>
            <div class="resumen-line"><span>Ventas efectivo</span><strong>${{ money(corteVisible.totalVentasEfectivo) }}</strong></div>
            <div class="resumen-line"><span>Ventas tarjeta</span><strong>${{ money(corteVisible.totalVentasTarjeta) }}</strong></div>
            <div class="resumen-line"><span>Ingresos manuales</span><strong>${{ money(corteVisible.totalIngresosManuales) }}</strong></div>
            <div class="resumen-line"><span>Retiros totales</span><strong>${{ money(corteVisible.totalRetiros) }}</strong></div>
            <div class="resumen-line"><span>Retiros seguridad</span><strong>${{ money(corteVisible.totalRetirosSeguridad) }}</strong></div>
            <div class="resumen-line total"><span>Efectivo en caja</span><strong>${{ money(corteVisible.efectivoEnCaja) }}</strong></div>
            <div class="resumen-line"><span>Saldo operativo</span><strong>${{ money(corteVisible.saldoEsperado) }}</strong></div>
            <div v-if="corteVisible.montoDeclarado !== null" class="resumen-line"><span>Monto declarado</span><strong>${{ money(corteVisible.montoDeclarado) }}</strong></div>
            <div
              v-if="corteVisible.diferencia !== null"
              class="resumen-line"
              :class="Number(corteVisible.diferencia) === 0 ? 'ok' : 'warn'"
            >
              <span>Diferencia</span><strong>${{ money(corteVisible.diferencia) }}</strong>
            </div>
          </div>
        </div>

        <div class="panel">
          <h2 v-if="!haySesionAbierta">Abrir caja</h2>
          <h2 v-else>Operación de caja</h2>

          <div v-if="!haySesionAbierta" class="form-stack">
            <label class="field">
              <span>Fondo inicial</span>
              <input v-model="fondoInicial" type="number" min="0" step="0.01" />
            </label>
            <button class="action-btn primary" :disabled="cargando" @click="abrirCaja">
              {{ cargando ? 'ABRIENDO...' : 'ABRIR CAJA' }}
            </button>
          </div>

          <div v-else class="form-stack">
            <label class="field">
              <span>Tipo de movimiento</span>
              <select v-model="tipoMovimiento">
                <option value="INGRESO_MANUAL">INGRESO MANUAL</option>
                <option value="RETIRO">RETIRO</option>
              </select>
            </label>

            <label class="field">
              <span>Monto</span>
              <input v-model="montoMovimiento" type="number" min="0" step="0.01" />
            </label>

            <label class="field">
              <span>Motivo</span>
              <input v-model="motivoMovimiento" type="text" placeholder="Ej. cambio, compra, retiro" />
            </label>

            <button class="action-btn" :disabled="cargando" @click="registrarMovimiento">
              {{ cargando ? 'GUARDANDO...' : 'REGISTRAR MOVIMIENTO' }}
            </button>

            <div class="divider"></div>

            <label class="field">
              <span>Monto declarado al cierre</span>
              <input v-model="montoDeclarado" type="number" min="0" step="0.01" placeholder="Opcional" />
            </label>

            <button class="action-btn danger" :disabled="cargando" @click="cerrarCaja">
              {{ cargando ? 'CERRANDO...' : 'CERRAR CAJA' }}
            </button>
          </div>
        </div>
      </div>

      <div class="caja-grid secondary">
        <div class="panel">
          <div class="panel-head">
            <h2>Resumen por cajero</h2>
            <span>{{ tituloPeriodo }}</span>
          </div>
          <div v-if="!resumenUsuarios.length" class="empty-state">Sin datos en este periodo.</div>
          <div v-else class="table-list">
            <div v-for="usuario in resumenUsuarios" :key="usuario.idUsuario" class="table-item">
              <div>
                <strong>{{ usuario.nombreUsuario }}</strong>
                <span>{{ usuario.sesiones }} sesiones / {{ usuario.cierres }} cierres</span>
                <span>{{ usuario.ventasCobradas }} ventas / {{ usuario.cancelaciones }} cancelaciones</span>
              </div>
              <div class="historial-meta">
                <span>Efectivo: ${{ money(usuario.efectivoEnCaja) }}</span>
                <strong>${{ money(usuario.totalVentas) }}</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">
            <h2>Historial de sesiones</h2>
            <span v-if="cargandoHistorial">Cargando...</span>
          </div>

          <div v-if="!historialSesiones.length" class="empty-state">
            Sin sesiones anteriores.
          </div>

          <div v-else class="historial-list">
            <button
              v-for="sesion in historialSesiones"
              :key="sesion.idSesionCaja"
              class="historial-item"
              :class="{ selected: corteVisible?.idSesionCaja === sesion.idSesionCaja }"
              @click="cargarCorte(sesion.idSesionCaja)"
            >
              <div>
                <strong>Sesión #{{ sesion.idSesionCaja }}</strong>
                <span>{{ sesion.nombreUsuario }}</span>
                <span>{{ formatoFecha(sesion.apertura) }}</span>
                <span v-if="sesion.cierre">{{ formatoFecha(sesion.cierre) }}</span>
              </div>
              <div class="historial-meta">
                <span>{{ sesion.estado }}</span>
                <strong>${{ money(sesion.efectivoEnCaja) }}</strong>
              </div>
            </button>
          </div>
        </div>
      </div>

      <div class="caja-grid secondary">
        <div v-if="corteVisible?.movimientos?.length" class="panel">
          <h2>Movimientos de la sesión</h2>
          <div class="movimientos-list">
            <div v-for="movimiento in corteVisible.movimientos" :key="movimiento.idMovimientoCaja" class="movimiento-item">
              <div>
                <strong>{{ movimiento.tipo }}</strong>
                <p>{{ movimiento.motivo || 'Sin motivo' }}</p>
                <span>{{ formatoFecha(movimiento.fecha) }}</span>
              </div>
              <strong>${{ money(movimiento.monto) }}</strong>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">
            <h2>Bitácora formal</h2>
            <span>{{ tituloPeriodo }}</span>
          </div>
          <div v-if="!bitacora.length" class="empty-state">Sin retiros ni cancelaciones en este periodo.</div>
          <div v-else class="movimientos-list">
            <div v-for="evento in bitacora" :key="`${evento.tipoEvento}-${evento.referenciaId}-${evento.fecha}`" class="movimiento-item">
              <div>
                <strong>{{ evento.tipoEvento }}</strong>
                <p>{{ evento.nombreUsuario }} · {{ evento.referencia }}</p>
                <span>{{ formatoFecha(evento.fecha) }}</span>
                <span>{{ evento.motivo || 'Sin motivo' }}</span>
              </div>
              <strong>${{ money(evento.monto) }}</strong>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="corteVisible" class="print-only">
      <div class="ticket-paper">
        <h2>CORTE DE CAJA</h2>
        <p>Sesión #{{ corteVisible.idSesionCaja }}</p>
        <p>Cajero: {{ corteVisible.nombreUsuario }}</p>
        <p>Estado: {{ corteVisible.estado }}</p>
        <p>Apertura: {{ formatoFecha(corteVisible.apertura) }}</p>
        <p v-if="corteVisible.cierre">Cierre: {{ formatoFecha(corteVisible.cierre) }}</p>
        <div class="ticket-divider"></div>
        <p>Fondo inicial: ${{ money(corteVisible.fondoInicial) }}</p>
        <p>Ventas totales: ${{ money(corteVisible.totalVentas) }}</p>
        <p>Ventas efectivo: ${{ money(corteVisible.totalVentasEfectivo) }}</p>
        <p>Ventas tarjeta: ${{ money(corteVisible.totalVentasTarjeta) }}</p>
        <p>Ingresos manuales: ${{ money(corteVisible.totalIngresosManuales) }}</p>
        <p>Retiros: ${{ money(corteVisible.totalRetiros) }}</p>
        <p>Retiros seguridad: ${{ money(corteVisible.totalRetirosSeguridad) }}</p>
        <p>Efectivo en caja: ${{ money(corteVisible.efectivoEnCaja) }}</p>
        <p>Saldo operativo: ${{ money(corteVisible.saldoEsperado) }}</p>
        <p v-if="corteVisible.montoDeclarado !== null">Monto declarado: ${{ money(corteVisible.montoDeclarado) }}</p>
        <p v-if="corteVisible.diferencia !== null">Diferencia: ${{ money(corteVisible.diferencia) }}</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.caja-layout { width: 100%; display: flex; justify-content: center; }
.caja-card { width: min(1380px, 100%); background: #fff; border: 4px solid #000; border-radius: 24px; padding: 24px; box-shadow: 10px 10px 0 rgba(0, 0, 0, 0.12); }
.caja-header { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 18px; }
.caja-header h1, .panel h2 { margin: 0; font-family: var(--font-main); color: var(--rojo-may); }
.caja-header p { margin: 6px 0 0; color: #4b5563; }
.header-actions { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.periodo-select { display: flex; flex-direction: column; gap: 6px; font-weight: 700; }
.periodo-select select { border: 2px solid #000; border-radius: 12px; padding: 10px 14px; background: #fff; }
.estado-badge { min-width: 120px; text-align: center; padding: 10px 14px; border: 3px solid #000; border-radius: 12px; font-weight: 800; }
.estado-badge.abierta { background: #dcfce7; }
.estado-badge.cerrada { background: #e5e7eb; }
.status { margin: 0 0 16px; padding: 12px 14px; border-radius: 12px; font-weight: 700; }
.status.error { background: #fee2e2; color: #991b1b; }
.status.success { background: #dcfce7; color: #166534; }
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 18px; }
.summary-card { border: 3px solid #000; border-radius: 18px; padding: 14px; background: #fff7d6; }
.summary-card span { display: block; font-weight: 700; }
.summary-card strong { font-size: 1.35rem; }
.caja-grid { display: grid; grid-template-columns: 1.15fr .85fr; gap: 16px; margin-bottom: 16px; }
.caja-grid.secondary { grid-template-columns: 1fr 1fr; }
.panel { border: 3px solid #000; border-radius: 20px; padding: 18px; background: #fff; min-width: 0; }
.panel-head { display: flex; justify-content: space-between; gap: 12px; align-items: center; margin-bottom: 14px; }
.panel-head-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.empty-state { padding: 28px; border: 2px dashed #9ca3af; border-radius: 16px; text-align: center; font-weight: 700; color: #4b5563; }
.resumen, .form-stack, .movimientos-list, .historial-list, .table-list { display: flex; flex-direction: column; gap: 10px; }
.resumen-line { display: flex; justify-content: space-between; gap: 12px; align-items: center; padding-bottom: 8px; border-bottom: 1px solid #e5e7eb; }
.resumen-line.total strong, .resumen-line.total span { font-size: 1.05rem; font-weight: 900; }
.resumen-line.ok strong { color: #166534; }
.resumen-line.warn strong { color: #b45309; }
.field { display: flex; flex-direction: column; gap: 6px; font-weight: 700; }
.field input, .field select { border: 2px solid #000; border-radius: 12px; padding: 12px 14px; }
.action-btn, .mini-btn { border: 3px solid #000; border-radius: 14px; padding: 12px 16px; background: #fff; font-weight: 900; cursor: pointer; }
.action-btn.primary { background: #fde68a; }
.action-btn.danger { background: #fecaca; }
.divider { height: 2px; background: #e5e7eb; margin: 6px 0; }
.historial-item, .movimiento-item, .table-item { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; border: 2px solid #000; border-radius: 14px; padding: 12px; background: #fff; text-align: left; }
.historial-item { cursor: pointer; }
.historial-item.selected { background: #fff7d6; }
.historial-item span, .movimiento-item p, .movimiento-item span, .table-item span { display: block; margin-top: 3px; color: #4b5563; }
.historial-meta { text-align: right; min-width: 160px; }
.ticket-paper { width: 280px; margin: 0 auto; border: 2px dashed #000; padding: 16px; font-family: monospace; background: #fff; }
.ticket-paper h2 { margin: 0 0 8px; text-align: center; }
.ticket-paper p { margin: 4px 0; }
.ticket-divider { border-top: 1px dashed #000; margin: 10px 0; }
.print-only { display: none; }
@media (max-width: 1180px) {
  .summary-grid, .caja-grid, .caja-grid.secondary { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 820px) {
  .summary-grid, .caja-grid, .caja-grid.secondary { grid-template-columns: 1fr; }
  .caja-header, .panel-head, .historial-item, .movimiento-item, .table-item { flex-direction: column; }
  .historial-meta { min-width: 0; text-align: left; }
}
@media print {
  .caja-card { box-shadow: none; border: none; }
  .caja-card > :not(.print-only) { display: none !important; }
  .print-only { display: block; }
}
</style>
