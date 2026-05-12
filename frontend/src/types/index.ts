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
  updatedAt: string
}

export interface PatientRequest {
  name: string
  email?: string
  phone?: string
  dateOfBirth?: string
  notes?: string
}

export interface Professional {
  id: string
  userId: string
  meetLink?: string
  specialty?: string
  active: boolean
}

export interface Appointment {
  id: string
  tenantId: string
  patientId: string
  patientName?: string
  professionalId: string
  professionalName?: string
  recurrenceId?: string
  scheduledAt: string
  durationMin: number
  status: 'SCHEDULED' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW'
  meetLink?: string
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface AppointmentRequest {
  patientId: string
  professionalId: string
  scheduledAt: string
  durationMin?: number
  status?: 'SCHEDULED' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW'
  meetLink?: string
  notes?: string
  recurrenceId?: string
}

export interface User {
  id: string
  tenantId: string
  name: string
  email: string
  role: 'ADMIN' | 'PROFESSIONAL' | 'PATIENT'
  active: boolean
}
