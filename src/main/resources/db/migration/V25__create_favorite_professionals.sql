CREATE TABLE favorite_professionals (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_favorite_client FOREIGN KEY (client_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_professional FOREIGN KEY (professional_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT unique_favorite UNIQUE (client_id, professional_id)
);

CREATE INDEX idx_favorite_client ON favorite_professionals(client_id);
CREATE INDEX idx_favorite_professional ON favorite_professionals(professional_id);