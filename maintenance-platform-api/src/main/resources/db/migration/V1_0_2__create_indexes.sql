CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_building ON users(building_id);

CREATE INDEX idx_incidents_building_status ON incidents(building_id, status);
CREATE INDEX idx_incidents_technician_status ON incidents(technician_id, status);
CREATE INDEX idx_incidents_priority ON incidents(priority);
CREATE INDEX idx_incidents_category ON incidents(category);
CREATE INDEX idx_incidents_created_at ON incidents(created_at DESC);
CREATE INDEX idx_incidents_status_overdue_created_at ON incidents(status, overdue, created_at);

CREATE INDEX idx_incident_comments_incident_created_at ON incident_comments(incident_id, created_at);
CREATE INDEX idx_attachments_incident_created_at ON attachments(incident_id, created_at);
CREATE INDEX idx_status_history_incident_created_at ON incident_status_history(incident_id, created_at);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
