<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import UserDropdown from './UserDropdown.vue'
// import { useRouter } from 'vue-router' // Descomenta si usas router

const dropdownOpen = ref(false)
// const router = useRouter() // Descomenta si usas router

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value
}

// Función para cerrar sesión
const handleLogout = () => {
  console.log("Cerrando sesión para:", nombreUsuario.value)
  
  // 1. Limpiar datos (ejemplo)
  // localStorage.removeItem('token')
  // userStore.logout() 

  // 2. Cerrar el menú
  dropdownOpen.value = false

}

const closeDropdown = (e) => {
  if (!e.target.closest('.user-menu-container')) {
    dropdownOpen.value = false
  }
}

// Reloj y Estado
const horaActual = ref('')
const nombreUsuario = ref('GUILLERMO CAMARENA')
let intervalo = null

const actualizarHora = () => {
  const ahora = new Date()
  horaActual.value = ahora.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true
  })
}

onMounted(() => {
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
    <!-- BLOQUE IZQUIERDO: Menú + Títulos -->
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
    
    <!-- CENTRO: Logo Hexagonal (El banderín) -->
    <div class="nav-center">
      <div class="logo-hexagon">
        <img src="@/assets/images/Logomay.png" alt="HM" />
      </div>
    </div>

    <!-- DERECHA: Usuario y Hora (Restaurado) -->
    <!-- DERECHA: Usuario y Hora -->
    <div class="nav-right">
    <div class="user-meta">
        <h3>{{ nombreUsuario }}</h3>
        <div class="clock-display">{{ horaActual }}</div>
    </div>
    
    <!-- CONTENEDOR RELATIVO CLAVE -->
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
  /* Aumentamos la altura */
  height: 115px; 
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  color: white;
  position: relative;
  border-bottom: 8px solid var(--amarillo-may); /* Un poco más grueso el borde */
  box-sizing: border-box;
}

/* Alineación Izquierda: Rayas y Texto */
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

/* Logo Central */
.logo-hexagon {
  background-color: var(--amarillo-may);
  width: 150px;
  height: 155px; /* Más alto para que sobresalga bien */
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  clip-path: polygon(0% 0%, 100% 0%, 100% 80%, 50% 100%, 0% 80%);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.logo-hexagon img {
  width: 120px;
  margin-bottom: 15px;
}

/* Lado Derecho: Usuario y Reloj */
.nav-right {
  display: flex;
  align-items: center;
  gap: 60px;
  padding-right: 40px; /* <--- Aumenta este valor para alejarlo del borde derecho */
}

.user-meta {
  text-align: right;
  margin-right: 10px; /* Espacio extra entre el texto y el círculo */
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

.user-avatar-circle {
  width: 90px;
  height: 90px;
  background-color: #E6E6E6;
  border-radius: 50%;
  border: 4px solid #000;
  cursor: pointer;
  position: relative; 
  /* Este z-index es vital para que el círculo oculte el inicio del dropdown */
  z-index: 100; 
  box-shadow: 0 4px 0px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.user-section {
  position: relative; /* Clave para el centrado */
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-menu-container {
  position: relative; 
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 110; /* Asegura que todo este grupo esté sobre la franja amarilla */
}

.user-avatar-circle:hover {
  transform: scale(1.05);
}
</style>