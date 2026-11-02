package hms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection provides JDBC database connectivity using MySQL Connector/J.
 * 
 * Secure Configuration:
 * - Host: localhost (configurable via DB_HOST env)
 * - Port: 3306 (configurable via DB_PORT env)
 * - Database: hospital_oop_db (configurable via DB_NAME env)
 * - Username: hospital_app (configurable via DB_USER env)
 * - Password: Read securely from DB_PASSWORD environment variable.
 * 
 * Strict Security Guidelines:
 * - Passwords are NEVER hardcoded.
 * - Root account is NEVER used by the application.
 */
public class DBConnection {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB   = "hospital_oop_db";
    private static final String DEFAULT_USER = "hospital_app";

    private static Connection connection = null;
    private static Boolean connectionAvailable = null;
    private static String lastErrorMessage = "";

    static {
        try {
            // Load MySQL Connector/J JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Connector/J driver class not found in classpath: " + e.getMessage());
        }
    }

    public static String getHost() {
        String env = System.getenv("DB_HOST");
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_HOST;
    }

    public static String getPort() {
        String env = System.getenv("DB_PORT");
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_PORT;
    }

    public static String getDatabaseName() {
        String env = System.getenv("DB_NAME");
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_DB;
    }

    public static String getUsername() {
        String env = System.getenv("DB_USER");
        return (env != null && !env.trim().isEmpty()) ? env.trim() : DEFAULT_USER;
    }

    /**
     * Reads the database password securely from the DB_PASSWORD environment variable.
     */
    public static String getPassword() {
        String env = System.getenv("DB_PASSWORD");
        return env != null ? env : "";
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabaseName() +
               "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    /**
     * Obtains an active JDBC Connection or establishes a new one.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed() && connection.isValid(2)) {
            return connection;
        }

        String password = getPassword();
        if (password.isEmpty()) {
            lastErrorMessage = "DB_PASSWORD environment variable is not set. Please set DB_PASSWORD before connecting.";
            connectionAvailable = false;
            throw new SQLException(lastErrorMessage);
        }

        try {
            connection = DriverManager.getConnection(getJdbcUrl(), getUsername(), password);
            connectionAvailable = true;
            lastErrorMessage = "";
            System.out.println("[DBConnection] Connected successfully to MySQL database '" + getDatabaseName() + "' as user '" + getUsername() + "'.");
            return connection;
        } catch (SQLException ex) {
            connectionAvailable = false;
            lastErrorMessage = ex.getMessage();
            throw ex;
        }
    }

    /**
     * Checks whether an active database connection can be established.
     */
    public static synchronized boolean isConnected() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            connectionAvailable = false;
            return false;
        }
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Safely closes the active connection.
     */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }
}
