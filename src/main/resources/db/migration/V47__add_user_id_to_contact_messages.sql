ALTER TABLE contact_messages
    ADD COLUMN user_id BIGINT NULL,
    ADD CONSTRAINT fk_contact_messages_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE SET NULL;
