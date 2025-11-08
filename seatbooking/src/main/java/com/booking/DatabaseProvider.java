package com.booking;

import java.sql.Connection;

/**
 * Abstraction for obtaining JDBC connections and initializing schema.
 * Implementations allow swapping DB providers for testing or different stores.
 */
public interface DatabaseProvider {
    /**
     * Get a new JDBC Connection. Caller is responsible for closing it.
     */
    /**
     * Get a new JDBC Connection. Implementations should wrap SQL errors in DatabaseException.
     */
    Connection getConnection();

    /**
     * Initialize required database schema (if any).
     */
    void init();
}
