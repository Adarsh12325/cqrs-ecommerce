DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    items_json TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO products (name, category, price) VALUES 
('iPhone 15', 'Electronics', 999.99),
('MacBook Pro', 'Electronics', 1999.99),
('T-Shirt', 'Clothing', 29.99),
('Jeans', 'Clothing', 89.99)
ON CONFLICT DO NOTHING;
