ALTER TABLE ratings
  ADD COLUMN professional_name VARCHAR(100) NULL,
  ADD COLUMN business_name VARCHAR(100) NULL,
  MODIFY COLUMN professional_id BIGINT NULL;

-- Poblar los campos con datos actuales
UPDATE ratings r
JOIN app_users u ON r.professional_id = u.id
SET r.professional_name = u.name;

UPDATE ratings r
JOIN businesses b ON r.business_id = b.id
SET r.business_name = b.name;