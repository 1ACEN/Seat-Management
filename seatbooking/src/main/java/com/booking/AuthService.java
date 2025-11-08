package com.booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthService backed by a MySQL database (users table).
 *
 * Notes:
 * - Passwords are stored in plaintext for simplicity (keep as-is to match existing design).
 * - In production, always store salted hashes.
 */
public class AuthService implements AuthProvider {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    private final DatabaseProvider db;

    public AuthService(DatabaseProvider db) {
        this.db = db;
        // Initialize DB schema and ensure default admin exists
        try {
            this.db.init();
        } catch (DatabaseException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            throw new AuthException("Database initialization failed", e);
        }

        try {
            if (findUserByUsername("admin") == null) {
                try (Connection c = this.db.getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO users(username, password, role) VALUES(?,?,?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.setString(3, Role.ADMIN.name());
                    ps.executeUpdate();
                    System.out.println("Created default admin user (admin/admin123)");
                }
            }
        } catch (DatabaseException | SQLException e) {
            // If another process created it concurrently, ignore duplicate key otherwise log
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (!msg.contains("duplicate") && !msg.contains("unique")) {
                LOGGER.log(Level.SEVERE, "Failed to ensure default admin user", e);
                throw new AuthException("Failed to ensure default admin user", e);
            }
        }
    }

    /**
     * Checks if a user with this username already exists.
     * @param username The username to check.
     * @return The User object if found, null otherwise.
     */
    private User findUserByUsername(String username) {
        String sql = "SELECT username, password, role FROM users WHERE username = ?";
        try (Connection c = this.db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String u = rs.getString("username");
                    String p = rs.getString("password");
                    Role r = Role.valueOf(rs.getString("role"));
                    return new User(u, p, r);
                }
            }
        } catch (DatabaseException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying user by username", e);
            throw new AuthException("Database error while finding user", e);
        }
        return null;
    }

    /**
     * Registers a new user (as a PASSENGER only).
     * @param username The desired username.
     * @param password The desired password.
     * @return true if registration was successful, false if username already exists.
     */
    public boolean register(String username, String password) {
        try {
            if (findUserByUsername(username) != null) {
                System.out.println("Error: Username already exists. Please try another.");
                return false;
            }

            String sql = "INSERT INTO users(username, password, role) VALUES(?,?,?)";
            try (Connection c = this.db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, Role.PASSENGER.name());
                ps.executeUpdate();
                System.out.println("Registration successful for: " + username);
                return true;
            }
        } catch (AuthException | DatabaseException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering user", e);
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Logs a user in.
     * @param username The username.
     * @param password The password.
     * @return The User object if login is successful, null otherwise.
     */
    public User login(String username, String password) {
        try {
            User user = findUserByUsername(username);

            if (user != null && user.checkPassword(password)) {
                System.out.println("Login successful! Welcome, " + user.getUsername());
                return user; // Return the logged-in user object
            }
            System.out.println("Error: Invalid username or password.");
            return null; // Login failed
        } catch (AuthException | DatabaseException e) {
            LOGGER.log(Level.SEVERE, "Authentication failed due to system error", e);
            System.out.println("Authentication failed due to system error. Please try again later.");
            return null;
        }
    }
}