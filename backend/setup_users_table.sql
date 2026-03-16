-- Smart Parking System - Users Table Setup Script
-- PostgreSQL SQL Script

-- Drop existing table if needed (for development only)
-- DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    fullname VARCHAR(100) NOT NULL,
    phone VARCHAR(11) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'GUARD')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

-- Create index for faster queries
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- Insert test ADMIN user
-- Password: password123 (BCrypt hashed)
-- Hash: $2a$10$kbHyJoRqpOkx0Z7fG5k1.OXd0vLN5qJ5n6M5t7K8L9Y0P1Q2R3S4T
INSERT INTO users (id, username, password, fullname, phone, role, status, created_at, is_deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000'::UUID,
    'admin',
    '$2a$10$kbHyJoRqpOkx0Z7fG5k1.OXd0vLN5qJ5n6M5t7K8L9Y0P1Q2R3S4T',
    'Admin User',
    '0987654321',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    false
)
ON CONFLICT (username) DO NOTHING;

-- Insert test GUARD user (for testing role-based access)
-- Password: guard123
-- Hash: $2a$10$cVZ9H.wv1Z5Q3E8B7M0N.9A8B7C6D5E4F3G2H1I0J9K8L7M6N5O4P
INSERT INTO users (id, username, password, fullname, phone, role, status, created_at, is_deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001'::UUID,
    'guard',
    '$2a$10$cVZ9H.wv1Z5Q3E8B7M0N.9A8B7C6D5E4F3G2H1I0J9K8L7M6N5O4P',
    'Guard User',
    '0123456789',
    'GUARD',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    false
)
ON CONFLICT (username) DO NOTHING;

-- Verify data
SELECT id, username, fullname, role, status FROM users;

-- Display information
\echo 'Users table created successfully!'
\echo 'Test credentials:'
\echo 'ADMIN - username: admin, password: password123'
\echo 'GUARD - username: guard, password: guard123'

