<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Estado para el formulario
const usuario = ref('')
const password = ref('')

// Configuración de Banners
const imagenes = [
  'Banner_Hamburguesas1.jpg',
  'Banner_Hamburguesas2.jpg',
  'Banner_Hamburguesas3.jpg'
]
const indiceActual = ref(0)
let intervalo = null

// Función para cambiar de imagen
const cambiarImagen = () => {
  indiceActual.value = (indiceActual.value + 1) % imagenes.length
}

// Helper para rutas dinámicas de imágenes en Vite
const getImageUrl = (name) => {
  return new URL(`../assets/images/${name}`, import.meta.url).href
}

const handleLogin = () => {
  // Por ahora, entramos directo
  router.push('/dashboard')
}

onMounted(() => {
  intervalo = setInterval(cambiarImagen, 30000) // 30 segundos
})

onUnmounted(() => {
  if (intervalo) clearInterval(intervalo)
})
</script>

<template>
  <div class="login-container">
    <!-- SECCIÓN IZQUIERDA: Formulario -->
    <div class="login-form-section">
      <div class="login-card">
        <img src="@/assets/images/Logomay.png" alt="Logo HM" class="logo" />
        <h1 class="title">HAMBURGUESAS MAY SYSTEM POS</h1>
        
        <form @submit.prevent="handleLogin">
          <div class="input-group">
            <label>USUARIO</label>
            <input type="text" placeholder="Ingrese su usuario" v-model="usuario" />
          </div>
          
          <div class="input-group">
            <label>CONTRASEÑA</label>
            <input type="password" placeholder="Ingrese su contraseña" v-model="password" />
          </div>
          
          <button type="submit" class="btn-entrar">ENTRAR</button>
        </form>
      </div>
    </div>

    <!-- SECCIÓN DERECHA: Banners Animados -->
    <div class="login-image-section">
      <transition name="fade" mode="out-in">
        <img 
          :key="indiceActual" 
          :src="getImageUrl(imagenes[indiceActual])" 
          class="banner-img"
        />
      </transition>
    </div>
  </div>
</template>

<style scoped>
/* Contenedor Principal */
.login-container {
  display: flex;
  height: 100vh;
  width: 100%;
}

/* Lado Izquierdo */
.login-form-section {
  flex: 1;
  background-color: var(--crema-fondo);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  background: white;
  padding: 3rem 2rem; 
  border-radius: 40px;
  box-shadow: 0px 15px 35px rgba(0, 0, 0, 0.2); 
  width: 85%;
  max-width: 500px;
  text-align: center;
  border: 1px solid rgba(0,0,0,0.05);
}

.logo {
  max-width: 120px;
  margin-bottom: 1rem;
}

.title {
  font-family: var(--font-main);
  color: var(--rojo-may);
  font-size: 3.5rem; /* Ajustado ligeramente para mejor fit */
  line-height: 0.9;
  margin-bottom: 2rem;
  text-shadow: 4px 4px 0px var(--amarillo-may);
}

/* Inputs y Formulario */
.input-group {
  text-align: left;
  margin-bottom: 1.5rem;
}

.input-group label {
  font-weight: bold;
  display: block;
  margin-bottom: 8px;
  margin-left: 5px;
}

.input-group input {
  width: 100%;
  padding: 14px 20px; 
  border: 2px solid var(--amarillo-may);
  border-radius: 12px;
  outline: none;
  font-size: 1rem;
  box-sizing: border-box; 
  color: #333;
  transition: border-color 0.3s ease;
}

.input-group input:focus {
  border-color: var(--rojo-may);
}

.btn-entrar {
  width: 100%;
  background-color: var(--rojo-may);
  color: white;
  border: none;
  padding: 15px;
  border-radius: 12px;
  font-family: var(--font-main);
  font-size: 1.8rem;
  cursor: pointer;
  margin-top: 10px;
  transition: transform 0.1s;
}

.btn-entrar:active {
  transform: scale(0.98);
}

/* Lado Derecho (Banners) */
.login-image-section {
  flex: 1;
  position: relative;
  overflow: hidden;
  background-color: #000;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
}

/* Animación Fade */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 1.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Mobile Responsive */
@media (max-width: 1024px) {
  .title { font-size: 2.8rem; }
}

@media (max-width: 768px) {
  .login-image-section { display: none; }
}
</style>