-- Snapshot del nombre del cliente en ratings, para conservar la calificación
-- cuando la cuenta del cliente se elimina (FK client_id ON DELETE SET NULL, ver V57).

ALTER TABLE ratings ADD COLUMN client_name VARCHAR(100) NULL;

-- Backfill de los tres snapshots donde todavía estén vacíos
UPDATE ratings r JOIN app_users u ON u.id = r.client_id
SET r.client_name = LEFT(u.name, 100)
WHERE r.client_name IS NULL;

UPDATE ratings r JOIN app_users u ON u.id = r.professional_id
SET r.professional_name = LEFT(u.name, 100)
WHERE r.professional_name IS NULL;

UPDATE ratings r JOIN businesses b ON b.id = r.business_id
SET r.business_name = LEFT(b.name, 100)
WHERE r.business_name IS NULL;
