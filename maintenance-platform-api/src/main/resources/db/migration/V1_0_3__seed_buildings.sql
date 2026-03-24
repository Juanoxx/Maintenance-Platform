INSERT INTO buildings (name, address, commune, active)
VALUES
    ('Edificio Central', 'Av. Principal 123', 'Santiago Centro', TRUE),
    ('Condominio Los Robles', 'Camino Las Flores 456', 'Providencia', TRUE)
ON CONFLICT DO NOTHING;
