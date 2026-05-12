-- TB Care Platform - Domain Initial
-- V3: Professionals, Recurrences and Appointments expansion

-- Professionals table
CREATE TABLE professionals (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    meet_link       VARCHAR(500),
    specialty       VARCHAR(255),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Appointment Recurrences table
CREATE TABLE appointment_recurrences (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    patient_id          UUID NOT NULL REFERENCES patients(id),
    professional_id     UUID NOT NULL REFERENCES professionals(id),
    recurrence_type     VARCHAR(50) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE,
    duration_min        INTEGER NOT NULL DEFAULT 60,
    time_of_day         VARCHAR(10) NOT NULL,
    day_of_week         INTEGER,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Add new columns to appointments
ALTER TABLE appointments ADD COLUMN professional_id UUID REFERENCES professionals(id);
ALTER TABLE appointments ADD COLUMN recurrence_id UUID REFERENCES appointment_recurrences(id);
ALTER TABLE appointments ADD COLUMN meet_link VARCHAR(500);

-- Migrate existing data (user_id -> professional_id)
-- For now, we'll keep user_id and add professional_id
-- In production, you'd create professionals from users first

-- Add NO_SHOW to status (already in enum, no migration needed)

-- Indexes
CREATE INDEX idx_professionals_tenant ON professionals(tenant_id);
CREATE INDEX idx_professionals_user ON professionals(user_id);
CREATE INDEX idx_recurrences_tenant ON appointment_recurrences(tenant_id);
CREATE INDEX idx_recurrences_patient ON appointment_recurrences(patient_id);
CREATE INDEX idx_recurrences_professional ON appointment_recurrences(professional_id);
CREATE INDEX idx_appointments_professional ON appointments(professional_id);
CREATE INDEX idx_appointments_recurrence ON appointments(recurrence_id);

-- Seed: Create professional for existing user
INSERT INTO professionals (id, tenant_id, user_id, meet_link, specialty, active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002',
    'https://meet.google.com/abc-defg-hij',
    'Psicologia Clínica',
    true
);
