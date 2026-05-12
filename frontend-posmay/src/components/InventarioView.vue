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
const promociones = ref([])
const insumos = ref([])
const usuarios = ref([])
const movimientosInventario = ref([])
const costosProducto = ref([])
const costosModificador = ref([])
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
  esNuevo: false,
  recetaTexto: ''
})
const modificadorForm = ref({
  idModificador: null,
  idProducto: '',
  nombre: '',
  precioExtra: '',
  activo: true,
  recetaTexto: ''
})
const promocionForm = ref({
  idPromocion: null,
  nombre: '',
  descripcion: '',
  tipoRegla: 'PORCENTAJE',
  tipoObjetivo: 'PRODUCTO',
  idsObjetivo: '',
  idModificadorGratis: '',
  fechaInicio: '',
  fechaFin: '',
  horaInicio: '',
  horaFin: '',
  porcentajeDescuento: '',
  montoDescuento: '',
  cantidadMinima: '',
  activa: true
})
const insumoForm = ref({
  idInsumo: null,
  nombre: '',
  unidadMedida: 'pieza',
  stockActual: '',
  stockMinimo: '',
  costoUnitario: '',
  activo: true
})
const movimientoForm = ref({
  idInsumo: '',
  tipo: 'ENTRADA_MANUAL',
  cantidad: '',
  motivo: ''
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
  promociones: 'promociones',
  insumos: 'insumos',
  usuarios: 'usuarios'
}[tabActiva.value]))

const alertasStockBajo = computed(() => insumos.value.filter(insumo => insumo.activo && insumo.stockBajo))

const cargarUsuario = () => {
  const guardado = sessionStorage.getItem('usuarioActual')
  usuarioActual.value = guardado ? JSON.parse(guardado) : null
}

const formatearReceta = (receta = []) => receta
  .map(item => `${item.idInsumo}:${Number(item.cantidad).toFixed(3).replace(/\.?0+$/, '')}`)
  .join('\n')

const parsearReceta = (texto) => {
  if (!texto || !texto.trim()) return []
  return texto
    .split('\n')
    .map(linea => linea.trim())
    .filter(Boolean)
    .map(linea => {
      const [id, cantidad] = linea.split(':').map(valor => valor?.trim())
      if (!id || !cantidad || Number.isNaN(Number(id)) || Number.isNaN(Number(cantidad)) || Number(cantidad) <= 0) {
        throw new Error(`Receta inválida en "${linea}". Usa formato idInsumo:cantidad`)
      }
      return { idInsumo: Number(id), cantidad: Number(cantidad) }
    })
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
    esNuevo: false,
    recetaTexto: ''
  }
}
const resetModificadorForm = () => {
  modificadorForm.value = {
    idModificador: null,
    idProducto: productos.value[0]?.idProducto ?? '',
    nombre: '',
    precioExtra: '',
    activo: true,
    recetaTexto: ''
  }
}
const resetPromocionForm = () => {
  promocionForm.value = {
    idPromocion: null,
    nombre: '',
    descripcion: '',
    tipoRegla: 'PORCENTAJE',
    tipoObjetivo: 'PRODUCTO',
    idsObjetivo: '',
    idModificadorGratis: '',
    fechaInicio: '',
    fechaFin: '',
    horaInicio: '',
    horaFin: '',
    porcentajeDescuento: '',
    montoDescuento: '',
    cantidadMinima: '',
    activa: true
  }
}
const resetInsumoForm = () => {
  insumoForm.value = {
    idInsumo: null,
    nombre: '',
    unidadMedida: 'pieza',
    stockActual: '',
    stockMinimo: '',
    costoUnitario: '',
    activo: true
  }
}
const resetMovimientoForm = () => {
  movimientoForm.value = {
    idInsumo: insumos.value[0]?.idInsumo ?? '',
    tipo: 'ENTRADA_MANUAL',
    cantidad: '',
    motivo: ''
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

const cargarInventarioDetalle = async () => {
  const [movimientos, costosProductos, costosModificadores] = await Promise.all([
    apiGet(`/inventario/negocios/${usuarioActual.value.idNegocio}/movimientos`),
    apiGet(`/inventario/negocios/${usuarioActual.value.idNegocio}/costos-producto`),
    apiGet(`/inventario/negocios/${usuarioActual.value.idNegocio}/costos-modificador`)
  ])
  movimientosInventario.value = movimientos
  costosProducto.value = costosProductos
  costosModificador.value = costosModificadores
}

const cargarCatalogo = async () => {
  if (!usuarioActual.value?.idNegocio) return
  cargando.value = true
  error.value = ''
  try {
    const respuesta = await apiGet(`/admin/negocios/${usuarioActual.value.idNegocio}/catalogo`)
    categorias.value = respuesta.categorias
    productos.value = respuesta.productos
    modificadores.value = respuesta.modificadores
    promociones.value = respuesta.promociones || []
    insumos.value = respuesta.insumos
    usuarios.value = respuesta.usuarios
    await cargarInventarioDetalle()
    resetCategoriaForm()
    resetProductoForm()
    resetModificadorForm()
    resetPromocionForm()
    resetInsumoForm()
    resetMovimientoForm()
    resetUsuarioForm()
  } catch (err) {
    error.value = err.message || 'No fue posible cargar el catálogo administrativo'
  } finally {
    cargando.value = false
  }
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
    esNuevo: productoForm.value.esNuevo,
    receta: parsearReceta(productoForm.value.recetaTexto)
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
    activo: modificadorForm.value.activo,
    receta: parsearReceta(modificadorForm.value.recetaTexto)
  }
  await guardarEntidad(
    modificadorForm.value.idModificador
      ? () => apiPut(`/admin/negocios/${usuarioActual.value.idNegocio}/modificadores/${modificadorForm.value.idModificador}`, payload)
      : () => apiPost(`/admin/negocios/${usuarioActual.value.idNegocio}/modificadores`, payload),
    'Modificador guardado'
  )
}

const guardarPromocion = async () => {
  const payload = {
    nombre: promocionForm.value.nombre,
    descripcion: promocionForm.value.descripcion,
    tipoRegla: promocionForm.value.tipoRegla,
    tipoObjetivo: promocionForm.value.tipoObjetivo,
    idsObjetivo: promocionForm.value.idsObjetivo,
    idModificadorGratis: promocionForm.value.idModificadorGratis ? Number(promocionForm.value.idModificadorGratis) : null,
    fechaInicio: promocionForm.value.fechaInicio,
    fechaFin: promocionForm.value.fechaFin,
    horaInicio: promocionForm.value.horaInicio || null,
    horaFin: promocionForm.value.horaFin || null,
    porcentajeDescuento: promocionForm.value.porcentajeDescuento === '' ? null : Number(promocionForm.value.porcentajeDescuento),
    montoDescuento: promocionForm.value.montoDescuento === '' ? null : Number(promocionForm.value.montoDescuento),
    cantidadMinima: promocionForm.value.cantidadMinima === '' ? null : Number(promocionForm.value.cantidadMinima),
    activa: promocionForm.value.activa
  }
  await guardarEntidad(
    promocionForm.value.idPromocion
      ? () => apiPut(`/admin/negocios/${usuarioActual.value.idNegocio}/promociones/${promocionForm.value.idPromocion}`, payload)
      : () => apiPost(`/admin/negocios/${usuarioActual.value.idNegocio}/promociones`, payload),
    'Promoción guardada'
  )
}

const guardarInsumo = async () => {
  const payload = {
    nombre: insumoForm.value.nombre,
    unidadMedida: insumoForm.value.unidadMedida,
    stockActual: Number(insumoForm.value.stockActual || 0),
    stockMinimo: Number(insumoForm.value.stockMinimo || 0),
    costoUnitario: Number(insumoForm.value.costoUnitario || 0),
    activo: insumoForm.value.activo
  }
  await guardarEntidad(
    insumoForm.value.idInsumo
      ? () => apiPut(`/admin/negocios/${usuarioActual.value.idNegocio}/insumos/${insumoForm.value.idInsumo}`, payload)
      : () => apiPost(`/admin/negocios/${usuarioActual.value.idNegocio}/insumos`, payload),
    'Insumo guardado'
  )
}

const guardarMovimientoInventario = async () => {
  if (!movimientoForm.value.idInsumo) {
    error.value = 'Selecciona un insumo'
    return
  }
  await guardarEntidad(
    () => apiPost(`/inventario/negocios/${usuarioActual.value.idNegocio}/insumos/${movimientoForm.value.idInsumo}/movimientos`, {
      tipo: movimientoForm.value.tipo,
      cantidad: Number(movimientoForm.value.cantidad || 0),
      motivo: movimientoForm.value.motivo
    }),
    'Movimiento de inventario guardado'
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

const desactivar = async (tipo, id) => {
  await guardarEntidad(
    () => apiDelete(`/admin/negocios/${usuarioActual.value.idNegocio}/${tipo}/${id}`),
    'Registro actualizado'
  )
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
    esNuevo: producto.esNuevo,
    recetaTexto: formatearReceta(producto.receta)
  }
  tabActiva.value = 'productos'
}
const editarModificador = (modificador) => {
  modificadorForm.value = {
    idModificador: modificador.idModificador,
    idProducto: modificador.idProducto,
    nombre: modificador.nombre,
    precioExtra: modificador.precioExtra,
    activo: modificador.activo,
    recetaTexto: formatearReceta(modificador.receta)
  }
  tabActiva.value = 'modificadores'
}
const editarPromocion = (promocion) => {
  promocionForm.value = {
    idPromocion: promocion.idPromocion,
    nombre: promocion.nombre,
    descripcion: promocion.descripcion || '',
    tipoRegla: promocion.tipoRegla,
    tipoObjetivo: promocion.tipoObjetivo,
    idsObjetivo: promocion.idsObjetivo || '',
    idModificadorGratis: promocion.idModificadorGratis || '',
    fechaInicio: promocion.fechaInicio,
    fechaFin: promocion.fechaFin,
    horaInicio: promocion.horaInicio || '',
    horaFin: promocion.horaFin || '',
    porcentajeDescuento: promocion.porcentajeDescuento ?? '',
    montoDescuento: promocion.montoDescuento ?? '',
    cantidadMinima: promocion.cantidadMinima ?? '',
    activa: promocion.activa
  }
  tabActiva.value = 'promociones'
}
const editarInsumo = (insumo) => {
  insumoForm.value = {
    idInsumo: insumo.idInsumo,
    nombre: insumo.nombre,
    unidadMedida: insumo.unidadMedida,
    stockActual: insumo.stockActual,
    stockMinimo: insumo.stockMinimo,
    costoUnitario: insumo.costoUnitario,
    activo: insumo.activo
  }
  movimientoForm.value.idInsumo = insumo.idInsumo
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
        <button :class="{ active: tabActiva === 'promociones' }" @click="tabActiva = 'promociones'">Promociones</button>
        <button :class="{ active: tabActiva === 'insumos' }" @click="tabActiva = 'insumos'">Insumos</button>
        <button :class="{ active: tabActiva === 'usuarios' }" @click="tabActiva = 'usuarios'">Usuarios</button>
      </div>

      <div class="admin-grid">
        <div class="panel">
          <h2>Editar {{ tabLabel }}</h2>

          <div v-if="tabActiva === 'categorias'" class="form-stack">
            <label class="field"><span>Nombre</span><input v-model="categoriaForm.nombre" type="text" /></label>
            <div class="actions"><button class="btn primary" :disabled="cargando" @click="guardarCategoria">Guardar</button><button class="btn" :disabled="cargando" @click="resetCategoriaForm">Limpiar</button></div>
          </div>

          <div v-else-if="tabActiva === 'productos'" class="form-stack">
            <label class="field"><span>Categoría</span><select v-model="productoForm.idCategoria"><option v-for="categoria in categorias" :key="categoria.idCategoria" :value="categoria.idCategoria">{{ categoria.nombre }}</option></select></label>
            <label class="field"><span>Nombre</span><input v-model="productoForm.nombre" type="text" /></label>
            <label class="field"><span>Precio</span><input v-model="productoForm.precio" type="number" min="0" step="0.01" /></label>
            <label class="field"><span>Imagen URL</span><input v-model="productoForm.imagenUrl" type="text" /></label>
            <label class="field"><span>Receta</span><textarea v-model="productoForm.recetaTexto" rows="6" placeholder="Una línea por insumo. Formato: idInsumo:cantidad"></textarea></label>
            <div class="checks"><label><input v-model="productoForm.activo" type="checkbox" /> Activo</label><label><input v-model="productoForm.esPopular" type="checkbox" /> Popular</label><label><input v-model="productoForm.esNuevo" type="checkbox" /> Nuevo</label></div>
            <div class="actions"><button class="btn primary" :disabled="cargando" @click="guardarProducto">Guardar</button><button class="btn" :disabled="cargando" @click="resetProductoForm">Limpiar</button></div>
          </div>

          <div v-else-if="tabActiva === 'modificadores'" class="form-stack">
            <label class="field"><span>Producto</span><select v-model="modificadorForm.idProducto"><option v-for="producto in productos" :key="producto.idProducto" :value="producto.idProducto">{{ producto.nombre }}</option></select></label>
            <label class="field"><span>Nombre</span><input v-model="modificadorForm.nombre" type="text" /></label>
            <label class="field"><span>Precio extra</span><input v-model="modificadorForm.precioExtra" type="number" min="0" step="0.01" /></label>
            <label class="field"><span>Receta del modificador</span><textarea v-model="modificadorForm.recetaTexto" rows="6" placeholder="Una línea por insumo. Formato: idInsumo:cantidad"></textarea></label>
            <div class="checks"><label><input v-model="modificadorForm.activo" type="checkbox" /> Activo</label></div>
            <div class="actions"><button class="btn primary" :disabled="cargando" @click="guardarModificador">Guardar</button><button class="btn" :disabled="cargando" @click="resetModificadorForm">Limpiar</button></div>
          </div>

          <div v-else-if="tabActiva === 'promociones'" class="form-stack">
            <label class="field"><span>Nombre</span><input v-model="promocionForm.nombre" type="text" /></label>
            <label class="field"><span>Descripción</span><input v-model="promocionForm.descripcion" type="text" /></label>
            <div class="two-cols">
              <label class="field"><span>Regla</span><select v-model="promocionForm.tipoRegla"><option value="PORCENTAJE">Porcentaje</option><option value="DOS_X_UNO">2x1</option><option value="EXTRA_GRATIS">Extra gratis</option><option value="DESCUENTO_HORARIO">Descuento por horario</option><option value="COMBO">Combo</option></select></label>
              <label class="field"><span>Objetivo</span><select v-model="promocionForm.tipoObjetivo"><option value="PRODUCTO">Producto</option><option value="CATEGORIA">Categoría</option><option value="COMBO">Combo</option></select></label>
            </div>
            <label class="field"><span>IDs objetivo</span><input v-model="promocionForm.idsObjetivo" type="text" placeholder="Ej. 1 o 1,2,3" /></label>
            <label class="field"><span>ID modificador gratis</span><input v-model="promocionForm.idModificadorGratis" type="number" min="1" placeholder="Solo para extra gratis" /></label>
            <div class="two-cols">
              <label class="field"><span>Fecha inicio</span><input v-model="promocionForm.fechaInicio" type="date" /></label>
              <label class="field"><span>Fecha fin</span><input v-model="promocionForm.fechaFin" type="date" /></label>
            </div>
            <div class="two-cols">
              <label class="field"><span>Hora inicio</span><input v-model="promocionForm.horaInicio" type="time" /></label>
              <label class="field"><span>Hora fin</span><input v-model="promocionForm.horaFin" type="time" /></label>
            </div>
            <div class="two-cols">
              <label class="field"><span>% descuento</span><input v-model="promocionForm.porcentajeDescuento" type="number" min="0" step="0.01" /></label>
              <label class="field"><span>Monto descuento</span><input v-model="promocionForm.montoDescuento" type="number" min="0" step="0.01" /></label>
            </div>
            <label class="field"><span>Cantidad mínima</span><input v-model="promocionForm.cantidadMinima" type="number" min="1" step="1" /></label>
            <div class="checks"><label><input v-model="promocionForm.activa" type="checkbox" /> Activa</label></div>
            <div class="actions"><button class="btn primary" :disabled="cargando" @click="guardarPromocion">Guardar</button><button class="btn" :disabled="cargando" @click="resetPromocionForm">Limpiar</button></div>
          </div>

          <div v-else-if="tabActiva === 'insumos'" class="form-stack">
            <label class="field"><span>Nombre</span><input v-model="insumoForm.nombre" type="text" /></label>
            <div class="two-cols">
              <label class="field"><span>Unidad</span><input v-model="insumoForm.unidadMedida" type="text" /></label>
              <label class="field"><span>Stock actual</span><input v-model="insumoForm.stockActual" type="number" min="0" step="0.001" /></label>
            </div>
            <div class="two-cols">
              <label class="field"><span>Stock mínimo</span><input v-model="insumoForm.stockMinimo" type="number" min="0" step="0.001" /></label>
              <label class="field"><span>Costo unitario</span><input v-model="insumoForm.costoUnitario" type="number" min="0" step="0.01" /></label>
            </div>
            <div class="checks"><label><input v-model="insumoForm.activo" type="checkbox" /> Activo</label></div>
            <div class="actions"><button class="btn primary" :disabled="cargando" @click="guardarInsumo">Guardar</button><button class="btn" :disabled="cargando" @click="resetInsumoForm">Limpiar</button></div>

            <div class="subpanel">
              <h3>Movimiento manual</h3>
              <div class="two-cols">
                <label class="field"><span>Insumo</span><select v-model="movimientoForm.idInsumo"><option v-for="insumo in insumos" :key="insumo.idInsumo" :value="insumo.idInsumo">{{ insumo.nombre }}</option></select></label>
                <label class="field"><span>Tipo</span><select v-model="movimientoForm.tipo"><option value="ENTRADA_MANUAL">Entrada manual</option><option value="MERMA">Merma</option><option value="AJUSTE">Ajuste</option></select></label>
              </div>
              <div class="two-cols">
                <label class="field"><span>{{ movimientoForm.tipo === 'AJUSTE' ? 'Stock final' : 'Cantidad' }}</span><input v-model="movimientoForm.cantidad" type="number" min="0" step="0.001" /></label>
                <label class="field"><span>Motivo</span><input v-model="movimientoForm.motivo" type="text" placeholder="Ej. compra, caducidad, conteo físico" /></label>
              </div>
              <div class="actions"><button class="btn primary" :disabled="cargando" @click="guardarMovimientoInventario">Registrar movimiento</button><button class="btn" :disabled="cargando" @click="resetMovimientoForm">Limpiar</button></div>
            </div>
          </div>

          <div v-else class="form-stack">
            <label class="field"><span>Nombre</span><input v-model="usuarioForm.nombre" type="text" /></label>
            <label class="field"><span>PIN</span><input v-model="usuarioForm.pinAcceso" type="text" placeholder="Requerido al crear; opcional al editar" /></label>
            <label class="field"><span>Rol</span><select v-model="usuarioForm.rol"><option v-for="rol in rolesDisponibles" :key="rol" :value="rol">{{ rol }}</option></select></label>
            <label class="field"><span>Avatar URL</span><input v-model="usuarioForm.avatarUrl" type="text" /></label>
            <div class="checks"><label><input v-model="usuarioForm.activo" type="checkbox" /> Activo</label></div>
            <div class="actions"><button class="btn primary" :disabled="cargando" @click="guardarUsuario">Guardar</button><button class="btn" :disabled="cargando" @click="resetUsuarioForm">Limpiar</button></div>
          </div>
        </div>

        <div class="panel">
          <h2>Listado</h2>
          <div v-if="cargando" class="empty-state">Cargando...</div>

          <div v-else-if="tabActiva === 'categorias'" class="list">
            <div v-for="categoria in categorias" :key="categoria.idCategoria" class="item"><div><strong>{{ categoria.nombre }}</strong><span>{{ categoria.totalProductos }} productos</span></div><div class="item-actions"><button class="mini-btn" @click="editarCategoria(categoria)">Editar</button><button class="mini-btn danger" @click="desactivar('categorias', categoria.idCategoria)">Eliminar</button></div></div>
          </div>

          <div v-else-if="tabActiva === 'productos'" class="list">
            <div v-for="producto in productos" :key="producto.idProducto" class="item"><div><strong>{{ producto.nombre }}</strong><span>{{ producto.categoriaNombre }} · ${{ Number(producto.precio).toFixed(2) }}</span><span>Costo estimado ${{ Number(producto.costoEstimado || 0).toFixed(2) }}</span><span>{{ producto.activo ? 'Activo' : 'Inactivo' }}</span></div><div class="item-actions"><button class="mini-btn" @click="editarProducto(producto)">Editar</button><button class="mini-btn danger" @click="desactivar('productos', producto.idProducto)">Desactivar</button></div></div>
            <div class="subpanel">
              <h3>Costos por producto</h3>
              <div v-if="!costosProducto.length" class="empty-state">Sin costos calculados.</div>
              <div v-else class="list compact-list">
                <div v-for="costo in costosProducto" :key="costo.idProducto" class="item dense"><div><strong>{{ costo.nombre }}</strong><span>{{ costo.categoria }} · Venta ${{ Number(costo.precioVenta).toFixed(2) }}</span><span>Costo estimado ${{ Number(costo.costoEstimado).toFixed(2) }}</span></div></div>
              </div>
            </div>
          </div>

          <div v-else-if="tabActiva === 'modificadores'" class="list">
            <div v-for="modificador in modificadores" :key="modificador.idModificador" class="item"><div><strong>{{ modificador.nombre }}</strong><span>{{ modificador.productoNombre }} · ${{ Number(modificador.precioExtra).toFixed(2) }}</span><span>Costo estimado ${{ Number(modificador.costoEstimado || 0).toFixed(2) }}</span><span>{{ modificador.activo ? 'Activo' : 'Inactivo' }}</span></div><div class="item-actions"><button class="mini-btn" @click="editarModificador(modificador)">Editar</button><button class="mini-btn danger" @click="desactivar('modificadores', modificador.idModificador)">Desactivar</button></div></div>
            <div class="subpanel">
              <h3>Costos por modificador</h3>
              <div v-if="!costosModificador.length" class="empty-state">Sin costos calculados.</div>
              <div v-else class="list compact-list">
                <div v-for="costo in costosModificador" :key="costo.idModificador" class="item dense"><div><strong>{{ costo.nombre }}</strong><span>{{ costo.productoNombre }} · Extra ${{ Number(costo.precioExtra).toFixed(2) }}</span><span>Costo estimado ${{ Number(costo.costoEstimado).toFixed(2) }}</span></div></div>
              </div>
            </div>
          </div>

          <div v-else-if="tabActiva === 'promociones'" class="list">
            <div v-for="promocion in promociones" :key="promocion.idPromocion" class="item"><div><strong>{{ promocion.nombre }}</strong><span>{{ promocion.tipoRegla }} · {{ promocion.tipoObjetivo }}</span><span>{{ promocion.fechaInicio }} a {{ promocion.fechaFin }}</span><span>{{ promocion.activa ? 'Activa' : 'Inactiva' }}</span></div><div class="item-actions"><button class="mini-btn" @click="editarPromocion(promocion)">Editar</button><button class="mini-btn danger" @click="desactivar('promociones', promocion.idPromocion)">Desactivar</button></div></div>
          </div>

          <div v-else-if="tabActiva === 'insumos'" class="list">
            <div v-if="alertasStockBajo.length" class="subpanel warning-panel">
              <h3>Alertas de stock bajo</h3>
              <div class="list compact-list">
                <div v-for="insumo in alertasStockBajo" :key="`alerta-${insumo.idInsumo}`" class="item dense warning-item"><div><strong>{{ insumo.nombre }}</strong><span>Stock {{ Number(insumo.stockActual).toFixed(3) }} {{ insumo.unidadMedida }}</span><span>Mínimo {{ Number(insumo.stockMinimo || 0).toFixed(3) }}</span></div></div>
              </div>
            </div>

            <div v-for="insumo in insumos" :key="insumo.idInsumo" class="item"><div><strong>{{ insumo.nombre }}</strong><span>{{ insumo.unidadMedida }} · Stock {{ Number(insumo.stockActual).toFixed(3) }}</span><span>Mínimo {{ Number(insumo.stockMinimo || 0).toFixed(3) }} · Costo ${{ Number(insumo.costoUnitario || 0).toFixed(2) }}</span><span :class="insumo.stockBajo ? 'danger-text' : ''">{{ insumo.stockBajo ? 'Stock bajo' : 'Stock OK' }}</span></div><div class="item-actions"><button class="mini-btn" @click="editarInsumo(insumo)">Editar</button><button class="mini-btn danger" @click="desactivar('insumos', insumo.idInsumo)">Desactivar</button></div></div>

            <div class="subpanel">
              <h3>Historial de movimientos</h3>
              <div v-if="!movimientosInventario.length" class="empty-state">Sin movimientos registrados.</div>
              <div v-else class="list compact-list">
                <div v-for="movimiento in movimientosInventario" :key="movimiento.idMovimientoInventario" class="item dense"><div><strong>{{ movimiento.nombreInsumo }}</strong><span>{{ movimiento.tipo }} · {{ Number(movimiento.cantidad).toFixed(3) }}</span><span>{{ Number(movimiento.stockAnterior).toFixed(3) }} -> {{ Number(movimiento.stockResultante).toFixed(3) }}</span><span>{{ movimiento.fecha?.replace('T', ' ').slice(0, 16) }} · {{ movimiento.referencia || movimiento.motivo || 'Sin referencia' }}</span></div></div>
              </div>
            </div>
          </div>

          <div v-else class="list">
            <div v-for="usuario in usuarios" :key="usuario.idUsuario" class="item"><div><strong>{{ usuario.nombre }}</strong><span>{{ usuario.rol }}</span><span>{{ usuario.activo ? 'Activo' : 'Inactivo' }}</span></div><div class="item-actions"><button class="mini-btn" @click="editarUsuario(usuario)">Editar</button><button class="mini-btn danger" @click="desactivar('usuarios', usuario.idUsuario)">Desactivar</button></div></div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-layout { width: 100%; display: flex; justify-content: center; }
.admin-card { width: min(1320px, 100%); padding: 24px; background: white; border: 4px solid black; border-radius: 24px; box-shadow: 10px 10px 0 rgba(0, 0, 0, 0.1); }
.head h1 { margin: 0; font-family: var(--font-main); color: var(--rojo-may); }
.head p { margin: 6px 0 0; color: #4b5563; }
.status { margin: 14px 0; padding: 12px 14px; border-radius: 12px; font-weight: 700; }
.status.error { background: #fee2e2; color: #991b1b; }
.status.success { background: #dcfce7; color: #166534; }
.tabs { display: flex; flex-wrap: wrap; gap: 10px; margin: 18px 0; }
.tabs button { border: 3px solid #000; border-radius: 12px; padding: 10px 14px; background: #fff; cursor: pointer; font-weight: 700; }
.tabs button.active { background: #fff1b8; }
.admin-grid { display: grid; grid-template-columns: 0.95fr 1.05fr; gap: 18px; }
.panel { border: 3px solid #000; border-radius: 18px; padding: 18px; background: #fffdf7; }
.panel h2, .subpanel h3 { margin: 0 0 12px; font-family: var(--font-main); color: var(--rojo-may); }
.form-stack, .list { display: grid; gap: 12px; }
.two-cols { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.field { display: grid; gap: 6px; }
.field span { font-weight: 700; }
.field input, .field select, .field textarea { width: 100%; padding: 12px 14px; border: 2px solid #111827; border-radius: 12px; background: #fff; resize: vertical; font: inherit; }
.checks { display: flex; flex-wrap: wrap; gap: 12px; font-weight: 700; }
.actions, .item-actions { display: flex; gap: 10px; flex-wrap: wrap; }
.btn, .mini-btn { border: 2px solid #000; border-radius: 12px; background: #fff; cursor: pointer; font-weight: 700; }
.btn { padding: 12px 14px; }
.btn.primary { background: var(--rojo-may); color: #fff; }
.mini-btn { padding: 8px 10px; }
.danger { background: #111827; color: #fff; }
.item { display: flex; justify-content: space-between; gap: 12px; align-items: center; padding: 12px 14px; border: 2px solid #000; border-radius: 14px; background: #fff; }
.item.dense { align-items: flex-start; }
.item span { display: block; margin-top: 4px; color: #4b5563; }
.subpanel { margin-top: 8px; padding: 14px; border: 2px solid #000; border-radius: 14px; background: #fff; }
.warning-panel { border-color: #b45309; background: #fffbeb; }
.warning-item { border-color: #f59e0b; }
.danger-text { color: #b91c1c; }
.compact-list { max-height: 340px; overflow: auto; }
.empty-state { padding: 18px; border: 2px dashed #d1d5db; border-radius: 14px; color: #6b7280; }
@media (max-width: 980px) {
  .admin-grid, .two-cols { grid-template-columns: 1fr; }
}
</style>
