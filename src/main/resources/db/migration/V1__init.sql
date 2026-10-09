CREATE TABLE users
(
    id            UUID PRIMARY KEY DEFAULT Gen_random_uuid(),
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    username      VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    role          VARCHAR(50) NOT NULL
);

CREATE TABLE addresses
(
    id             UUID PRIMARY KEY DEFAULT Gen_random_uuid(),
    user_id        UUID REFERENCES users(id),
    country        VARCHAR(100) NOT NULL,
    city           VARCHAR(100) NOT NULL,
    county         VARCHAR(100) NOT NULL,
    street_address VARCHAR(255) NOT NULL
);

CREATE TABLE product_categories
(
    id          UUID PRIMARY KEY DEFAULT Gen_random_uuid(),
    NAME        VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE products
(
    id          UUID PRIMARY KEY DEFAULT Gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES product_categories(id),
    NAME        VARCHAR(255) NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    weight      DOUBLE PRECISION
);

CREATE TABLE product_details
(
    product_id  UUID PRIMARY KEY REFERENCES products(id),
    description TEXT,
    image       BYTEA
);

CREATE TABLE locations
(
    id         UUID PRIMARY KEY DEFAULT Gen_random_uuid(),
    NAME       VARCHAR(255) NOT NULL,
    address_id UUID NOT NULL REFERENCES addresses(id)
);

CREATE TABLE stocks
(
    product_id  UUID REFERENCES products(id),
    location_id UUID REFERENCES locations(id),
    quantity    INTEGER NOT NULL,
    PRIMARY KEY (product_id, location_id)
);

CREATE TABLE orders
(
    id                  UUID PRIMARY KEY DEFAULT Gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id),
    delivery_address_id UUID NOT NULL REFERENCES addresses(id),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_details
(
    order_id        UUID REFERENCES orders(id),
    product_id      UUID REFERENCES products(id),
    shipped_from_id UUID REFERENCES locations(id),
    quantity        INTEGER NOT NULL,
    purchased_price DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (order_id, product_id)
);