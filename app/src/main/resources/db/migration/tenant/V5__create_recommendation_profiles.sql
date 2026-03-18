CREATE TABLE IF NOT EXISTS recommendation_profiles (
    customer_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    predicted_next_visit DATE,
    confidence_score FLOAT NOT NULL DEFAULT 0.0,
    last_appointment_date DATE,
    avg_days_between_visits DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    retention_min_days INT NOT NULL DEFAULT 20,
    retention_max_days INT NOT NULL DEFAULT 30,
    suggested_professional_id UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rec_profiles_tenant ON recommendation_profiles(tenant_id);
CREATE INDEX idx_rec_profiles_next_visit ON recommendation_profiles(predicted_next_visit);
