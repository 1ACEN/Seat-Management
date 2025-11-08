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

        // Create trains table: store route as comma-separated values and total_seats
        String createTrains = "CREATE TABLE IF NOT EXISTS trains ("
                + "train_number VARCHAR(50) PRIMARY KEY,"
                + "train_name VARCHAR(255) NOT NULL,"
                + "route TEXT NOT NULL,"
                + "total_seats INT NOT NULL"
                + ") ENGINE=InnoDB;";

        // Create tickets table: store PNR, username, train_number, seat_number, travel_date and status
        String createTickets = "CREATE TABLE IF NOT EXISTS tickets ("
                + "pnr VARCHAR(50) PRIMARY KEY,"
                + "username VARCHAR(100) NOT NULL,"
                + "train_number VARCHAR(50) NOT NULL,"
                + "seat_number VARCHAR(50) NOT NULL,"
                + "travel_date VARCHAR(20) NOT NULL,"
                + "status VARCHAR(20) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB;";

    // Create user_history table to record login/register/logout events
    String createUserHistory = "CREATE TABLE IF NOT EXISTS user_history ("
        + "id INT AUTO_INCREMENT PRIMARY KEY,"
        + "username VARCHAR(100) NOT NULL,"
        + "action VARCHAR(50) NOT NULL,"
        + "details TEXT,"
        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
        + ") ENGINE=InnoDB;";

        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(createUsers);
            s.executeUpdate(createTrains);
            s.executeUpdate(createTickets);
            s.executeUpdate(createUserHistory);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}

