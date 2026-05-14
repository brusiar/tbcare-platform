'use client'

import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { api } from '@/lib/api'
import { useRouter, useParams } from 'next/navigation'
import { FormEvent, useEffect, useState } from 'react'

interface User {
  id: string
  name: string
  email: string
}

interface Professional {
  id: string
  userId: string
  specialty: string
  meetLink: string
}

export default function ProfissionalFormPage() {
  const router = useRouter()
  const params = useParams()
  const isEdit = params?.id && params.id !== 'novo'

  const [users, setUsers] = useState<User[]>([])
  const [userId, setUserId] = useState('')
  const [specialty, setSpecialty] = useState('')
  const [meetLink, setMeetLink] = useState('')
  const [loading, setLoading] = useState(false)
  const [loadingData, setLoadingData] = useState(isEdit)

  useEffect(() => {
    loadUsers()
    if (isEdit) {
      loadProfessional()
    }
  }, [])

  async function loadUsers() {
    try {
      const data = await api.get<User[]>('/users')
      setUsers(data)
    } catch (error) {
      console.error('Erro ao carregar usuários:', error)
    }
  }

  async function loadProfessional() {
    try {
      const data = await api.get<Professional>(`/professionals/${params.id}`)
      setUserId(data.userId)
      setSpecialty(data.specialty || '')
      setMeetLink(data.meetLink || '')
    } catch (error) {
      alert('Erro ao carregar profissional')
      router.push('/profissionais')
    } finally {
      setLoadingData(false)
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)

    try {
      const payload = { userId, specialty, meetLink }

      if (isEdit) {
        await api.put(`/professionals/${params.id}`, payload)
      } else {
        await api.post('/professionals', payload)
      }

      router.push('/profissionais')
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao salvar profissional')
    } finally {
      setLoading(false)
    }
  }

  if (loadingData) {
    return (
      <>
        <Header
          title={isEdit ? 'Editar Profissional' : 'Novo Profissional'}
          subtitle="Gestão de profissionais"
        />
        <div className="p-8">
          <p className="text-text-muted">Carregando...</p>
        </div>
      </>
    )
  }

  return (
    <>
      <Header
        title={isEdit ? 'Editar Profissional' : 'Novo Profissional'}
        subtitle="Gestão de profissionais"
      />

      <div className="p-8">
        <Card className="max-w-2xl">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-text mb-1.5">
                Usuário *
              </label>
              <select
                className="input"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                required
                disabled={isEdit || loading}
              >
                <option value="">Selecione um usuário</option>
                {users.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.name} ({user.email})
                  </option>
                ))}
              </select>
              {isEdit && (
                <p className="text-xs text-text-muted mt-1">
                  O usuário não pode ser alterado após criação
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-text mb-1.5">
                Especialidade
              </label>
              <input
                type="text"
                className="input"
                placeholder="Ex: Psicologia Clínica"
                value={specialty}
                onChange={(e) => setSpecialty(e.target.value)}
                disabled={loading}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text mb-1.5">
                Link do Google Meet
              </label>
              <input
                type="url"
                className="input"
                placeholder="https://meet.google.com/..."
                value={meetLink}
                onChange={(e) => setMeetLink(e.target.value)}
                disabled={loading}
              />
            </div>

            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={loading}>
                {loading ? 'Salvando...' : 'Salvar'}
              </Button>
              <Button
                type="button"
                variant="secondary"
                onClick={() => router.push('/profissionais')}
                disabled={loading}
              >
                Cancelar
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </>
  )
}
