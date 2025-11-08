package com.booking;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple Database utility to get JDBC connections.
 * Reads configuration from environment variables:
 *  - DB_URL (default: jdbc:mysql://localhost:3306/seatbooking?useSSL=false&serverTimezone=UTC)
 *  - DB_USER (default: root)
 *  - DB_PASSWORD (default: empty)
 */
public class Database {
       private static final Dotenv dotenv = Dotenv.load();

    private static final String url = dotenv.get("DB_URL", "jdbc:mysql://localhost:3306/seatbooking?useSSL=false&serverTimezone=UTC");
    private static final String user = dotenv.get("DB_USER", "root");
    private static final String password = dotenv.get("DB_PASSWORD", "");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Initialize database schema minimal required tables.
     * Creates `users` table if it doesn't exist.
     */
    public static void init() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(100) UNIQUE NOT NULL,"
                + "password VARCHAR(255) NOT NULL,"
                + "role VARCHAR(50) NOT NULL"
                + ") ENGINE=InnoDB;";

        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(createUsers);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}
