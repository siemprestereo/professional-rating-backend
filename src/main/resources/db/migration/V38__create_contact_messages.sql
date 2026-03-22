CREATE TABLE contact_messages (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    sender_name VARCHAR(255),
    sender_email VARCHAR(255),
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
