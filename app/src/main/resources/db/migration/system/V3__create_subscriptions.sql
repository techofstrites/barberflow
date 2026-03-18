CREATE TABLE IF NOT EXISTS public.subscriptions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL UNIQUE REFERENCES public.tenants(id),
    plan_tier VARCHAR(20) NOT NULL DEFAULT 'STARTER',
    status VARCHAR(20) NOT NULL DEFAULT 'TRIALING',
    current_period_end DATE NOT NULL,
    messages_sent_this_month INT NOT NULL DEFAULT 0,
    appointments_this_month INT NOT NULL DEFAULT 0,
    external_payment_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_tenant ON public.subscriptions(tenant_id);
CREATE INDEX idx_subscriptions_status ON public.subscriptions(status);
