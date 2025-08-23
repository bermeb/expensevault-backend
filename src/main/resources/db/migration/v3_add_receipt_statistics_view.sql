-- Create a view for receipt statistics
CREATE
OR REPLACE VIEW receipt_statistics AS
SELECT DATE_TRUNC('month', r.date) as month,
    c.name as category_name,
    COUNT(*) as receipt_count,
    SUM(r.amount) as total_amount,
    AVG(r.amount) as average_amount,
    MIN(r.amount) as min_amount,
    MAX(r.amount) as max_amount
FROM receipts r
    LEFT JOIN categories c
ON r.category_id = c.id
GROUP BY DATE_TRUNC('month', r.date), c.name
ORDER BY month DESC, total_amount DESC;

-- Create a view for monthly totals
CREATE
OR REPLACE VIEW monthly_expense_totals AS
SELECT DATE_TRUNC('month', date) as month,
    COUNT(*) as receipt_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount
FROM receipts
GROUP BY DATE_TRUNC('month', date)
ORDER BY month DESC;