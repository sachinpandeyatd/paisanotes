-- 1. Add Billing Cycle days to Accounts
ALTER TABLE accounts ADD COLUMN statement_day INT;
ALTER TABLE accounts ADD COLUMN due_day INT;

-- 2. Create the Credit Card Bills table
CREATE TABLE credit_card_bills (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    billing_month VARCHAR(50) NOT NULL, -- e.g., "August 2026"
    total_billed_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    minimum_due DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID', -- UNPAID, PARTIALLY_PAID, CLEARED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);