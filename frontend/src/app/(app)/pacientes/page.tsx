import { Header } from '@/components/layout/Header'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'

export default function PacientesPage() {
  return (
    <>
      <Header title="Pacientes" subtitle="Gerenciamento de pacientes" />

      <div className="p-8">
        <Card>
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-semibold text-text">Lista de Pacientes</h3>
            <Button size="sm">+ Novo Paciente</Button>
          </div>
          <p className="text-text-muted text-sm">Nenhum paciente cadastrado.</p>
        </Card>
      </div>
    </>
  )
}
