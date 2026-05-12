<script setup>
import { computed, onMounted, ref } from 'vue'
import { apiGet, apiPost } from '../services/api'

const usuarioActual = ref(null)
const sesionActual = ref(null)
const corteSeleccionado = ref(null)
const historialSesiones = ref([])
const cargando = ref(false)
const cargandoHistorial = ref(false)
const error = ref('')
const exito = ref('')

const fondoInicial = ref('200')
const tipoMovimiento = ref('INGRESO_MANUAL')
const montoMovimiento = ref('')
const motivoMovimiento = ref('')
const montoDeclarado = ref('')

const haySesionAbierta = computed(() => sesionActual.value?.estado === 'ABIERTA')
const corteVisible = computed(() => corteSeleccionado.value || sesionActual.value)

const money = (valor) => Number(valor || 0).toFixed(2)

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

const refrescarCaja = async (idPreferido = null) => {
  await cargarSesionActual()
  await cargarHistorial()

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
          <p>Apertura, movimientos, cierre y corte por sesión.</p>
        </div>
        <div v-if="sesionActual" class="estado-badge" :class="sesionActual.estado === 'ABIERTA' ? 'abierta' : 'cerrada'">
          {{ sesionActual.estado }}
        </div>
      </div>

      <p v-if="error" class="status error">{{ error }}</p>
      <p v-if="exito" class="status success">{{ exito }}</p>

      <div class="caja-grid">
        <div class="panel">
          <div class="panel-head">
            <h2>Corte</h2>
            <span v-if="corteVisible">Sesión #{{ corteVisible.idSesionCaja }}</span>
          </div>

          <div v-if="!corteVisible" class="empty-state">
            No hay sesiones registradas todavía.
          </div>

          <div v-else class="resumen">
            <div class="resumen-line"><span>Estado</span><strong>{{ corteVisible.estado }}</strong></div>
            <div class="resumen-line"><span>Fondo inicial</span><strong>${{ money(corteVisible.fondoInicial) }}</strong></div>
            <div class="resumen-line"><span>Apertura</span><strong>{{ new Date(corteVisible.apertura).toLocaleString() }}</strong></div>
            <div v-if="corteVisible.cierre" class="resumen-line"><span>Cierre</span><strong>{{ new Date(corteVisible.cierre).toLocaleString() }}</strong></div>
            <div class="resumen-line"><span>Ventas cobradas</span><strong>{{ corteVisible.cantidadVentas }}</strong></div>
            <div class="resumen-line"><span>Movimientos</span><strong>{{ corteVisible.cantidadMovimientos }}</strong></div>
            <div class="resumen-line"><span>Total ventas</span><strong>${{ money(corteVisible.totalVentas) }}</strong></div>
            <div class="resumen-line"><span>Ingresos manuales</span><strong>${{ money(corteVisible.totalIngresosManuales) }}</strong></div>
            <div class="resumen-line"><span>Retiros</span><strong>${{ money(corteVisible.totalRetiros) }}</strong></div>
            <div class="resumen-line total"><span>Saldo esperado</span><strong>${{ money(corteVisible.saldoEsperado) }}</strong></div>
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
                <span>{{ new Date(sesion.apertura).toLocaleString() }}</span>
                <span v-if="sesion.cierre">{{ new Date(sesion.cierre).toLocaleString() }}</span>
              </div>
              <div class="historial-meta">
                <span>{{ sesion.estado }}</span>
                <strong>${{ money(sesion.saldoEsperado) }}</strong>
              </div>
            </button>
          </div>
        </div>

        <div v-if="corteVisible?.movimientos?.length" class="panel movimientos-panel">
          <h2>Movimientos de la sesión</h2>
          <div class="movimientos-list">
            <div v-for="movimiento in corteVisible.movimientos" :key="movimiento.idMovimientoCaja" class="movimiento-item">
              <div>
                <strong>{{ movimiento.tipo }}</strong>
                <p>{{ movimiento.motivo || 'Sin motivo' }}</p>
              </div>
              <strong>${{ money(movimiento.monto) }}</strong>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.caja-layout {
  width: 100%;
  display: flex;
  justify-content: center;
}

.caja-card {
  width: min(1180px, 100%);
  background: #fff;
  border: 4px solid #000;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 10px 10px 0 rgba(0, 0, 0, 0.12);
}

.caja-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.caja-header h1,
.panel h2 {
  margin: 0;
  font-family: var(--font-main);
  color: var(--rojo-may);
}

.caja-header p {
  margin: 6px 0 0;
  color: #4b5563;
}

.estado-badge {
  min-width: 120px;
  text-align: center;
  padding: 10px 14px;
  border: 3px solid #000;
  border-radius: 12px;
  font-weight: 800;
}

.estado-badge.abierta {
  background: #dcfce7;
}

.estado-badge.cerrada {
  background: #e5e7eb;
}

.status {
  margin: 0 0 16px;
  padding: 12px 14px;
  border-radius: 12px;
  font-weight: 700;
}

.status.error {
  background: #fee2e2;
  color: #991b1b;
}

.status.success {
  background: #dcfce7;
  color: #166534;
}

.caja-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
}

.caja-grid.secondary {
  margin-top: 18px;
}

.panel {
  border: 3px solid #000;
  border-radius: 18px;
  padding: 18px;
  background: #fffdf7;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.resumen,
.form-stack,
.historial-list,
.movimientos-list {
  display: grid;
  gap: 12px;
}

.resumen-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px dashed #d1d5db;
}

.resumen-line.total strong,
.resumen-line.total span {
  color: var(--rojo-may);
}

.resumen-line.ok strong {
  color: #166534;
}

.resumen-line.warn strong {
  color: #b45309;
}

.field {
  display: grid;
  gap: 6px;
}

.field span {
  font-weight: 700;
}

.field input,
.field select {
  width: 100%;
  padding: 12px 14px;
  border: 2px solid #111827;
  border-radius: 12px;
  background: #fff;
}

.action-btn {
  border: 3px solid #000;
  border-radius: 12px;
  padding: 14px;
  font-family: var(--font-main);
  font-size: 1.4rem;
  background: var(--amarillo-may);
  cursor: pointer;
}

.action-btn.primary {
  background: var(--rojo-may);
  color: #fff;
}

.action-btn.danger {
  background: #111827;
  color: #fff;
}

.action-btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

.divider {
  border-top: 2px dashed #d1d5db;
  margin: 4px 0;
}

.historial-item,
.movimiento-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  border: 2px solid #000;
  border-radius: 14px;
  padding: 12px 14px;
  background: #fff;
}

.historial-item {
  text-align: left;
  cursor: pointer;
}

.historial-item.selected {
  background: #fff1b8;
}

.historial-item span,
.movimiento-item p {
  display: block;
  margin: 4px 0 0;
  color: #4b5563;
}

.historial-meta {
  text-align: right;
}

.empty-state {
  padding: 18px;
  border: 2px dashed #d1d5db;
  border-radius: 14px;
  color: #6b7280;
}

@media (max-width: 980px) {
  .caja-grid {
    grid-template-columns: 1fr;
  }
}
</style>
