CREATE TABLE revenue
(
    id            UUID PRIMARY KEY DEFAULT Gen_random_uuid(),
    location_id   UUID NOT NULL REFERENCES locations(id),
    date          DATE NOT NULL,
    sum           DECIMAL(12, 2) NOT NULL,
    calculated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_revenue_location_date UNIQUE (location_id, date)
);

CREATE INDEX idx_orders_created_at ON orders (created_at);
