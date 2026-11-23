
import java.sql.*;
import java.math.BigDecimal;
import java.time.YearMonth;

public class BudgetDAO {

    // Save or update monthly budget
    public static boolean saveBudget(int userId, int month, int year,
            BigDecimal limit) throws Exception {

        validate(userId, month, year);

        if (limit == null || limit.signum() <= 0 ||
                limit.scale() > 2 ||
                limit.precision() - limit.scale() > 10)
            throw new IllegalArgumentException("Invalid budget amount.");

        String sql = "INSERT INTO budgets " +
                "(user_id, budget_month, budget_year, monthly_limit) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE monthly_limit = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ps.setBigDecimal(4, limit);
            ps.setBigDecimal(5, limit);

            ps.executeUpdate();
            return true;
        }
    }

    // Get budget for selected month
    public static BigDecimal getBudget(int userId, int month, int year)
            throws Exception {

        validate(userId, month, year);

        String sql = "SELECT monthly_limit FROM budgets " +
                "WHERE user_id = ? AND budget_month = ? AND budget_year = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
        }
    }

    // Calculate monthly expenses
    public static BigDecimal getMonthlyExpenses(
            int userId, int month, int year) throws Exception {

        validate(userId, month, year);

        YearMonth ym = YearMonth.of(year, month);

        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE user_id = ? AND type = 'EXPENSE' " +
                "AND transaction_date >= ? AND transaction_date < ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(ym.atDay(1)));
            ps.setDate(3, java.sql.Date.valueOf(
                    ym.plusMonths(1).atDay(1)));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    // Shared validation
    private static void validate(int userId, int month, int year) {
        if (userId <= 0 || month < 1 || month > 12 ||
                year < 2000 || year > 2100)
            throw new IllegalArgumentException(
                    "Invalid user, month or year.");
    }
}
