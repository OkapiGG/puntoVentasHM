<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { carritoStore } from '../store/carrito'
import { apiGet } from '../services/api'

const router = useRouter()
const categorias = ref([])
const paginaActual = ref(0)
const itemsPorPagina = 8
const cargando = ref(false)
const error = ref('')

const categoriasVisibles = computed(() => {
  const inicio = paginaActual.value * itemsPorPagina
  return categorias.value.slice(inicio, inicio + itemsPorPagina)
})

const irACategoria = (categoria) => {
  router.push({ name: 'productos-categoria', params: { categoria: categoria.slug } })
}

const siguiente = () => {
  if ((paginaActual.value + 1) * itemsPorPagina < categorias.value.length) {
    paginaActual.value++
  }
}

const anterior = () => {
  if (paginaActual.value > 0) {
    paginaActual.value--
  }
}

const cargarCategorias = async () => {
  cargando.value = true
  error.value = ''

  try {
    categorias.value = await apiGet('/categorias')
  } catch (err) {
    error.value = err.message || 'No fue posible cargar las categorias'
  } finally {
    cargando.value = false
  }
}

const irAVentas = () => {
  router.push({ name: 'ventas' })
}

onMounted(cargarCategorias)
</script>

<template>
  <div class="categorias-container">
    <header class="header-menu">
      <div class="title-group">
        <h1 class="title">CATEGORÍAS</h1>
        <div class="title-underline"></div>
      </div>

      <div class="action-bar">
        <button class="btn-control" @click="anterior" :disabled="paginaActual === 0">ANT</button>

        <div class="cart-status">
          <div class="cart-icon-wrapper" @click="irAVentas">
            <img src="@/assets/images/Logomay.png" alt="Cart" class="cart-img" />
            <span class="cart-badge">{{ carritoStore.totalItems.value }}</span>
          </div>
        </div>

        <button
          class="btn-control"
          @click="siguiente"
          :disabled="(paginaActual + 1) * itemsPorPagina >= categorias.length"
        >
          SIG
        </button>
      </div>
    </header>

    <p v-if="cargando" class="status-message">Cargando categorias...</p>
    <p v-else-if="error" class="status-message error">{{ error }}</p>

    <div class="categorias-grid">
      <button class="btn-add-category" aria-label="Agregar categoría">
        <span class="plus-icon">+</span>
      </button>

      <button
        v-for="categoria in categoriasVisibles"
        :key="categoria.idCategoria"
        class="cat-button"
        @click="irACategoria(categoria)"
      >
        <span class="cat-text">{{ categoria.nombre }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.categorias-container {
  background-color: #ffffff;
  border: 4px solid #000000;
  border-radius: 30px;
  padding: 2rem;
  width: 98%;
  max-width: 1600px;
  box-shadow: 12px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-menu {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2.5rem;
}

.title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title {
  font-family: 'Arial Black', sans-serif;
  font-size: 2.5rem;
  color: #000;
  margin: 0;
  line-height: 1;
}

.title-underline {
  height: 8px;
  background-color: var(--amarillo-may, #ffc132);
  border-radius: 10px;
  width: 100%;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-control {
  background: #f0f0f0;
  border: 2px solid #000;
  border-radius: 12px;
  padding: 0.8rem 1.5rem;
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-control:hover:not(:disabled) {
  background: #e0e0e0;
}

.btn-control:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  filter: grayscale(1);
}

.cart-status {
  background: #ffffff;
  border: 3px solid #000;
  border-radius: 15px;
  padding: 8px 20px;
  box-shadow: 0 4px 0 #000;
}

.cart-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.cart-img {
  height: 40px;
}

.cart-badge {
  position: absolute;
  top: -12px;
  right: -15px;
  background-color: var(--amarillo-may, #ffc132);
  border: 3px solid #000;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 0.9rem;
}

.status-message {
  font-weight: bold;
  margin: 0 0 1rem;
}

.status-message.error {
  color: #b71c1c;
}

.categorias-grid {
  display: grid;
  grid-template-columns: 140px repeat(4, 1fr);
  grid-template-rows: repeat(2, 180px);
  gap: 25px;
  min-height: 400px;
}

.btn-add-category {
  grid-row: span 2;
  background: #ffffff;
  border: 4px solid #000;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 0 #000;
  transition: all 0.1s;
}

.plus-icon {
  font-size: 6rem;
  font-weight: 100;
  line-height: 0;
  margin-bottom: 10px;
}

.cat-button {
  background: #ffffff;
  border: 4px solid #000;
  border-radius: 20px;
  padding: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  box-shadow: 0 8px 0 #000;
  transition: all 0.1s ease;
}

.cat-text {
  font-family: 'Arial Black', sans-serif;
  font-size: 1.3rem;
  color: #000;
  text-transform: uppercase;
}

.cat-button:active,
.btn-add-category:active {
  transform: translateY(6px);
  box-shadow: 0 2px 0 #000;
}

.cat-button:hover:not(:active) {
  background-color: #fff9e6;
  transform: translateY(-2px);
  box-shadow: 0 10px 0 #000;
}
</style>
