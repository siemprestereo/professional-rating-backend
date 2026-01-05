-- Agregar columna para trackear el último cambio de rol
ALTER TABLE app_users ADD COLUMN last_role_switch_at TIMESTAMP NULL;

-- Inicializar con la fecha de creación para usuarios existentes
-- (asumimos que nunca cambiaron de rol)
UPDATE app_users
SET last_role_switch_at = created_at
WHERE last_role_switch_at IS NULL;