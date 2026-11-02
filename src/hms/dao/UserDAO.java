package hms.dao;

import hms.db.DBConnection;
import hms.security.PasswordUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO manages staff and administrator credentials in the MySQL database.
 * Supports SHA-256 password hash verification and RBAC roles.
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
     * Authenticates credentials against the users table using SHA-256 password hash verification.
     */
    public boolean authenticate(String username, String password, String role) {
        if (!DBConnection.isConnected() || username == null || password == null) return false;

        String sql = "SELECT user_id, username, password_hash, role, full_name FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String storedRole = rs.getString("role");

                    boolean passwordMatches = PasswordUtils.verifyPassword(password.trim(), storedHash);
                    if (!passwordMatches) return false;

                    // Match role or allow Staff Officer alias for Hospital Staff
                    if (role == null || role.trim().isEmpty()) return true;
                    if (storedRole.equalsIgnoreCase(role.trim())) return true;
                    if ("Hospital Staff".equalsIgnoreCase(storedRole) && "Staff Officer".equalsIgnoreCase(role.trim())) return true;
                    if ("Staff Officer".equalsIgnoreCase(storedRole) && "Hospital Staff".equalsIgnoreCase(role.trim())) return true;
                    if ("Administrator".equalsIgnoreCase(storedRole)) return true; // Admin can log in as any role for maintenance

                    return false;
                }
            }
        } catch (SQLException ex) {
            System.err.println("[UserDAO] Authentication query error: " + ex.getMessage());
            return false;
        }
        return false;
    }

    public UserRecord findByUsername(String username) {
        if (!DBConnection.isConnected() || username == null) return null;

        String sql = "SELECT user_id, username, role, full_name FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
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

    public List<UserRecord> findAll() {
        List<UserRecord> list = new ArrayList<>();
        if (!DBConnection.isConnected()) return list;

        String sql = "SELECT user_id, username, role, full_name FROM users ORDER BY role, username";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new UserRecord(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("full_name")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("[UserDAO] findAll error: " + ex.getMessage());
        }
        return list;
    }

    public boolean saveUser(String username, String plainPassword, String role, String fullName) {
        if (!DBConnection.isConnected() || username == null || plainPassword == null) return false;

        String hash = PasswordUtils.hashPassword(plainPassword.trim());
        String sql = "INSERT INTO users (username, password_hash, role, full_name) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), role = VALUES(role), full_name = VALUES(full_name)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, hash);
            stmt.setString(3, role != null ? role.trim() : "Hospital Staff");
            stmt.setString(4, fullName != null ? fullName.trim() : username.trim());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[UserDAO] saveUser error: " + ex.getMessage());
            return false;
        }
    }
}
