'use client'

import { Header } from '@/components/layout/Header'
import { Card } from '@/components/ui/Card'
import { api } from '@/lib/api'
import { useEffect, useState } from 'react'

interface DashboardStats {
  activePatientsCount: number
  appointmentsTodayCount: number
  appointmentsThisWeekCount: number
  pendingAppointmentsCount: number
}

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStats()
  }, [])

  async function loadStats() {
    try {
      const data = await api.get<DashboardStats>('/dashboard/stats')
      setStats(data)
    } catch (error) {
      console.error('Erro ao carregar estatísticas:', error)
    } finally {
      setLoading(false)
    }
  }

  const statsData = [
    { 
      label: 'Pacientes Ativos', 
      value: loading ? '—' : stats?.activePatientsCount.toString() || '0', 
      color: 'text-primary' 
    },
    { 
      label: 'Consultas Hoje', 
      value: loading ? '—' : stats?.appointmentsTodayCount.toString() || '0', 
      color: 'text-accent' 
    },
    { 
      label: 'Consultas na Semana', 
      value: loading ? '—' : stats?.appointmentsThisWeekCount.toString() || '0', 
      color: 'text-primary' 
    },
    { 
      label: 'Pendentes', 
      value: loading ? '—' : stats?.pendingAppointmentsCount.toString() || '0', 
      color: 'text-yellow-600' 
    },
  ]

  return (
    <>
      <Header title="Dashboard" subtitle="Visão geral da plataforma" />

      <div className="p-8 space-y-8">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {statsData.map((stat) => (
            <Card key={stat.label}>
              <p className="text-sm text-text-muted">{stat.label}</p>
              <p className={`text-3xl font-bold mt-2 ${stat.color}`}>{stat.value}</p>
            </Card>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <Card>
            <h3 className="font-semibold text-text mb-4">Próximas Consultas</h3>
            <p className="text-text-muted text-sm">Nenhuma consulta agendada.</p>
          </Card>
          <Card>
            <h3 className="font-semibold text-text mb-4">Atividade Recente</h3>
            <p className="text-text-muted text-sm">Nenhuma atividade recente.</p>
          </Card>
        </div>
      </div>
    </>
  )
}
