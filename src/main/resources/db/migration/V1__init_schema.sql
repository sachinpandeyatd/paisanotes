-- V1__init_schema.sql

-- Enable UUID extension in PostgreSQL (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. USERS TABLE
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. PEOPLE TABLE (Financial CRM)
CREATE TABLE people (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 3. TRANSACTIONS TABLE
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(12, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE
    merchant VARCHAR(255),
    category VARCHAR(100) NOT NULL,
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- CASH, UPI, CC, DC, NETBANKING
    source VARCHAR(50) NOT NULL, -- MANUAL, NOTIFICATION, EMI_AUTO, LOAN_REPAYMENT
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 4. EMIS TABLE
CREATE TABLE emis (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    person_id UUID REFERENCES people(id) ON DELETE SET NULL, -- Nullable if it's user's own EMI
    ref_number VARCHAR(100),
    item_name VARCHAR(255) NOT NULL,
    owner_type VARCHAR(20) NOT NULL, -- ME, FRIEND
    principal_amount DECIMAL(12, 2) NOT NULL,
    monthly_emi_amount DECIMAL(12, 2) NOT NULL,
    total_months INT NOT NULL,
    start_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CLOSED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 5. LOANS TABLE
CREATE TABLE loans (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES people(id) ON DELETE CASCADE,
    amount_lent DECIMAL(12, 2) NOT NULL,
    date_given DATE NOT NULL,
    expected_return_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CLOSED
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 6. INSTALLMENTS TABLE (Payment History)
CREATE TABLE installments (
    id UUID PRIMARY KEY,
    reference_type VARCHAR(20) NOT NULL, -- EMI, LOAN
    reference_id UUID NOT NULL,
    transaction_id UUID REFERENCES transactions(id) ON DELETE SET NULL, -- Proof of payment
    amount_paid DECIMAL(12, 2) NOT NULL,
    payment_date TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 7. AUDIT LOGS TABLE
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL, -- TRANSACTION, EMI, LOAN, PERSON
    entity_id UUID NOT NULL,
    action_type VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE, RESTORE
    metadata JSONB, -- Critical: JSONB for flexible before/after states
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. PARSING RULES TABLE (Backend Only - Auto Increment ID)
CREATE TABLE parsing_rules (
    id SERIAL PRIMARY KEY,
    package_name VARCHAR(255) NOT NULL,
    regex_pattern VARCHAR(1000) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    version INT NOT NULL DEFAULT 1
);

-- CREATE PERFORMANCE INDEXES
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transactions_is_deleted ON transactions(is_deleted);
CREATE INDEX idx_people_user_id ON people(user_id);
CREATE INDEX idx_audit_logs_user_entity ON audit_logs(user_id, entity_type, entity_id);

-- Create a GIN Index for the JSONB metadata column for fast searching inside JSON
CREATE INDEX idx_audit_logs_metadata ON audit_logs USING GIN (metadata);