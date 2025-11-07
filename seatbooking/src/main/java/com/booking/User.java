package com.booking;

public class User {

    // 1. OOPS: Encapsulation
    // Data is private and only accessed via public methods.
    private String username;
    private String password; // In a real app, this should be hashed!
    private Role role;

    // 2. OOPS: Constructor
    // Used to create a new User object.
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // 3. OOPS: Abstraction
    // Public methods (getters) to access the data.
    public String getUsername() {
        return this.username;
    }

    public Role getRole() {
        return this.role;
    }

    // A method to check the password, rather than exposing it.
    public boolean checkPassword(String providedPassword) {
        return this.password.equals(providedPassword);
    }
}
