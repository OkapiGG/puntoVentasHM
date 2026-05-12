<script setup>
import { computed, onMounted, ref } from 'vue'
import { apiDelete, apiGet, apiPost, apiPut } from '../services/api'

const usuarioActual = ref(null)
const cargando = ref(false)
const error = ref('')
const exito = ref('')
const tabActiva = ref('categorias')

const categorias = ref([])
const productos = ref([])
const modificadores = ref([])
const insumos = ref([])
const usuarios = ref([])
const rolesDisponibles = ['ADMIN', 'GERENTE', 'CAJERO', 'REPARTIDOR']

const categoriaForm = ref({ idCategoria: null, nombre: '' })
const productoForm = ref({
  idProducto: null,
  idCategoria: '',
  nombre: '',
  precio: '',
  imagenUrl: '',
  activo: true,
  esPopular: false,
  esNuevo: false
})
const modificadorForm = ref({
  idModificador: null,
  idProducto: '',
  nombre: '',
  precioExtra: '',
  activo: true
})
const insumoForm = ref({
  idInsumo: null,
  nombre: '',
  unidadMedida: 'pieza',
  stockActual: '',
  activo: true
})
const usuarioForm = ref({
  idUsuario: null,
  nombre: '',
  pinAcceso: '',
  rol: 'CAJERO',
  avatarUrl: '',
  activo: true
})

const tabLabel = computed(() => ({
  categorias: 'categorías',
  productos: 'productos',
  modificadores: 'modificadores',
  insumos: 'insumos',
  usuarios: 'usuarios'
}[tabActiva.value]))

const cargarUsuario = () => {
  const guardado = sessionStorage.getItem('usuarioActual')
  usuarioActual.value = guardado ? JSON.parse(guardado) : null
}

const resetCategoriaForm = () => {
  categoriaForm.value = { idCategoria: null, nombre: '' }
}

const resetProductoForm = () => {
  productoForm.value = {
    idProducto: null,
    idCategoria: categorias.value[0]?.idCategoria ?? '',
    nombre: '',
    precio: '',
    imagenUrl: '',
    activo: true,
    esPopular: false,
    esNuevo: false
  }
}

const resetModificadorForm = () => {
  modificadorForm.value = {
    idModificador: null,
    idProducto: productos.value[0]?.idProducto ?? '',
    nombre: '',
    precioExtra: '',
    activo: true
  }
}

const resetInsumoForm = () => {
  insumoForm.value = {
    idInsumo: null,
    nombre: '',
    unidadMedida: 'pieza',
    stockActual: '',
    activo: true
  }
}

const resetUsuarioForm = () => {
  usuarioForm.value = {
    idUsuario: null,
    nombre: '',
    pinAcceso: '',
    rol: 'CAJERO',
    avatarUrl: '',
    activo: true
  }
}

const resetForms = () => {
  resetCategoriaForm()
  resetProductoForm()
  resetModificadorForm()
  resetInsumoForm()
  resetUsuarioForm()
}

const cargarCatalogo = async () => {
  if (!usuarioActual.value?.idNegocio) {
    return
  }

  cargando.value = true
  error.value = ''
  try {
    const respuesta = await apiGet(`/admin/negocios/${usuarioActual.value.idNegocio}/catalogo`)
    categorias.value = respuesta.categorias
    productos.value = respuesta.productos
    modificadores.value = respuesta.modificadores
    insumos.value = respuesta.insumos
    usuarios.value = respuesta.usuarios
    resetForms()
  } catch (err) {
    error.value = err.message || 'No fue posible cargar el catálogo administrativo'
  } finally {
    cargando.value = false
  }
}

const guardarCategoria = async () => {
  await guardarEntidad(
    categoriaForm.value.idCategoria
      ? () => apiPut(`/admin/negocios/${usuarioActual.value.idNegocio}/categorias/${categoriaForm.value.idCategoria}`, { nombre: categoriaForm.value.nombre })
      : () => apiPost(`/admin/negocios/${usuarioActual.value.idNegocio}/categorias`, { nombre: categoriaForm.value.nombre }),
    'Categoría guardada'
  )
}

const guardarProducto = async () => {
  const payload = {
    idCategoria: Number(productoForm.value.idCategoria),
    nombre: productoForm.value.nombre,
    precio: Number(productoForm.value.precio || 0),
    imagenUrl: productoForm.value.imagenUrl,
    activo: productoForm.value.activo,
    esPopular: productoForm.value.esPopular,
    esNuevo: productoForm.value.esNuevo
  }

  await guardarEntidad(
    productoForm.value.idProducto
      ? () => apiPut(`/admin/negocios/${usuarioActual.value.idNegocio}/productos/${productoForm.value.idProducto}`, payload)
      : () => apiPost(`/admin/negocios/${usuarioActual.value.idNegocio}/productos`, payload),
    'Producto guardado'
  )
}

const guardarModificador = async () => {
  const payload = {
    idProducto: Number(modificadorForm.value.idProducto),
    nombre: modificadorForm.value.nombre,
    precioExtra: Number(modificadorForm.value.precioExtra || 0),
    activo: modificadorForm.value.activo
  }

  await guardarEntidad(
    modificadorForm.value.idModificador
      ? () => apiPut(`/admin/negocios/${usuarioActual.value.idNegocio}/modificadores/${modificadorForm.value.idModificador}`, payload)
      : () => apiPost(`/admin/negocios/${usuarioActual.value.idNegocio}/modificadores`, payload),
    'Modificador guardado'
  )
}

const guardarInsumo = async () => {
  const payload = {
    nombre: insumoForm.value.nombre,
    unidadMedida: insumoForm.value.unidadMedida,
    stockActual: Number(insumoForm.value.stockActual || 0),
    activo: insumoForm.value.activo
  }

  await guardarEntidad(
    insumoForm.value.idInsumo
      ? () => apiPut(`/admin/negocios/${usuarioActual.value.idNegocio}/insumos/${insumoForm.value.idInsumo}`, payload)
      : () => apiPost(`/admin/negocios/${usuarioActual.value.idNegocio}/insumos`, payload),
    'Insumo guardado'
  )
}

const guardarUsuario = async () => {
  const payload = {
    nombre: usuarioForm.value.nombre,
    pinAcceso: usuarioForm.value.pinAcceso,
    rol: usuarioForm.value.rol,
    avatarUrl: usuarioForm.value.avatarUrl,
    activo: usuarioForm.value.activo
  }

  await guardarEntidad(
    usuarioForm.value.idUsuario
      ? () => apiPut(`/admin/negocios/${usuarioActual.value.idNegocio}/usuarios/${usuarioForm.value.idUsuario}`, payload)
      : () => apiPost(`/admin/negocios/${usuarioActual.value.idNegocio}/usuarios`, payload),
    'Usuario guardado'
  )
}

const guardarEntidad = async (operacion, mensaje) => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    await operacion()
    exito.value = mensaje
    await cargarCatalogo()
  } catch (err) {
    error.value = err.message || 'No fue posible guardar'
  } finally {
    cargando.value = false
  }
}

const desactivar = async (tipo, id) => {
  error.value = ''
  exito.value = ''
  cargando.value = true
  try {
    await apiDelete(`/admin/negocios/${usuarioActual.value.idNegocio}/${tipo}/${id}`)
    exito.value = 'Registro actualizado'
    await cargarCatalogo()
  } catch (err) {
    error.value = err.message || 'No fue posible eliminar'
  } finally {
    cargando.value = false
  }
}

const editarCategoria = (categoria) => {
  categoriaForm.value = { ...categoria }
  tabActiva.value = 'categorias'
}

const editarProducto = (producto) => {
  productoForm.value = {
    idProducto: producto.idProducto,
    idCategoria: producto.idCategoria,
    nombre: producto.nombre,
    precio: producto.precio,
    imagenUrl: producto.imagenUrl || '',
    activo: producto.activo,
    esPopular: producto.esPopular,
    esNuevo: producto.esNuevo
  }
  tabActiva.value = 'productos'
}

const editarModificador = (modificador) => {
  modificadorForm.value = {
    idModificador: modificador.idModificador,
    idProducto: modificador.idProducto,
    nombre: modificador.nombre,
    precioExtra: modificador.precioExtra,
    activo: modificador.activo
  }
  tabActiva.value = 'modificadores'
}

const editarInsumo = (insumo) => {
  insumoForm.value = {
    idInsumo: insumo.idInsumo,
    nombre: insumo.nombre,
    unidadMedida: insumo.unidadMedida,
    stockActual: insumo.stockActual,
    activo: insumo.activo
  }
  tabActiva.value = 'insumos'
}

const editarUsuario = (usuario) => {
  usuarioForm.value = {
    idUsuario: usuario.idUsuario,
    nombre: usuario.nombre,
    pinAcceso: '',
    rol: usuario.rol,
    avatarUrl: usuario.avatarUrl || '',
    activo: usuario.activo
  }
  tabActiva.value = 'usuarios'
}

onMounted(async () => {
  cargarUsuario()
  await cargarCatalogo()
})
</script>

<template>
  <section class="admin-layout">
    <div class="admin-card">
      <div class="head">
        <div>
          <h1>ADMINISTRACIÓN</h1>
          <p>CRUD de categorías, productos, modificadores, insumos y empleados.</p>
        </div>
      </div>

      <p v-if="error" class="status error">{{ error }}</p>
      <p v-if="exito" class="status success">{{ exito }}</p>

      <div class="tabs">
        <button :class="{ active: tabActiva === 'categorias' }" @click="tabActiva = 'categorias'">Categorías</button>
        <button :class="{ active: tabActiva === 'productos' }" @click="tabActiva = 'productos'">Productos</button>
        <button :class="{ active: tabActiva === 'modificadores' }" @click="tabActiva = 'modificadores'">Modificadores</button>
        <button :class="{ active: tabActiva === 'insumos' }" @click="tabActiva = 'insumos'">Insumos</button>
        <button :class="{ active: tabActiva === 'usuarios' }" @click="tabActiva = 'usuarios'">Usuarios</button>
      </div>

      <div class="admin-grid">
        <div class="panel">
          <h2>Editar {{ tabLabel }}</h2>

          <div v-if="tabActiva === 'categorias'" class="form-stack">
            <label class="field">
              <span>Nombre</span>
              <input v-model="categoriaForm.nombre" type="text" />
            </label>
            <div class="actions">
              <button class="btn primary" :disabled="cargando" @click="guardarCategoria">Guardar</button>
              <button class="btn" :disabled="cargando" @click="resetCategoriaForm">Limpiar</button>
            </div>
          </div>

          <div v-else-if="tabActiva === 'productos'" class="form-stack">
            <label class="field">
              <span>Categoría</span>
              <select v-model="productoForm.idCategoria">
                <option v-for="categoria in categorias" :key="categoria.idCategoria" :value="categoria.idCategoria">{{ categoria.nombre }}</option>
              </select>
            </label>
            <label class="field">
              <span>Nombre</span>
              <input v-model="productoForm.nombre" type="text" />
            </label>
            <label class="field">
              <span>Precio</span>
              <input v-model="productoForm.precio" type="number" min="0" step="0.01" />
            </label>
            <label class="field">
              <span>Imagen URL</span>
              <input v-model="productoForm.imagenUrl" type="text" />
            </label>
            <div class="checks">
              <label><input v-model="productoForm.activo" type="checkbox" /> Activo</label>
              <label><input v-model="productoForm.esPopular" type="checkbox" /> Popular</label>
              <label><input v-model="productoForm.esNuevo" type="checkbox" /> Nuevo</label>
            </div>
            <div class="actions">
              <button class="btn primary" :disabled="cargando" @click="guardarProducto">Guardar</button>
              <button class="btn" :disabled="cargando" @click="resetProductoForm">Limpiar</button>
            </div>
          </div>

          <div v-else-if="tabActiva === 'modificadores'" class="form-stack">
            <label class="field">
              <span>Producto</span>
              <select v-model="modificadorForm.idProducto">
                <option v-for="producto in productos" :key="producto.idProducto" :value="producto.idProducto">{{ producto.nombre }}</option>
              </select>
            </label>
            <label class="field">
              <span>Nombre</span>
              <input v-model="modificadorForm.nombre" type="text" />
            </label>
            <label class="field">
              <span>Precio extra</span>
              <input v-model="modificadorForm.precioExtra" type="number" min="0" step="0.01" />
            </label>
            <div class="checks">
              <label><input v-model="modificadorForm.activo" type="checkbox" /> Activo</label>
            </div>
            <div class="actions">
              <button class="btn primary" :disabled="cargando" @click="guardarModificador">Guardar</button>
              <button class="btn" :disabled="cargando" @click="resetModificadorForm">Limpiar</button>
            </div>
          </div>

          <div v-else-if="tabActiva === 'insumos'" class="form-stack">
            <label class="field">
              <span>Nombre</span>
              <input v-model="insumoForm.nombre" type="text" />
            </label>
            <label class="field">
              <span>Unidad</span>
              <input v-model="insumoForm.unidadMedida" type="text" />
            </label>
            <label class="field">
              <span>Stock actual</span>
              <input v-model="insumoForm.stockActual" type="number" min="0" step="0.001" />
            </label>
            <div class="checks">
              <label><input v-model="insumoForm.activo" type="checkbox" /> Activo</label>
            </div>
            <div class="actions">
              <button class="btn primary" :disabled="cargando" @click="guardarInsumo">Guardar</button>
              <button class="btn" :disabled="cargando" @click="resetInsumoForm">Limpiar</button>
            </div>
          </div>

          <div v-else class="form-stack">
            <label class="field">
              <span>Nombre</span>
              <input v-model="usuarioForm.nombre" type="text" />
            </label>
            <label class="field">
              <span>PIN</span>
              <input v-model="usuarioForm.pinAcceso" type="text" placeholder="Requerido al crear; opcional al editar" />
            </label>
            <label class="field">
              <span>Rol</span>
              <select v-model="usuarioForm.rol">
                <option v-for="rol in rolesDisponibles" :key="rol" :value="rol">{{ rol }}</option>
              </select>
            </label>
            <label class="field">
              <span>Avatar URL</span>
              <input v-model="usuarioForm.avatarUrl" type="text" />
            </label>
            <div class="checks">
              <label><input v-model="usuarioForm.activo" type="checkbox" /> Activo</label>
            </div>
            <div class="actions">
              <button class="btn primary" :disabled="cargando" @click="guardarUsuario">Guardar</button>
              <button class="btn" :disabled="cargando" @click="resetUsuarioForm">Limpiar</button>
            </div>
          </div>
        </div>

        <div class="panel">
          <h2>Listado</h2>
          <div v-if="cargando" class="empty-state">Cargando...</div>

          <div v-else-if="tabActiva === 'categorias'" class="list">
            <div v-for="categoria in categorias" :key="categoria.idCategoria" class="item">
              <div>
                <strong>{{ categoria.nombre }}</strong>
                <span>{{ categoria.totalProductos }} productos</span>
              </div>
              <div class="item-actions">
                <button class="mini-btn" @click="editarCategoria(categoria)">Editar</button>
                <button class="mini-btn danger" @click="desactivar('categorias', categoria.idCategoria)">Eliminar</button>
              </div>
            </div>
          </div>

          <div v-else-if="tabActiva === 'productos'" class="list">
            <div v-for="producto in productos" :key="producto.idProducto" class="item">
              <div>
                <strong>{{ producto.nombre }}</strong>
                <span>{{ producto.categoriaNombre }} · ${{ Number(producto.precio).toFixed(2) }}</span>
                <span>{{ producto.activo ? 'Activo' : 'Inactivo' }}</span>
              </div>
              <div class="item-actions">
                <button class="mini-btn" @click="editarProducto(producto)">Editar</button>
                <button class="mini-btn danger" @click="desactivar('productos', producto.idProducto)">Desactivar</button>
              </div>
            </div>
          </div>

          <div v-else-if="tabActiva === 'modificadores'" class="list">
            <div v-for="modificador in modificadores" :key="modificador.idModificador" class="item">
              <div>
                <strong>{{ modificador.nombre }}</strong>
                <span>{{ modificador.productoNombre }} · ${{ Number(modificador.precioExtra).toFixed(2) }}</span>
                <span>{{ modificador.activo ? 'Activo' : 'Inactivo' }}</span>
              </div>
              <div class="item-actions">
                <button class="mini-btn" @click="editarModificador(modificador)">Editar</button>
                <button class="mini-btn danger" @click="desactivar('modificadores', modificador.idModificador)">Desactivar</button>
              </div>
            </div>
          </div>

          <div v-else-if="tabActiva === 'insumos'" class="list">
            <div v-for="insumo in insumos" :key="insumo.idInsumo" class="item">
              <div>
                <strong>{{ insumo.nombre }}</strong>
                <span>{{ insumo.unidadMedida }} · {{ Number(insumo.stockActual).toFixed(3) }}</span>
                <span>{{ insumo.activo ? 'Activo' : 'Inactivo' }}</span>
              </div>
              <div class="item-actions">
                <button class="mini-btn" @click="editarInsumo(insumo)">Editar</button>
                <button class="mini-btn danger" @click="desactivar('insumos', insumo.idInsumo)">Desactivar</button>
              </div>
            </div>
          </div>

          <div v-else class="list">
            <div v-for="usuario in usuarios" :key="usuario.idUsuario" class="item">
              <div>
                <strong>{{ usuario.nombre }}</strong>
                <span>{{ usuario.rol }}</span>
                <span>{{ usuario.activo ? 'Activo' : 'Inactivo' }}</span>
              </div>
              <div class="item-actions">
                <button class="mini-btn" @click="editarUsuario(usuario)">Editar</button>
                <button class="mini-btn danger" @click="desactivar('usuarios', usuario.idUsuario)">Desactivar</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-layout {
  width: 100%;
  display: flex;
  justify-content: center;
}

.admin-card {
  width: min(1240px, 100%);
  padding: 24px;
  background: white;
  border: 4px solid black;
  border-radius: 24px;
  box-shadow: 10px 10px 0 rgba(0, 0, 0, 0.1);
}

.head h1 {
  margin: 0;
  font-family: var(--font-main);
  color: var(--rojo-may);
}

.head p {
  margin: 6px 0 0;
  color: #4b5563;
}

.status {
  margin: 14px 0;
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

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 18px 0;
}

.tabs button {
  border: 3px solid #000;
  border-radius: 12px;
  padding: 10px 14px;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
}

.tabs button.active {
  background: #fff1b8;
}

.admin-grid {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 18px;
}

.panel {
  border: 3px solid #000;
  border-radius: 18px;
  padding: 18px;
  background: #fffdf7;
}

.panel h2 {
  margin: 0 0 12px;
  font-family: var(--font-main);
  color: var(--rojo-may);
}

.form-stack,
.list {
  display: grid;
  gap: 12px;
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

.checks {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-weight: 700;
}

.actions,
.item-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn,
.mini-btn {
  border: 2px solid #000;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
}

.btn {
  padding: 12px 14px;
}

.btn.primary {
  background: var(--rojo-may);
  color: #fff;
}

.mini-btn {
  padding: 8px 10px;
}

.danger {
  background: #111827;
  color: #fff;
}

.item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  border: 2px solid #000;
  border-radius: 14px;
  background: #fff;
}

.item span {
  display: block;
  margin-top: 4px;
  color: #4b5563;
}

.empty-state {
  padding: 18px;
  border: 2px dashed #d1d5db;
  border-radius: 14px;
  color: #6b7280;
}

@media (max-width: 980px) {
  .admin-grid {
    grid-template-columns: 1fr;
  }
}
</style>
