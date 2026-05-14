'use client'

import { Header } from '@/components/layout/Header'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { api } from '@/lib/api'
import Link from 'next/link'
import { useEffect, useState } from 'react'

interface Professional {
  id: string
  userId: string
  userName: string
  userEmail: string
  specialty: string
  meetLink: string
  active: boolean
}

export default function ProfissionaisPage() {
  const [professionals, setProfessionals] = useState<Professional[]>([])
  const [filteredProfessionals, setFilteredProfessionals] = useState<Professional[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    loadProfessionals()
  }, [])

  useEffect(() => {
    if (searchTerm) {
      const filtered = professionals.filter(prof => 
        prof.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        prof.userEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (prof.specialty && prof.specialty.toLowerCase().includes(searchTerm.toLowerCase()))
      )
      setFilteredProfessionals(filtered)
    } else {
      setFilteredProfessionals(professionals)
    }
  }, [searchTerm, professionals])

  async function loadProfessionals() {
    try {
      const data = await api.get<Professional[]>('/professionals')
      setProfessionals(data)
      setFilteredProfessionals(data)
    } catch (error) {
      console.error('Erro ao carregar profissionais:', error)
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(id: string) {
    if (!confirm('Deseja realmente desativar este profissional?')) return

    try {
      await api.delete(`/professionals/${id}`)
      loadProfessionals()
    } catch (error) {
      alert('Erro ao desativar profissional')
    }
  }

  if (loading) {
    return (
      <>
        <Header title="Profissionais" subtitle="Gestão de profissionais" />
        <div className="p-8">
          <p className="text-text-muted">Carregando...</p>
        </div>
      </>
    )
  }

  return (
    <>
      <Header title="Profissionais" subtitle="Gestão de profissionais" />

      <div className="p-8 space-y-5">
        <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
          <div className="flex-1 w-full sm:max-w-md">
            <input
              type="text"
              placeholder="Buscar por nome, email ou especialidade..."
              className="input"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <Link href="/profissionais/novo">
            <Button>+ Novo Profissional</Button>
          </Link>
        </div>

        <p className="text-sm text-text-muted">
          {filteredProfessionals.length} profissional(is) {searchTerm && 'encontrado(s)'}
        </p>

        {filteredProfessionals.length === 0 ? (
          <Card>
            <p className="text-text-muted text-center py-8">
              {searchTerm ? 'Nenhum profissional encontrado' : 'Nenhum profissional cadastrado'}
            </p>
          </Card>
        ) : (
          <div className="grid gap-4">
            {filteredProfessionals.map((prof) => (
              <Card key={prof.id}>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h3 className="font-semibold text-text">{prof.userName}</h3>
                    <p className="text-sm text-text-muted mt-1">{prof.userEmail}</p>
                    {prof.specialty && (
                      <p className="text-sm text-text-muted mt-1">
                        <span className="font-medium">Especialidade:</span> {prof.specialty}
                      </p>
                    )}
                    {prof.meetLink && (
                      <a
                        href={prof.meetLink}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-primary hover:underline mt-1 inline-block"
                      >
                        Link do Meet
                      </a>
                    )}
                  </div>
                  <div className="flex gap-2">
                    <Link href={`/profissionais/${prof.id}`}>
                      <Button variant="secondary" size="sm">
                        Editar
                      </Button>
                    </Link>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => handleDelete(prof.id)}
                    >
                      Desativar
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </>
  )
}
