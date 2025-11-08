package com.booking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseService {

    // This is the path to our database file.
    // "jdbc:sqlite:" is the driver, "train_booking.db" is the file name.
    private static final String DB_URL = "jdbc:sqlite:train_booking.db";

    /**
     * Connects to the SQLite database.
     * The database file will be created automatically if it doesn't exist.
     * @return a Connection object
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Creates the necessary tables if they don't already exist.
     * This should be called once when the application starts.
     */
    public static void initializeDatabase() {
        // SQL query to create the users table
        // "CREATE TABLE IF NOT EXISTS" is safe to run every time.
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users ("
                        + " username TEXT PRIMARY KEY,"
                        + " password TEXT NOT NULL,"
                        + " role TEXT NOT NULL"
                        + ");";
        
        // We will add more tables here in the next steps
        // String sqlTrains = "..."
        // String sqlTickets = "..."

        // This is a "try-with-resources" block.
        // It automatically closes the connection and statement, which is very important.
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // Execute the query to create the table
            stmt.execute(sqlUsers);
            
        } catch (Exception e) {
            System.out.println("Database initialization error: " + e.getMessage());
        }
    }
}
