ALTER TABLE emis ADD COLUMN total_amount_with_interest DECIMAL(12, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE emis ADD COLUMN interest_rate DECIMAL(5, 2); -- Optional, e.g., 12.50
ALTER TABLE emis ADD COLUMN amount_paid DECIMAL(12, 2) NOT NULL DEFAULT 0.00;

UPDATE emis SET total_amount_with_interest = (principal_amount);