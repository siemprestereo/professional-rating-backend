UPDATE cvs SET public_slug = UUID() WHERE public_slug IS NULL;

ALTER TABLE cvs MODIFY COLUMN public_slug VARCHAR(64) NOT NULL;
