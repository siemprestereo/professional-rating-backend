CREATE TABLE email_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    type            VARCHAR(20)  NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    recipient_email VARCHAR(255),
    recipient_name  VARCHAR(255),
    target_role     VARCHAR(20),
    sender_alias    VARCHAR(100) NOT NULL,
    body_preview    VARCHAR(300) NOT NULL,
    recipients_count INT         NOT NULL DEFAULT 1,
    sent_at         DATETIME     NOT NULL
);
