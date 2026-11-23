USE personal_expense_tracker;

DROP TABLE IF EXISTS budgets;

CREATE TABLE budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    budget_month INT NOT NULL,
    budget_year INT NOT NULL,
    monthly_limit DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT unique_user_month_budget
        UNIQUE (user_id, budget_month, budget_year),

    CONSTRAINT chk_budget_month
        CHECK (budget_month BETWEEN 1 AND 12),

    CONSTRAINT chk_budget_limit
        CHECK (monthly_limit > 0)
);
DESCRIBE budgets;