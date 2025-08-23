-- Add full-text search capabilities for merchant names and descriptions
CREATE INDEX idx_receipts_merchant_name_gin ON receipts USING gin(to_tsvector('english', merchant_name));
CREATE INDEX idx_receipts_description_gin ON receipts USING gin(to_tsvector('english', description));
CREATE INDEX idx_ocr_data_raw_text_gin ON ocr_data USING gin(to_tsvector('english', raw_text));