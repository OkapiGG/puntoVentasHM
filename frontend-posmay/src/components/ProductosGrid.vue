<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { agregarProducto, cantidadProducto, carritoStore, decrementarPorProducto } from '../store/carrito'
import { apiGet } from '../services/api'

const props = defineProps(['categoria'])
const router = useRouter()

const productosBase = ref([])
const paginaActual = ref(0)
const itemsPorPagina = 8
const cargando = ref(false)
const error = ref('')
const productoEnConfiguracion = ref(null)
const modificadoresSeleccionados = ref([])

const productosVisibles = computed(() => {
  const inicio = paginaActual.value * itemsPorPagina
  return productosBase.value.slice(inicio, inicio + itemsPorPagina)
})

const tieneModificadores = (producto) => {
  return Array.isArray(producto.modificadores) && producto.modificadores.length > 0
}

const siguiente = () => {
  if ((paginaActual.value + 1) * itemsPorPagina < productosBase.value.length) {
    paginaActual.value++
  }
}

const anterior = () => {
  if (paginaActual.value > 0) {
    paginaActual.value--
  }
}

const irAVentas = () => router.push({ name: 'ventas' })

const abrirConfiguracion = (producto) => {
  productoEnConfiguracion.value = producto
  modificadoresSeleccionados.value = []
}

const cerrarConfiguracion = () => {
  productoEnConfiguracion.value = null
  modificadoresSeleccionados.value = []
}

const toggleModificador = (modificador) => {
  const existe = modificadoresSeleccionados.value.some(item => item.idModificador === modificador.idModificador)
  if (existe) {
    modificadoresSeleccionados.value = modificadoresSeleccionados.value.filter(item => item.idModificador !== modificador.idModificador)
    return
  }

  modificadoresSeleccionados.value = [...modificadoresSeleccionados.value, modificador]
}

const confirmarProducto = () => {
  if (!productoEnConfiguracion.value) {
    return
  }

  agregarProducto(productoEnConfiguracion.value, modificadoresSeleccionados.value)
  sincronizarCantidades()
  cerrarConfiguracion()
}

const agregarPrimero = (producto) => {
  if (tieneModificadores(producto)) {
    abrirConfiguracion(producto)
    return
  }

  agregarProducto(producto)
  sincronizarCantidades()
}

const sumar = (producto) => {
  if (tieneModificadores(producto)) {
    abrirConfiguracion(producto)
    return
  }

  agregarProducto(producto)
  sincronizarCantidades()
}

const restar = (producto) => {
  decrementarPorProducto(producto.idProducto)
  sincronizarCantidades()
}

const sincronizarCantidades = () => {
  productosBase.value = productosBase.value.map(producto => ({
    ...producto,
    cantidad: cantidadProducto(producto.idProducto)
  }))
}

const cargarProductos = async () => {
  cargando.value = true
  error.value = ''
  paginaActual.value = 0

  try {
    const productos = await apiGet(`/categorias/${props.categoria}/productos`)
    productosBase.value = productos.map(producto => ({
      ...producto,
      cantidad: cantidadProducto(producto.idProducto),
      descripcion: producto.esPopular ? 'Producto popular del menu.' : 'Disponible en el catalogo.'
    }))
  } catch (err) {
    productosBase.value = []
    error.value = err.message || 'No fue posible cargar los productos'
  } finally {
    cargando.value = false
  }
}

watch(() => props.categoria, cargarProductos)
watch(() => carritoStore.items.value.length, sincronizarCantidades)
onMounted(cargarProductos)
</script>

<template>
  <div class="categorias-container">
    <header class="header-menu">
      <div class="title-group">
        <h1 class="title">{{ (categoria || 'PRODUCTOS').replace(/-/g, ' ').toUpperCase() }}</h1>
        <div class="title-underline"></div>
      </div>

      <div class="action-bar">
        <button class="btn-control" @click="anterior" :disabled="paginaActual === 0">ANT</button>

        <div class="cart-status">
          <div class="cart-icon-wrapper" @click="irAVentas">
            <span class="menu-icon">📋</span>
            <img src="@/assets/images/Logomay.png" alt="Cart" class="cart-img" />
            <span class="cart-badge">{{ carritoStore.totalItems.value }}</span>
          </div>
        </div>

        <button
          class="btn-control"
          @click="siguiente"
          :disabled="(paginaActual + 1) * itemsPorPagina >= productosBase.length"
        >
          SIG
        </button>
      </div>
    </header>

    <p v-if="cargando" class="status-message">Cargando productos...</p>
    <p v-else-if="error" class="status-message error">{{ error }}</p>

    <div class="categorias-grid">
      <button class="btn-add-category" aria-label="Agregar platillo">
        <span class="plus-icon">+</span>
      </button>

      <div v-for="producto in productosVisibles" :key="producto.idProducto" class="producto-card">
        <div class="card-info">
          <div class="card-top">
            <h3>{{ producto.nombre }}</h3>
            <span class="price">$ {{ Number(producto.precio).toFixed(2) }}</span>
          </div>
          <p class="description">{{ producto.descripcion }}</p>
          <p v-if="tieneModificadores(producto)" class="modifier-hint">Admite extras</p>
        </div>

        <div class="card-footer">
          <button v-if="producto.cantidad === 0" class="btn-agregar" @click="agregarPrimero(producto)">
            AGREGAR
          </button>
          <div v-else class="counter-group">
            <button class="btn-qty-circle" @click="restar(producto)">-</button>
            <span class="qty-number">{{ producto.cantidad }}</span>
            <button class="btn-qty-circle" @click="sumar(producto)">+</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="productoEnConfiguracion" class="modal-overlay" @click.self="cerrarConfiguracion">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <h2>{{ productoEnConfiguracion.nombre }}</h2>
            <p>Selecciona los modificadores que deseas agregar</p>
          </div>
          <button class="close-btn" @click="cerrarConfiguracion">×</button>
        </div>

        <div class="mods-list">
          <label
            v-for="modificador in productoEnConfiguracion.modificadores"
            :key="modificador.idModificador"
            class="mod-item"
          >
            <input
              type="checkbox"
              :checked="modificadoresSeleccionados.some(item => item.idModificador === modificador.idModificador)"
              @change="toggleModificador(modificador)"
            />
            <span>{{ modificador.nombre }}</span>
            <strong>+${{ Number(modificador.precioExtra).toFixed(2) }}</strong>
          </label>
        </div>

        <button class="btn-confirm" @click="confirmarProducto">Agregar al carrito</button>
      </div>
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

.title {
  font-family: 'Arial Black', sans-serif;
  font-size: 2.5rem;
  color: #000;
  margin: 0;
  line-height: 1;
}

.title-underline {
  height: 8px;
  background-color: #ffc132;
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
}

.btn-control:disabled {
  opacity: 0.3;
  cursor: not-allowed;
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

.menu-icon {
  font-size: 1.5rem;
  margin-right: 12px;
}

.cart-img {
  height: 40px;
}

.cart-badge {
  position: absolute;
  top: -12px;
  right: -15px;
  background-color: #ffc132;
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
}

.plus-icon {
  font-size: 6rem;
  font-weight: 100;
  line-height: 0;
}

.producto-card {
  background: #ffffff;
  border: 4px solid #000;
  border-radius: 20px;
  padding: 1.2rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-top h3 {
  font-family: 'Arial Black', sans-serif;
  font-size: 0.9rem;
  margin: 0;
}

.price {
  font-weight: 900;
  font-size: 1.1rem;
}

.description {
  font-size: 0.75rem;
  color: #444;
  margin: 5px 0;
  line-height: 1.2;
}

.modifier-hint {
  margin: 0.3rem 0 0;
  color: #d32f2f;
  font-weight: bold;
  font-size: 0.75rem;
}

.btn-agregar {
  width: 100%;
  background-color: #d32f2f;
  color: white;
  border: 3px solid #000;
  border-radius: 10px;
  padding: 10px;
  font-family: 'Arial Black', sans-serif;
  cursor: pointer;
  box-shadow: 0 4px 0 #000;
}

.btn-agregar:active {
  transform: translateY(3px);
  box-shadow: 0 1px 0 #000;
}

.counter-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-qty-circle {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  border: 3px solid #d32f2f;
  background: white;
  color: #d32f2f;
  font-size: 1.5rem;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.qty-number {
  font-size: 1.6rem;
  font-weight: 900;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.modal-card {
  width: min(520px, 92vw);
  background: white;
  border: 4px solid #000;
  border-radius: 24px;
  padding: 1.4rem;
  box-shadow: 14px 14px 0 rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.modal-header h2 {
  margin: 0;
}

.modal-header p {
  margin: 0.2rem 0 0;
}

.close-btn {
  width: 38px;
  height: 38px;
  border: 2px solid #000;
  border-radius: 50%;
  background: white;
  font-size: 1.5rem;
  cursor: pointer;
}

.mods-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.mod-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 0.75rem;
  align-items: center;
  border: 2px solid #000;
  border-radius: 14px;
  padding: 0.8rem 1rem;
}

.btn-confirm {
  width: 100%;
  background: #d32f2f;
  color: white;
  border: 3px solid #000;
  border-radius: 12px;
  padding: 0.9rem;
  font-family: 'Arial Black', sans-serif;
  cursor: pointer;
}
</style>
