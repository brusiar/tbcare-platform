import { Header } from '@/components/layout/Header'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'

export default function AgendaPage() {
  return (
    <>
      <Header title="Agenda" subtitle="Gerenciamento de consultas" />

      <div className="p-8">
        <Card>
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-semibold text-text">Consultas</h3>
            <Button size="sm">+ Nova Consulta</Button>
          </div>
          <p className="text-text-muted text-sm">Nenhuma consulta encontrada.</p>
        </Card>
      </div>
    </>
  )
}
