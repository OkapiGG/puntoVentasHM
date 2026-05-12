<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { limpiarCarrito } from '../store/carrito'
import { ROLES, tieneAlgunRol } from '../utils/permisos'

defineProps({
  isOpen: Boolean
})

const emit = defineEmits(['close'])
const router = useRouter()
const puedeVerMenu = computed(() => tieneAlgunRol([ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO, ROLES.REPARTIDOR]))
const puedeVerVentas = computed(() => tieneAlgunRol([ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO, ROLES.REPARTIDOR]))
const puedeOperarCaja = computed(() => tieneAlgunRol([ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO]))
const puedeVerAdmin = computed(() => tieneAlgunRol([ROLES.ADMIN, ROLES.GERENTE]))

const navegar = (ruta) => {
  router.push(ruta)
  emit('close')
}

const logout = () => {
  sessionStorage.removeItem('usuarioActual')
  limpiarCarrito()
  router.push('/')
  emit('close')
}
</script>

<template>
  <div v-if="isOpen" class="sidebar-overlay" @click="$emit('close')"></div>

  <aside class="sidebar" :class="{ 'is-open': isOpen }">
    <div class="sidebar-header">
      <div class="hamburger-icon">
        <span></span><span></span><span></span>
      </div>
      <div class="brand-titles">
        <h2>HABURGUESAS MAY</h2>
        <p>SUCURSAL PERIFERICO</p>
      </div>
    </div>

    <hr class="divider-line" />

    <nav class="sidebar-nav">
      <button class="nav-btn" @click="navegar('/dashboard')">PRINCIPAL</button>
      <button v-if="puedeVerMenu" class="nav-btn" @click="navegar('/dashboard/menu')">MENU</button>
      <button v-if="puedeVerVentas" class="nav-btn" @click="navegar('/dashboard/ventas')">VENTAS</button>
      <button v-if="puedeOperarCaja" class="nav-btn" @click="navegar('/dashboard/caja')">CAJA</button>
      <button v-if="puedeVerAdmin" class="nav-btn" @click="navegar('/dashboard/estadisticas')">ESTADISTICAS</button>
      <button v-if="puedeVerAdmin" class="nav-btn" @click="navegar('/dashboard/inventario')">INVENTARIO</button>

      <div class="dashed-divider">-----------------------</div>

      <button class="nav-btn gray locked">
        SUCURSALES <span class="lock-icon">🔒</span>
      </button>
      <button class="nav-btn gray locked">
        EMPLEADOS <span class="lock-icon">🔒</span>
      </button>

      <button class="nav-btn logout" @click="logout">CERRAR SESIÓN</button>
    </nav>

    <footer class="sidebar-footer">
      ©TheTokens - UNACH
    </footer>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: -320px;
  width: 320px;
  height: 100vh;
  background-color: var(--rojo-may);
  z-index: 1000;
  transition: left 0.3s ease;
  display: flex;
  flex-direction: column;
  border-right: 4px solid black;
}

.sidebar.is-open {
  left: 0;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  z-index: 999;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  color: white;
}

.hamburger-icon span {
  display: block;
  width: 30px;
  height: 5px;
  background-color: var(--amarillo-may);
  margin-bottom: 4px;
  border-radius: 2px;
}

.brand-titles h2 {
  font-family: var(--font-main);
  font-size: 1.8rem;
  margin: 0;
  line-height: 0.9;
}

.brand-titles p {
  font-size: 0.8rem;
  font-style: italic;
  margin: 0;
  font-weight: bold;
}

.divider-line {
  border: 0;
  border-top: 5px solid var(--amarillo-may);
  margin: 0 20px;
}

.sidebar-nav {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-grow: 1;
}

.nav-btn {
  width: 100%;
  padding: 12px;
  font-family: var(--font-main);
  font-size: 1.4rem;
  border: 3px solid black;
  border-radius: 10px;
  cursor: pointer;
  text-align: center;
  box-shadow: 0 4px 0 black;
  transition: background-color 0.2s, transform 0.1s;
  background-color: white;
  color: black;
}

.nav-btn:active {
  transform: translateY(3px);
  box-shadow: 0 1px 0 black;
}

.nav-btn:hover:not(.locked) {
  background-color: var(--amarillo-may);
  transform: translateY(-2px);
  box-shadow: 0 6px 0 black;
}

.gray.locked {
  background-color: #bdbdbd;
  color: #424242;
  cursor: not-allowed;
}

.locked {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.dashed-divider {
  color: var(--amarillo-may);
  text-align: center;
  font-weight: bold;
  letter-spacing: 2px;
  margin: 10px 0;
}

.sidebar-footer {
  padding: 15px;
  color: white;
  text-align: center;
  font-size: 0.8rem;
  font-weight: bold;
}
</style>
