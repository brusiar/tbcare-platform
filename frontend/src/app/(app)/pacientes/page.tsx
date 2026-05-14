'use client'

import { useEffect, useState } from 'react'
import { api } from '@/lib/api'
import { ApiResponse, Patient } from '@/types'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'

export default function PacientesPage() {
  const [patients, setPatients] = useState<Patient[]>([])
  const [filteredPatients, setFilteredPatients] = useState<Patient[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingPatient, setEditingPatient] = useState<Patient | null>(null)
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    loadPatients()
  }, [])

  useEffect(() => {
    if (searchTerm) {
      const filtered = patients.filter(patient => 
        patient.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (patient.email && patient.email.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (patient.phone && patient.phone.includes(searchTerm))
      )
      setFilteredPatients(filtered)
    } else {
      setFilteredPatients(patients)
    }
  }, [searchTerm, patients])

  async function loadPatients() {
    try {
      setLoading(true)
      const response = await api.get<ApiResponse<Patient[]>>('/patients')
      setPatients(response.data)
      setFilteredPatients(response.data)
    } catch (error) {
      console.error('Erro ao carregar pacientes:', error)
    } finally {
      setLoading(false)
    }
  }

  function handleNew() {
    setEditingPatient(null)
    setShowForm(true)
  }

  function handleEdit(patient: Patient) {
    setEditingPatient(patient)
    setShowForm(true)
  }

  async function handleDelete(id: string) {
    if (!confirm('Deseja realmente desativar este paciente?')) return

    try {
      await api.delete(`/patients/${id}`)
      await loadPatients()
    } catch (error) {
      console.error('Erro ao desativar paciente:', error)
      alert('Erro ao desativar paciente')
    }
  }

  function handleFormClose() {
    setShowForm(false)
    setEditingPatient(null)
    loadPatients()
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Carregando...</div>
      </div>
    )
  }

  if (showForm) {
    return <PatientForm patient={editingPatient} onClose={handleFormClose} />
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
        <h1 className="text-2xl font-bold text-gray-900">Pacientes</h1>
        <Button onClick={handleNew}>+ Novo Paciente</Button>
      </div>

      {patients.length > 0 && (
        <div className="flex-1 w-full sm:max-w-md">
          <input
            type="text"
            placeholder="Buscar por nome, email ou telefone..."
            className="input"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      )}

      <p className="text-sm text-gray-600">
        {filteredPatients.length} paciente(s) {searchTerm && 'encontrado(s)'}
      </p>

      {filteredPatients.length === 0 ? (
        <Card>
          <div className="text-center py-12">
            <p className="text-gray-500 mb-4">
              {searchTerm ? 'Nenhum paciente encontrado' : 'Nenhum paciente cadastrado'}
            </p>
            {!searchTerm && <Button onClick={handleNew}>Cadastrar Primeiro Paciente</Button>}
          </div>
        </Card>
      ) : (
        <div className="grid gap-4">
          {filteredPatients.map((patient) => (
            <Card key={patient.id}>
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <h3 className="text-lg font-semibold text-gray-900">{patient.name}</h3>
                  <div className="mt-2 space-y-1 text-sm text-gray-600">
                    {patient.email && <div>📧 {patient.email}</div>}
                    {patient.phone && <div>📱 {patient.phone}</div>}
                    {patient.dateOfBirth && (
                      <div>🎂 {new Date(patient.dateOfBirth).toLocaleDateString('pt-BR')}</div>
                    )}
                  </div>
                  {patient.notes && (
                    <p className="mt-2 text-sm text-gray-500">{patient.notes}</p>
                  )}
                </div>
                <div className="flex gap-2">
                  <Button variant="secondary" size="sm" onClick={() => handleEdit(patient)}>
                    Editar
                  </Button>
                  <Button variant="danger" size="sm" onClick={() => handleDelete(patient.id)}>
                    Desativar
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}

function PatientForm({ patient, onClose }: { patient: Patient | null; onClose: () => void }) {
  const [formData, setFormData] = useState({
    name: patient?.name || '',
    email: patient?.email || '',
    phone: patient?.phone || '',
    dateOfBirth: patient?.dateOfBirth || '',
    notes: patient?.notes || '',
  })
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)

    try {
      if (patient) {
        await api.put(`/patients/${patient.id}`, formData)
      } else {
        await api.post('/patients', formData)
      }
      onClose()
    } catch (error) {
      console.error('Erro ao salvar paciente:', error)
      alert('Erro ao salvar paciente')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">
          {patient ? 'Editar Paciente' : 'Novo Paciente'}
        </h1>
        <Button variant="secondary" onClick={onClose}>
          Cancelar
        </Button>
      </div>

      <Card>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nome *
            </label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <input
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Telefone
            </label>
            <input
              type="tel"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Data de Nascimento
            </label>
            <input
              type="date"
              value={formData.dateOfBirth}
              onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Observações
            </label>
            <textarea
              rows={4}
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div className="flex gap-3">
            <Button type="submit" disabled={saving}>
              {saving ? 'Salvando...' : 'Salvar'}
            </Button>
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
