CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    state VARCHAR(30) NOT NULL DEFAULT 'IDLE',
    context TEXT NOT NULL DEFAULT '{}',
    message_history TEXT NOT NULL DEFAULT '[]',
    unrecognized_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_conversations_tenant ON conversations(tenant_id);
CREATE INDEX idx_conversations_phone ON conversations(customer_phone, tenant_id);
CREATE INDEX idx_conversations_state ON conversations(state);
