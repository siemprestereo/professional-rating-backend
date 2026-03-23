ALTER TABLE ratings DROP FOREIGN KEY fk_ratings_client;
ALTER TABLE ratings DROP FOREIGN KEY fk_ratings_professional;

ALTER TABLE ratings ADD CONSTRAINT fk_ratings_client
    FOREIGN KEY (client_id) REFERENCES app_users(id) ON DELETE SET NULL;

ALTER TABLE ratings ADD CONSTRAINT fk_ratings_professional
    FOREIGN KEY (professional_id) REFERENCES app_users(id) ON DELETE SET NULL;
