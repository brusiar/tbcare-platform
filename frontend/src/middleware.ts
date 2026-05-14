import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
  const token = request.cookies.get('tbcare_token')?.value || 
                request.headers.get('authorization')?.replace('Bearer ', '')

  const isLoginPage = request.nextUrl.pathname === '/login'
  const isPublicPage = request.nextUrl.pathname === '/' || isLoginPage

  // Se está na página de login e tem token, redireciona para dashboard
  if (isLoginPage && token) {
    return NextResponse.redirect(new URL('/dashboard', request.url))
  }

  // Se não está em página pública e não tem token, redireciona para login
  if (!isPublicPage && !token) {
    // Verifica localStorage via header customizado (workaround)
    const hasLocalToken = request.headers.get('x-has-token') === 'true'
    if (!hasLocalToken) {
      return NextResponse.redirect(new URL('/login', request.url))
    }
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
}
