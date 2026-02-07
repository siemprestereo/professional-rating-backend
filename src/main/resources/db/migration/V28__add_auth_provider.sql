-- Agregar columna auth_provider a app_users
ALTER TABLE app_users
ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL' AFTER email;

-- Marcar usuarios existentes con Google OAuth
UPDATE app_users
SET auth_provider = 'GOOGLE'
WHERE oauth_id IS NOT NULL;