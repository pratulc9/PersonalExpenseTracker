
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDAO {

    public static boolean addTransaction(int userId, String type,
            String category, BigDecimal amount, String description,
            LocalDate date) throws Exception {

        validate(userId, type, category, amount, description, date);

        String sql = "INSERT INTO transactions " +
                "(user_id, type, category, amount, description, transaction_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, category.trim());
            ps.setBigDecimal(4, amount);
            ps.setString(5, description);
            ps.setDate(6, java.sql.Date.valueOf(date));

            return ps.executeUpdate() == 1;
        }
    }

    public static BigDecimal[] getDashboardTotals(int userId)
            throws Exception {

        if (userId <= 0)
            throw new IllegalArgumentException("Invalid user ID.");

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        String sql = "SELECT type, SUM(amount) total FROM transactions " +
                     "WHERE user_id = ? GROUP BY type";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("INCOME".equals(rs.getString("type")))
                        income = rs.getBigDecimal("total");
                    else if ("EXPENSE".equals(rs.getString("type")))
                        expense = rs.getBigDecimal("total");
                }
            }
        }

        return new BigDecimal[]{
                income, expense, income.subtract(expense)
        };
    }

    public static boolean deleteTransaction(int userId, int transactionId)
            throws Exception {

        if (userId <= 0 || transactionId <= 0)
            throw new IllegalArgumentException("Invalid ID.");

        String sql = "DELETE FROM transactions " +
                     "WHERE transaction_id = ? AND user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean updateTransaction(int userId, int transactionId,
            String type, String category, BigDecimal amount,
            String description, LocalDate date) throws Exception {

        if (transactionId <= 0)
            throw new IllegalArgumentException("Invalid transaction ID.");

        validate(userId, type, category, amount, description, date);

        String sql = "UPDATE transactions SET type = ?, category = ?, " +
                "amount = ?, description = ?, transaction_date = ? " +
                "WHERE transaction_id = ? AND user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, type);
            ps.setString(2, category.trim());
            ps.setBigDecimal(3, amount);
            ps.setString(4, description);
            ps.setDate(5, java.sql.Date.valueOf(date));
            ps.setInt(6, transactionId);
            ps.setInt(7, userId);

            return ps.executeUpdate() == 1;
        }
    }

    private static void validate(int userId, String type, String category,
            BigDecimal amount, String description, LocalDate date) {

        if (userId <= 0)
            throw new IllegalArgumentException("Invalid user ID.");

        if (!"INCOME".equals(type) && !"EXPENSE".equals(type))
            throw new IllegalArgumentException("Invalid type.");

        if (category == null || category.trim().isEmpty()
                || category.trim().length() > 50)
            throw new IllegalArgumentException("Invalid category.");

        if (amount == null || amount.signum() <= 0
                || amount.scale() > 2
                || amount.precision() - amount.scale() > 10)
            throw new IllegalArgumentException("Invalid amount.");

        if (description != null && description.length() > 255)
            throw new IllegalArgumentException("Description is too long.");

        if (date == null)
            throw new IllegalArgumentException("Date is required.");
    }
}
