'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { cn } from '@/lib/utils'

const navItems = [
  { href: '/dashboard', label: 'Dashboard', icon: '⊞' },
  { href: '/agenda', label: 'Agenda', icon: '📅' },
  { href: '/pacientes', label: 'Pacientes', icon: '👥' },
]

export function Sidebar() {
  const pathname = usePathname()

  return (
    <aside className="w-64 min-h-screen bg-sidebar-bg flex flex-col">
      <div className="px-6 py-8 border-b border-sidebar-active">
        <h1 className="text-xl font-bold text-white tracking-wide">TB Care</h1>
        <p className="text-sidebar-muted text-xs mt-1">Plataforma de Saúde</p>
      </div>

      <nav className="flex-1 px-3 py-6 space-y-1">
        {navItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={cn(
              'flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors',
              pathname === item.href
                ? 'bg-sidebar-active text-white'
                : 'text-sidebar-text hover:bg-sidebar-hover hover:text-white'
            )}
          >
            <span className="text-base">{item.icon}</span>
            {item.label}
          </Link>
        ))}
      </nav>

      <div className="px-6 py-4 border-t border-sidebar-active">
        <p className="text-sidebar-muted text-xs">v0.1.0</p>
      </div>
    </aside>
  )
}
