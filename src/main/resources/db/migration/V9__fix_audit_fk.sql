-- 1. Drop the existing foreign key (if it exists)
ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS audit_logs_user_id_fkey;

-- 2. Add it back with a strict CASCADE rule
ALTER TABLE audit_logs ADD CONSTRAINT audit_logs_user_id_fkey
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;