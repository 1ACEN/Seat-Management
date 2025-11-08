package com.booking;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database implementation of {@link DatabaseProvider} that gets configuration from a .env or environment variables.
 */
public class Database implements DatabaseProvider {
    private final Dotenv dotenv = Dotenv.load();
    private final String url;
    private final String user;
    private final String password;

    public Database() {
        this.url = dotenv.get("DB_URL", "jdbc:mysql://localhost:3306/seatbooking?useSSL=false&serverTimezone=UTC");
        this.user = dotenv.get("DB_USER", "root");
        this.password = dotenv.get("DB_PASSWORD", "");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found", e);
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException("Unable to obtain database connection", e);
        }
    }

    /**
     * Initialize database schema minimal required tables.
     * Creates `users` table if it doesn't exist.
     */
    @Override
    public void init() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(100) UNIQUE NOT NULL,"
                + "password VARCHAR(255) NOT NULL,"
                + "role VARCHAR(50) NOT NULL"
                + ") ENGINE=InnoDB;";

        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(createUsers);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database schema", e);
        }
    }
}
