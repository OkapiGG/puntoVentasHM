export const ROLES = {
  ADMIN: 'ADMIN',
  GERENTE: 'GERENTE',
  CAJERO: 'CAJERO',
  REPARTIDOR: 'REPARTIDOR'
}

export function getUsuarioActual() {
  const raw = sessionStorage.getItem('usuarioActual')
  return raw ? JSON.parse(raw) : null
}

export function getRolActual() {
  return getUsuarioActual()?.rol || null
}

export function tieneAlgunRol(rolesPermitidos = []) {
  if (!rolesPermitidos.length) {
    return true
  }
  const rolActual = getRolActual()
  return rolesPermitidos.includes(rolActual)
}
