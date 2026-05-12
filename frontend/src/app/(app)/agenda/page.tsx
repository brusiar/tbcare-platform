'use client'

import { useEffect, useState } from 'react'
import { api } from '@/lib/api'
import { ApiResponse, Appointment, Patient } from '@/types'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'

export default function AgendaPage() {
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [patients, setPatients] = useState<Patient[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedDate, setSelectedDate] = useState(new Date())
  const [showForm, setShowForm] = useState(false)

  useEffect(() => {
    loadData()
  }, [selectedDate])

  async function loadData() {
    try {
      setLoading(true)
      const start = new Date(selectedDate)
      start.setHours(0, 0, 0, 0)
      const end = new Date(selectedDate)
      end.setHours(23, 59, 59, 999)

      const [appointmentsRes, patientsRes] = await Promise.all([
        api.get<ApiResponse<Appointment[]>>(
          `/appointments?start=${start.toISOString()}&end=${end.toISOString()}`
        ),
        api.get<ApiResponse<Patient[]>>('/patients'),
      ])

      setAppointments(appointmentsRes.data)
      setPatients(patientsRes.data)
    } catch (error) {
      console.error('Erro ao carregar dados:', error)
    } finally {
      setLoading(false)
    }
  }

  function handlePrevDay() {
    const newDate = new Date(selectedDate)
    newDate.setDate(newDate.getDate() - 1)
    setSelectedDate(newDate)
  }

  function handleNextDay() {
    const newDate = new Date(selectedDate)
    newDate.setDate(newDate.getDate() + 1)
    setSelectedDate(newDate)
  }

  function handleToday() {
    setSelectedDate(new Date())
  }

  function getStatusBadge(status: Appointment['status']) {
    const variants: Record<Appointment['status'], 'default' | 'success' | 'warning' | 'danger'> = {
      SCHEDULED: 'default',
      CONFIRMED: 'success',
      COMPLETED: 'success',
      CANCELLED: 'danger',
      NO_SHOW: 'warning',
    }
    return variants[status]
  }

  function getStatusLabel(status: Appointment['status']) {
    const labels: Record<Appointment['status'], string> = {
      SCHEDULED: 'Agendada',
      CONFIRMED: 'Confirmada',
      COMPLETED: 'Realizada',
      CANCELLED: 'Cancelada',
      NO_SHOW: 'Faltou',
    }
    return labels[status]
  }

  if (showForm) {
    return <AppointmentForm patients={patients} onClose={() => { setShowForm(false); loadData() }} />
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Agenda</h1>
        <Button onClick={() => setShowForm(true)}>+ Nova Sessão</Button>
      </div>

      <Card>
        <div className="flex items-center justify-between mb-6">
          <div className="flex gap-2">
            <Button variant="secondary" size="sm" onClick={handlePrevDay}>
              ← Anterior
            </Button>
            <Button variant="secondary" size="sm" onClick={handleToday}>
              Hoje
            </Button>
            <Button variant="secondary" size="sm" onClick={handleNextDay}>
              Próximo →
            </Button>
          </div>
          <h2 className="text-lg font-semibold">
            {selectedDate.toLocaleDateString('pt-BR', { 
              weekday: 'long', 
              year: 'numeric', 
              month: 'long', 
              day: 'numeric' 
            })}
          </h2>
        </div>

        {loading ? (
          <div className="text-center py-12 text-gray-500">Carregando...</div>
        ) : appointments.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500 mb-4">Nenhuma sessão agendada para este dia</p>
            <Button onClick={() => setShowForm(true)}>Agendar Sessão</Button>
          </div>
        ) : (
          <div className="space-y-3">
            {appointments
              .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime())
              .map((appointment) => (
                <div
                  key={appointment.id}
                  className="flex items-center gap-4 p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
                >
                  <div className="text-sm font-medium text-gray-900 w-20">
                    {new Date(appointment.scheduledAt).toLocaleTimeString('pt-BR', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </div>
                  <div className="flex-1">
                    <div className="font-medium text-gray-900">{appointment.patientName}</div>
                    <div className="text-sm text-gray-500">{appointment.durationMin} min</div>
                  </div>
                  <Badge variant={getStatusBadge(appointment.status)}>
                    {getStatusLabel(appointment.status)}
                  </Badge>
                  {appointment.meetLink && (
                    <a
                      href={appointment.meetLink}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-sm text-blue-600 hover:underline"
                    >
                      Link da Sala
                    </a>
                  )}
                </div>
              ))}
          </div>
        )}
      </Card>
    </div>
  )
}

function AppointmentForm({ patients, onClose }: { patients: Patient[]; onClose: () => void }) {
  const [formData, setFormData] = useState({
    patientId: '',
    professionalId: '00000000-0000-0000-0000-000000000001', // Default professional
    scheduledAt: '',
    durationMin: 60,
    notes: '',
  })
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)

    try {
      await api.post('/appointments', formData)
      onClose()
    } catch (error) {
      console.error('Erro ao criar sessão:', error)
      alert('Erro ao criar sessão')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Nova Sessão</h1>
        <Button variant="secondary" onClick={onClose}>
          Cancelar
        </Button>
      </div>

      <Card>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Paciente *
            </label>
            <select
              required
              value={formData.patientId}
              onChange={(e) => setFormData({ ...formData, patientId: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">Selecione um paciente</option>
              {patients.map((patient) => (
                <option key={patient.id} value={patient.id}>
                  {patient.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Data e Hora *
            </label>
            <input
              type="datetime-local"
              required
              value={formData.scheduledAt}
              onChange={(e) => setFormData({ ...formData, scheduledAt: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Duração (minutos)
            </label>
            <input
              type="number"
              value={formData.durationMin}
              onChange={(e) => setFormData({ ...formData, durationMin: parseInt(e.target.value) })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Observações
            </label>
            <textarea
              rows={3}
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div className="flex gap-3">
            <Button type="submit" disabled={saving}>
              {saving ? 'Salvando...' : 'Agendar'}
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
