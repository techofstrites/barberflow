CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    phone VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    preferred_professional_id UUID,
    preferred_time_of_day VARCHAR(20),
    last_appointment_date DATE,
    avg_days_between_visits DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_appointments INT NOT NULL DEFAULT 0,
    no_show_count INT NOT NULL DEFAULT 0,
    consent_given BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, phone)
);

CREATE INDEX idx_customers_tenant ON customers(tenant_id);
CREATE INDEX idx_customers_phone ON customers(phone);
