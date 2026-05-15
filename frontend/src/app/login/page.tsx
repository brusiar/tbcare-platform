'use client'

import { Button } from '@/components/ui/Button'
import { api } from '@/lib/api'
import { auth } from '@/lib/auth'
import { useRouter } from 'next/navigation'
import { FormEvent, useState } from 'react'

interface LoginResponse {
  token: string
  user: {
    id: string
    name: string
    email: string
    role: string
    tenantId: string
  }
}

export default function LoginPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await api.post<LoginResponse>('/auth/login', {
        email,
        password,
      })

      console.log('Login bem-sucedido:', response)
      auth.setToken(response.token)
      auth.setUser(response.user)
      console.log('Token salvo, redirecionando...')
      
      // Usar window.location para forçar redirecionamento
      window.location.href = '/dashboard'
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao fazer login')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-surface-muted flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-primary rounded-2xl mb-4">
            <span className="text-white text-2xl font-bold">TB</span>
          </div>
          <h1 className="text-2xl font-bold text-text">TB Care Platform</h1>
          <p className="text-text-muted mt-1">Acesse sua conta</p>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
          <form onSubmit={handleSubmit} className="space-y-5">
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
                {error}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-text mb-1.5">
                E-mail
              </label>
              <input
                type="email"
                placeholder="seu@email.com"
                className="input"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                disabled={loading}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text mb-1.5">
                Senha
              </label>
              <input
                type="password"
                placeholder="••••••••"
                className="input"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={loading}
              />
            </div>

            <Button type="submit" size="lg" className="w-full mt-2" disabled={loading}>
              {loading ? 'Entrando...' : 'Entrar'}
            </Button>
          </form>

          <div className="mt-6 p-4 bg-gray-50 rounded-lg">
            <p className="text-xs text-gray-600 font-medium mb-2">Usuários de desenvolvimento:</p>
            <p className="text-xs text-gray-500">Admin: admin@tbcare.com / admin123</p>
            <p className="text-xs text-gray-500">Prof: joao@tbcare.com / prof123</p>
          </div>
        </div>

        <p className="text-center text-xs text-text-light mt-6">
          TB Care Platform © {new Date().getFullYear()}
        </p>
      </div>
    </div>
  )
}
