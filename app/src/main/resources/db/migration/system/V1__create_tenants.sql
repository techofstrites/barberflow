CREATE TABLE IF NOT EXISTS public.tenants (
    id UUID PRIMARY KEY,
    slug VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    whatsapp_phone_number_id VARCHAR(100),
    whatsapp_access_token TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ONBOARDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenants_slug ON public.tenants(slug);
