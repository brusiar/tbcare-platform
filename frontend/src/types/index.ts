export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  timestamp: string
}

export interface Tenant {
  id: string
  name: string
  slug: string
  active: boolean
  createdAt: string
}

export interface Patient {
  id: string
  tenantId: string
  name: string
  dateOfBirth?: string
  phone?: string
  email?: string
  notes?: string
  active: boolean
  createdAt: string
}

export interface Appointment {
  id: string
  tenantId: string
  patientId: string
  userId: string
  scheduledAt: string
  durationMin: number
  status: 'SCHEDULED' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED'
  notes?: string
  createdAt: string
}

export interface User {
  id: string
  tenantId: string
  name: string
  email: string
  role: 'ADMIN' | 'USER'
  active: boolean
}
