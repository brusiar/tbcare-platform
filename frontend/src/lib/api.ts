import { auth } from './auth'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://ademir:8081/api'

if (typeof window !== 'undefined') {
  console.log('API_URL:', API_URL)
}

interface ApiResponse<T> {
  success: boolean
  data: T
  message: string | null
  timestamp: string
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = auth.getToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  if (options?.headers) {
    Object.assign(headers, options.headers)
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  })

  if (response.status === 401) {
    auth.logout()
    if (typeof window !== 'undefined') {
      window.location.href = '/login'
    }
    throw new Error('Não autorizado')
  }

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Erro na requisição' }))
    throw new Error(error.message || `HTTP ${response.status}`)
  }

  const apiResponse: ApiResponse<T> = await response.json()
  return apiResponse.data
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (path: string) => request(path, { method: 'DELETE' }),
}
