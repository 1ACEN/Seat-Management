package com.booking.service;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import com.booking.exception.DatabaseException;

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

        String createTrains = "CREATE TABLE IF NOT EXISTS trains ("
                + "train_number VARCHAR(50) PRIMARY KEY,"
                + "train_name VARCHAR(255) NOT NULL,"
                + "route TEXT NOT NULL,"
                + "total_seats INT NOT NULL"
                + ") ENGINE=InnoDB;";

        String createTickets = "CREATE TABLE IF NOT EXISTS tickets ("
                + "pnr VARCHAR(50) PRIMARY KEY,"
        + "username VARCHAR(100) NOT NULL,"
        + "booked_by VARCHAR(100) DEFAULT NULL,"
                + "train_number VARCHAR(50) NOT NULL,"
                + "seat_number VARCHAR(50) NOT NULL,"
                + "travel_date VARCHAR(20) NOT NULL,"
                + "status VARCHAR(20) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB;";

        // user_history stores booking-related history only (bookings/cancellations).
        // username column was previously used for login/logout events; we no longer
        // record auth events here. Keep user_id and pnr to link history to users/tickets.
        String createUserHistory = "CREATE TABLE IF NOT EXISTS user_history (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT DEFAULT NULL," +
            "pnr VARCHAR(50) DEFAULT NULL," +
            "action VARCHAR(50) NOT NULL," +
            "details TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
            ") ENGINE=InnoDB;";

        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(createUsers);
            s.executeUpdate(createTrains);
            s.executeUpdate(createTickets);
            s.executeUpdate(createUserHistory);

            // Migration-safe checks for older setups: add missing columns/constraints
            try (ResultSet rs = s.executeQuery("SHOW COLUMNS FROM user_history LIKE 'user_id'")) {
                if (!rs.next()) {
                    s.executeUpdate("ALTER TABLE user_history ADD COLUMN user_id INT DEFAULT NULL");
                    // add FK constraint (best-effort)
                    try {
                        s.executeUpdate("ALTER TABLE user_history ADD CONSTRAINT fk_user_history_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL");
                    } catch (SQLException ignored) {
                        // ignore if unable to add FK (permissions/older MySQL)
                    }
                }
            }

            try (ResultSet rs = s.executeQuery("SHOW COLUMNS FROM user_history LIKE 'pnr'")) {
                if (!rs.next()) {
                    s.executeUpdate("ALTER TABLE user_history ADD COLUMN pnr VARCHAR(50) DEFAULT NULL");
                }
            }
            // Ensure tickets.booked_by exists for tracking who made the booking
            try (ResultSet rs = s.executeQuery("SHOW COLUMNS FROM tickets LIKE 'booked_by'")) {
                if (!rs.next()) {
                    s.executeUpdate("ALTER TABLE tickets ADD COLUMN booked_by VARCHAR(100) DEFAULT NULL");
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database schema", e);
        }
    }
}
