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
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
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
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'PROFESSIONAL',
    true
);
