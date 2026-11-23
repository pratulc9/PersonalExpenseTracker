
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    // Register a new user
    public static boolean registerUser(
            String name, String email, char[] password)
            throws Exception {

        String sql = "INSERT INTO users " +
                "(full_name, email, password_hash) " +
                "VALUES (?, ?, ?)";

        String hashedPassword =
                PasswordUtil.hashPassword(password);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);

            return ps.executeUpdate() == 1;
        }
    }

    // Login user and return their user ID
    public static int loginUser(
            String email, char[] password)
            throws Exception {

        String sql = "SELECT user_id, password_hash " +
                "FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    String storedHash =
                            rs.getString("password_hash");

                    if (PasswordUtil.verifyPassword(
                            password, storedHash)) {

                        return rs.getInt("user_id");
                    }
                }
            }
        }

        // Invalid email or password
        return -1;
    }
}
