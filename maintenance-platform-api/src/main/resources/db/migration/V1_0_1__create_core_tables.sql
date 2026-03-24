CREATE TABLE buildings (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    address VARCHAR(200) NOT NULL,
    commune VARCHAR(80) NOT NULL,
    admin_user_id BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    building_id BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'RESIDENT', 'TECHNICIAN')),
    CONSTRAINT fk_users_building FOREIGN KEY (building_id) REFERENCES buildings(id)
);

ALTER TABLE buildings
    ADD CONSTRAINT fk_buildings_admin_user
    FOREIGN KEY (admin_user_id) REFERENCES users(id);

CREATE TABLE incidents (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(2500) NOT NULL,
    category VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    overdue BOOLEAN NOT NULL DEFAULT FALSE,
    resident_id BIGINT NOT NULL,
    technician_id BIGINT NULL,
    building_id BIGINT NOT NULL,
    assigned_at TIMESTAMPTZ NULL,
    resolved_at TIMESTAMPTZ NULL,
    closed_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_incidents_category CHECK (category IN ('ELECTRICIDAD', 'GASFITERIA', 'SEGURIDAD', 'LIMPIEZA', 'ASCENSOR', 'RUIDO', 'OTRO')),
    CONSTRAINT ck_incidents_priority CHECK (priority IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    CONSTRAINT ck_incidents_status CHECK (status IN ('PENDIENTE', 'ASIGNADA', 'EN_PROCESO', 'RESUELTA', 'CERRADA', 'REABIERTA', 'CANCELADA')),
    CONSTRAINT fk_incidents_resident FOREIGN KEY (resident_id) REFERENCES users(id),
    CONSTRAINT fk_incidents_technician FOREIGN KEY (technician_id) REFERENCES users(id),
    CONSTRAINT fk_incidents_building FOREIGN KEY (building_id) REFERENCES buildings(id)
);

CREATE TABLE incident_comments (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    comment_type VARCHAR(40) NOT NULL,
    message VARCHAR(2500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_incident_comments_type CHECK (comment_type IN ('NOTE', 'STATUS_CHANGE', 'SYSTEM', 'RESOLUTION_REJECTION')),
    CONSTRAINT fk_incident_comments_incident FOREIGN KEY (incident_id) REFERENCES incidents(id),
    CONSTRAINT fk_incident_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE attachments (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_attachments_incident FOREIGN KEY (incident_id) REFERENCES incidents(id),
    CONSTRAINT fk_attachments_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

CREATE TABLE incident_status_history (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    from_status VARCHAR(20) NULL,
    to_status VARCHAR(20) NOT NULL,
    changed_by BIGINT NOT NULL,
    reason VARCHAR(500) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_incident_status_history_from CHECK (from_status IS NULL OR from_status IN ('PENDIENTE', 'ASIGNADA', 'EN_PROCESO', 'RESUELTA', 'CERRADA', 'REABIERTA', 'CANCELADA')),
    CONSTRAINT ck_incident_status_history_to CHECK (to_status IN ('PENDIENTE', 'ASIGNADA', 'EN_PROCESO', 'RESUELTA', 'CERRADA', 'REABIERTA', 'CANCELADA')),
    CONSTRAINT fk_status_history_incident FOREIGN KEY (incident_id) REFERENCES incidents(id),
    CONSTRAINT fk_status_history_user FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    user_agent VARCHAR(400) NULL,
    ip_address VARCHAR(60) NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);
