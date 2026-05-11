import { Button } from '@/components/ui/Button'

export default function LoginPage() {
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
          <form className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-text mb-1.5">
                E-mail
              </label>
              <input
                type="email"
                placeholder="seu@email.com"
                className="input"
                autoComplete="email"
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
              />
            </div>

            <Button type="submit" size="lg" className="w-full mt-2">
              Entrar
            </Button>
          </form>
        </div>

        <p className="text-center text-xs text-text-light mt-6">
          TB Care Platform © {new Date().getFullYear()}
        </p>
      </div>
    </div>
  )
}
