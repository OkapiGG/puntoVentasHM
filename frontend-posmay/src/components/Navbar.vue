<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import UserDropdown from './UserDropdown.vue'

const dropdownOpen = ref(false)
const horaActual = ref('')
const nombreUsuario = ref('USUARIO')
const rolUsuario = ref('')
let intervalo = null

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value
}

const handleLogout = () => {
  dropdownOpen.value = false
}

const closeDropdown = (e) => {
  if (!e.target.closest('.user-menu-container')) {
    dropdownOpen.value = false
  }
}

const actualizarHora = () => {
  const ahora = new Date()
  horaActual.value = ahora.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true
  })
}

onMounted(() => {
  const usuarioGuardado = sessionStorage.getItem('usuarioActual')
  if (usuarioGuardado) {
    const usuarioActual = JSON.parse(usuarioGuardado)
    nombreUsuario.value = usuarioActual.nombre || 'USUARIO'
    rolUsuario.value = usuarioActual.rol || ''
  }

  actualizarHora()
  intervalo = setInterval(actualizarHora, 1000)
  window.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  if (intervalo) clearInterval(intervalo)
  window.removeEventListener('click', closeDropdown)
})
</script>

<template>
  <header class="navbar">
    <div class="nav-left">
      <button class="menu-trigger" @click="$emit('toggle-menu')">
        <div class="hamburger-icon">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </button>

      <div class="brand-titles">
        <h2>HABURGUESAS MAY</h2>
        <p>SUCURSAL PERIFERICO</p>
      </div>
    </div>

    <div class="nav-center">
      <div class="logo-hexagon">
        <img src="@/assets/images/Logomay.png" alt="HM" />
      </div>
    </div>

    <div class="nav-right">
      <div class="user-meta">
        <h3>{{ nombreUsuario }}</h3>
        <div class="role-display">{{ rolUsuario }}</div>
        <div class="clock-display">{{ horaActual }}</div>
      </div>

      <div class="user-menu-container">
        <button class="user-avatar-circle" @click="toggleDropdown"></button>

        <UserDropdown
          :isOpen="dropdownOpen"
          :username="nombreUsuario"
          @logout="handleLogout"
        />
      </div>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  background-color: var(--rojo-may);
  height: 115px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  color: white;
  position: relative;
  border-bottom: 8px solid var(--amarillo-may);
  box-sizing: border-box;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.menu-trigger {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
}

.hamburger-icon {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.hamburger-icon span {
  display: block;
  width: 35px;
  height: 5px;
  background-color: var(--amarillo-may);
  border-radius: 2px;
}

.brand-titles h2 {
  font-family: var(--font-main);
  font-size: 2.5rem;
  margin: 0;
  line-height: 1;
  letter-spacing: 1.5px;
}

.brand-titles p {
  font-size: 1rem;
  margin: 0;
  font-weight: bold;
  font-style: italic;
  text-transform: uppercase;
}

.logo-hexagon {
  background-color: var(--amarillo-may);
  width: 150px;
  height: 155px;
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  clip-path: polygon(0% 0%, 100% 0%, 100% 80%, 50% 100%, 0% 80%);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.logo-hexagon img {
  width: 120px;
  margin-bottom: 15px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 60px;
  padding-right: 40px;
}

.user-meta {
  text-align: right;
  margin-right: 10px;
}

.user-meta h3 {
  font-family: var(--font-main);
  font-size: 2.5rem;
  margin: 0;
  line-height: 1;
  letter-spacing: 1.5px;
}

.clock-display {
  font-weight: bold;
  font-size: 0.95rem;
}

.role-display {
  font-weight: 800;
  font-size: 0.85rem;
  text-transform: uppercase;
}

.user-avatar-circle {
  width: 90px;
  height: 90px;
  background-color: #e6e6e6;
  border-radius: 50%;
  border: 4px solid #000;
  cursor: pointer;
  position: relative;
  z-index: 100;
  box-shadow: 0 4px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.user-menu-container {
  position: relative;
}
</style>
