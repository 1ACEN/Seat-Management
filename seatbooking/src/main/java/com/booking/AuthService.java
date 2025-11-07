package com.booking;

import java.util.ArrayList;
import java.util.List;

public class AuthService {

    // We'll store users in a simple list for now (in-memory database)
    private List<User> users;

    public AuthService() {
        this.users = new ArrayList<>();
        // Let's add a default admin user for testing
        this.users.add(new User("admin", "admin123", Role.ADMIN));
    }

    /**
     * Checks if a user with this username already exists.
     * @param username The username to check.
     * @return The User object if found, null otherwise.
     */
    private User findUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
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

        // By default, new registrations are Passengers
        User newUser = new User(username, password, Role.PASSENGER);
        this.users.add(newUser);
        System.out.println("Registration successful for: " + username);
        return true;
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
            return user; // Return the logged-in user object
        }

        System.out.println("Error: Invalid username or password.");
        return null; // Login failed
    }
}