CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE categories
(
    id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name  VARCHAR(100) UNIQUE NOT NULL,
    color VARCHAR(7),
    icon  VARCHAR(255)
);

CREATE TABLE receipts
(
    id            UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    amount        DECIMAL(10, 2) NOT NULL,
    currency      VARCHAR(3)     NOT NULL DEFAULT 'EUR',
    merchant_name VARCHAR(255),
    description   TEXT,
    date          DATE           NOT NULL,
    category_id   UUID           REFERENCES categories (id) ON DELETE SET NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255)
);

CREATE TABLE ocr_data
(
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    receipt_id         UUID      NOT NULL REFERENCES receipts (id) ON DELETE CASCADE,
    raw_text           TEXT      NOT NULL,
    confidence         DECIMAL(3, 2),
    processed_at       TIMESTAMP NOT NULL,
    extracted_fields   JSONB,
    processing_time_ms INTEGER
);


CREATE INDEX idx_receipts_date ON receipts (date);
CREATE INDEX idx_receipts_category ON receipts (category_id);
CREATE INDEX idx_receipts_created_at ON receipts (created_at);
CREATE INDEX idx_receipts_merchant_name ON receipts (merchant_name);
CREATE INDEX idx_receipts_amount ON receipts (amount);

CREATE INDEX idx_ocr_data_receipt_id ON ocr_data (receipt_id);
CREATE INDEX idx_ocr_data_confidence ON ocr_data (confidence);
CREATE INDEX idx_ocr_data_processed_at ON ocr_data (processed_at);