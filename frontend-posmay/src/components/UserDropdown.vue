<template>
  <Transition name="drop">
    <div v-if="isOpen" class="dropdown-wrapper">
      <div class="user-card">
        <h3 class="full-name">{{ username }}</h3>

        <div class="menu-items">
          <button class="arrow-btn">DATOS PERSONALES</button>
          <button v-if="puedeVerReportes" class="arrow-btn">REPORTE DE VENTA</button>
          <button v-if="puedeVerCortes" class="arrow-btn">CORTE X</button>
          <button v-if="puedeVerCortes" class="arrow-btn">CORTE Y</button>
        </div>

        <button class="logout-btn" @click="logout">CERRAR SESIÓN</button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { limpiarCarrito } from '../store/carrito'
import { ROLES, tieneAlgunRol } from '../utils/permisos'

defineProps({
  isOpen: Boolean,
  username: String
})

const emit = defineEmits(['logout'])
const router = useRouter()
const puedeVerCortes = computed(() => tieneAlgunRol([ROLES.ADMIN, ROLES.GERENTE, ROLES.CAJERO]))
const puedeVerReportes = computed(() => tieneAlgunRol([ROLES.ADMIN, ROLES.GERENTE]))

const logout = () => {
  sessionStorage.removeItem('usuarioActual')
  limpiarCarrito()
  router.push('/')
  emit('logout')
}
</script>

<style scoped>
/* Tu CSS se mantiene igual, asegúrate de tener el mask-image que configuramos antes */
.dropdown-wrapper {
  position: absolute;
  top: 45px; 
  left: 50%;
  transform: translateX(-50%);
  width: 220px; 
  z-index: 50;
}

.user-card {
  width: 100%;
  background-color: var(--rojo-may, #e31b1b);
  border-left: 8px solid #ffeb3b;
  border-right: 8px solid #ffeb3b;
  border-bottom: 6px solid #ffeb3b;
  border-top: none; 
  border-radius: 0 0 25px 25px; 
  padding: 50px 20px 10px 20px; 
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0px 10px 20px rgba(0,0,0,0.3);
  box-sizing: border-box;

  /* Máscara para la transparencia superior */
  -webkit-mask-image: linear-gradient(to bottom, transparent 0%, transparent 0%, black 25%, black 100%);
  mask-image: linear-gradient(to bottom, transparent 0%, transparent 12.85%, black 0%, black 100%);
}

.full-name {
  color: #000;
  font-family: 'Arial Black', sans-serif;
  font-size: 1.1rem;
  text-align: center;
  margin-bottom: 20px;
  text-transform: uppercase;
}

.menu-items {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.arrow-btn {
  background-color: #FFC132;
  color: #000;
  border: none;
  font-weight: 900;
  font-size: 0.85rem;
  padding: 8px 6px;
  cursor: pointer;
  clip-path: polygon(0% 0%, 88% 0%, 100% 50%, 88% 100%, 0% 100%);
  text-align: center;
  padding-right: 18px; 
}

.logout-btn {
  background-color: #fff;
  color: #000;
  border: 3px solid #000;
  border-radius: 12px;
  padding: 10px;
  width: 100%;
  font-weight: 900;
  font-size: 0.8rem;
  box-shadow: 0 2px 0 #000;
  cursor: pointer;
}

.logout-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 #000;
}

/* Animación */
.drop-enter-active, .drop-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.2);
}
.drop-enter-from, .drop-leave-to {
  transform: translateX(-50%) translateY(-15px);
  opacity: 0;
}
</style>
