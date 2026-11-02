package hms.dao;

import hms.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO manages staff and administrator credentials in the MySQL database.
 */
public class UserDAO {

    public static class UserRecord {
        private final int userId;
        private final String username;
        private final String role;
        private final String fullName;

        public UserRecord(int userId, String username, String role, String fullName) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.fullName = fullName;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getFullName() { return fullName; }
    }

    /**
     * Authenticates credentials against the users table.
     */
    public boolean authenticate(String username, String password, String role) {
        if (!DBConnection.isConnected()) return false;

        String sql = "SELECT user_id, username, role, full_name FROM users WHERE username = ? AND password_hash = ? AND role = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            System.err.println("[UserDAO] Authentication query error: " + ex.getMessage());
            return false;
        }
    }

    public UserRecord findByUsername(String username) {
        if (!DBConnection.isConnected()) return null;

        String sql = "SELECT user_id, username, role, full_name FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserRecord(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("full_name")
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("[UserDAO] findByUsername error: " + ex.getMessage());
        }
        return null;
    }
}
