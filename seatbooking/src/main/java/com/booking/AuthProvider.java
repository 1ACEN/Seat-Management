package com.booking;

/**
 * Authentication provider abstraction.
 * Implementations may store users in-memory, in a database, or via an external service.
 */
public interface AuthProvider {

    /**
     * Register a new user with given username and password. Default role should be PASSENGER.
     * @return true if registration succeeded, false otherwise (e.g. username exists)
     */
    boolean register(String username, String password);

    /**
     * Authenticate a user and return the User object if credentials are valid.
     * @return User on success or null on failure
     */
    User login(String username, String password);
}
