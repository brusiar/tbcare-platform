import { Header } from '@/components/layout/Header'
import { Card } from '@/components/ui/Card'

const stats = [
  { label: 'Pacientes Ativos', value: '—', color: 'text-primary' },
  { label: 'Consultas Hoje', value: '—', color: 'text-accent' },
  { label: 'Consultas na Semana', value: '—', color: 'text-primary' },
  { label: 'Pendentes', value: '—', color: 'text-yellow-600' },
]

export default function DashboardPage() {
  return (
    <>
      <Header title="Dashboard" subtitle="Visão geral da plataforma" />

      <div className="p-8 space-y-8">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {stats.map((stat) => (
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
