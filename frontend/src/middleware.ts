import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
  // Middleware simplificado - a autenticação é feita no cliente
  // O redirecionamento pós-login é feito via window.location.href
  
  const isLoginPage = request.nextUrl.pathname === '/login'
  
  // Apenas permite acesso a todas as páginas
  // A verificação de autenticação é feita nos componentes via auth.isAuthenticated()
  return NextResponse.next()
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
}
