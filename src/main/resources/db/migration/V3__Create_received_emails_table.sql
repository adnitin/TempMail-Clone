CREATE TABLE received_emails (
    mail_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_id UUID NOT NULL REFERENCES temp_emails(email_id) ON DELETE CASCADE,
    sender VARCHAR(255) NOT NULL,
    subject VARCHAR(512),
    body TEXT,
    received_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_received_email FOREIGN KEY(email_id) REFERENCES temp_emails(email_id)
);

CREATE INDEX idx_received_emails_email_id ON received_emails(email_id);
CREATE INDEX idx_received_emails_received_at ON received_emails(received_at);