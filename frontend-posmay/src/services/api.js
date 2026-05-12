const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

function buildHeaders(extraHeaders = {}) {
  const raw = sessionStorage.getItem('usuarioActual')
  const usuarioActual = raw ? JSON.parse(raw) : null
  const headers = { ...extraHeaders }

  if (usuarioActual?.idUsuario) {
    headers['X-User-Id'] = String(usuarioActual.idUsuario)
  }

  return headers
}

async function parseResponse(response) {
  if (response.ok) {
    return response.status === 204 ? null : response.json()
  }

  const message = await response.text()
  throw new Error(message || 'Error al consumir la API')
}

export async function apiGet(path) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: buildHeaders()
  })
  return parseResponse(response)
}

export async function apiPost(path, payload) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: buildHeaders({
      'Content-Type': 'application/json'
    }),
    body: JSON.stringify(payload)
  })

  return parseResponse(response)
}

export async function apiPut(path, payload) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'PUT',
    headers: buildHeaders({
      'Content-Type': 'application/json'
    }),
    body: JSON.stringify(payload)
  })

  return parseResponse(response)
}

export async function apiDelete(path) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'DELETE',
    headers: buildHeaders()
  })

  return parseResponse(response)
}
