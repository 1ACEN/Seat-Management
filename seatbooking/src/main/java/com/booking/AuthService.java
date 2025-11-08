package com.booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AuthService backed by a MySQL database (users table).
 *
 * Notes:
 * - Passwords are stored in plaintext for simplicity (keep as-is to match existing design).
 * - In production, always store salted hashes.
 */
public class AuthService {

    public AuthService() {
        // Initialize DB schema and ensure default admin exists
        Database.init();

        if (findUserByUsername("admin") == null) {
            try (Connection c = Database.getConnection();
                 PreparedStatement ps = c.prepareStatement("INSERT INTO users(username, password, role) VALUES(?,?,?)")) {
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                ps.setString(3, Role.ADMIN.name());
                ps.executeUpdate();
                System.out.println("Created default admin user (admin/admin123)");
            } catch (SQLException e) {
                // If another process created it concurrently, ignore duplicate key
                if (!e.getMessage().toLowerCase().contains("duplicate")) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Records a user-related event into user_history table.
     */
    private void recordUserHistory(String username, String action, String details) {
        String sql = "INSERT INTO user_history(username, action, details) VALUES(?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Non-fatal: print and continue
            System.out.println("Warning: could not write to user_history: " + e.getMessage());
        }
    }

    /**
     * Checks if a user with this username already exists.
     * @param username The username to check.
     * @return The User object if found, null otherwise.
     */
    private User findUserByUsername(String username) {
        String sql = "SELECT username, password, role FROM users WHERE username = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String u = rs.getString("username");
                    String p = rs.getString("password");
                    Role r = Role.valueOf(rs.getString("role"));
                    return new User(u, p, r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        if (findUserByUsername(username) != null) {
            System.out.println("Error: Username already exists. Please try another.");
            return false;
        }

        String sql = "INSERT INTO users(username, password, role) VALUES(?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, Role.PASSENGER.name());
            ps.executeUpdate();
            System.out.println("Registration successful for: " + username);
                // record history
                recordUserHistory(username, "REGISTER", null);
            return true;
        } catch (SQLException e) {
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
        User user = findUserByUsername(username);

        if (user != null && user.checkPassword(password)) {
            System.out.println("Login successful! Welcome, " + user.getUsername());
            // record login
            recordUserHistory(username, "LOGIN", null);
            return user; // Return the logged-in user object
        }

        System.out.println("Error: Invalid username or password.");
        return null; // Login failed
    }

    /**
     * Public helper to record logout events.
     */
    public void recordLogout(String username) {
        recordUserHistory(username, "LOGOUT", null);
    }
}