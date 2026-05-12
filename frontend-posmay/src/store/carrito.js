import { computed, ref } from 'vue'

const items = ref([])

const crearClave = (idProducto, modificadores = []) => {
  const ids = modificadores.map(modificador => modificador.idModificador).sort((a, b) => a - b)
  return `${idProducto}:${ids.join(',')}`
}

const calcularPrecioLinea = (precioBase, modificadores = []) => {
  const extras = modificadores.reduce((acumulado, modificador) => acumulado + Number(modificador.precioExtra || 0), 0)
  return Number(precioBase) + extras
}

const totalItems = computed(() => {
  return items.value.reduce((acumulado, item) => acumulado + item.cantidad, 0)
})

const totalImporte = computed(() => {
  return items.value.reduce((acumulado, item) => acumulado + (item.precioUnitarioFinal * item.cantidad), 0)
})

export const carritoStore = {
  items,
  totalItems,
  totalImporte
}

export const agregarProducto = (producto, modificadoresSeleccionados = []) => {
  const clave = crearClave(producto.idProducto, modificadoresSeleccionados)
  const itemExistente = items.value.find(item => item.clave === clave)

  if (itemExistente) {
    itemExistente.cantidad++
    return
  }

  items.value.push({
    clave,
    idProducto: producto.idProducto,
    nombre: producto.nombre,
    precioBase: Number(producto.precio),
    precioUnitarioFinal: calcularPrecioLinea(producto.precio, modificadoresSeleccionados),
    cantidad: 1,
    modificadores: modificadoresSeleccionados.map(modificador => ({
      idModificador: modificador.idModificador,
      nombre: modificador.nombre,
      precioExtra: Number(modificador.precioExtra)
    }))
  })
}

export const incrementarItem = (clave) => {
  const itemExistente = items.value.find(item => item.clave === clave)
  if (itemExistente) {
    itemExistente.cantidad++
  }
}

export const decrementarItem = (clave) => {
  const itemExistente = items.value.find(item => item.clave === clave)
  if (!itemExistente) {
    return
  }

  if (itemExistente.cantidad === 1) {
    items.value = items.value.filter(item => item.clave !== clave)
    return
  }

  itemExistente.cantidad--
}

export const decrementarPorProducto = (idProducto) => {
  const itemExistente = [...items.value].reverse().find(item => item.idProducto === idProducto)
  if (itemExistente) {
    decrementarItem(itemExistente.clave)
  }
}

export const cantidadProducto = (idProducto) => {
  return items.value
    .filter(item => item.idProducto === idProducto)
    .reduce((acumulado, item) => acumulado + item.cantidad, 0)
}

export const limpiarCarrito = () => {
  items.value = []
}
