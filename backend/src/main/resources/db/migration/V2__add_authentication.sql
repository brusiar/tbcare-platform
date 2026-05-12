-- TB Care Platform - Authentication & Multi-Tenant
-- V2: Add authentication fields and seed data

-- Add password_hash to users table
ALTER TABLE users ADD COLUMN password_hash VARCHAR(255);

-- Create index for email lookup (authentication)
CREATE INDEX idx_users_email ON users(email);

-- Seed: Create admin user for default tenant
-- Password: admin123 (hashed with BCrypt)
INSERT INTO users (id, tenant_id, name, email, password_hash, role, active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'Admin User',
    'admin@tbcare.com',
    '$2a$10$ZH381I8LXmzyZCKlEny0..ZDmrsYx.3B6uELYz0BVRbQyXE46yXtG',
    'ADMIN',
    true
);

-- Seed: Create professional user for default tenant
-- Password: prof123
INSERT INTO users (id, tenant_id, name, email, password_hash, role, active)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    'Dr. João Silva',
    'joao@tbcare.com',
    '$2a$10$kVLO3FxP6juwq0VcFux.RO8FWc5vYeKtFBpq7NvwyF2jLSWVEPOOa',
    'PROFESSIONAL',
    true
);
