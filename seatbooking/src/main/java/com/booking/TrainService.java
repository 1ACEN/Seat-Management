package com.booking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrainService {

    private List<Train> trains;

    public TrainService() {
        this.trains = new ArrayList<>();
        // Ensure DB schema exists
        Database.init();

        // Load trains from DB. If none exist, populate defaults and reload.
        loadTrainsFromDb();
        if (this.trains.isEmpty()) {
            initializeTrains();
            // reload to ensure data came from DB insert
            this.trains.clear();
            loadTrainsFromDb();
        }
    }

    /**
     * Creates some dummy trains for the system to use.
     */
    private void initializeTrains() {
        Train t1 = new Train("T123", "City Express", 
            Arrays.asList("Mumbai", "Pune", "Delhi"), 50);
        
        Train t2 = new Train("T456", "Deccan Queen", 
            Arrays.asList("Mumbai", "Thane", "Pune"), 80);

        Train t3 = new Train("T789", "Capital Mail", 
            Arrays.asList("Delhi", "Jaipur", "Ahmedabad"), 60);

        this.trains.add(t1);
        this.trains.add(t2);
        this.trains.add(t3);
    }

    /**
     * Loads trains from the database into the in-memory list.
     */
    private void loadTrainsFromDb() {
        String sql = "SELECT train_number, train_name, route, total_seats FROM trains";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String number = rs.getString("train_number");
                String name = rs.getString("train_name");
                String routeCsv = rs.getString("route");
                int totalSeats = rs.getInt("total_seats");

                List<String> route = new ArrayList<>();
                if (routeCsv != null && !routeCsv.isEmpty()) {
                    route = Arrays.asList(routeCsv.split(","));
                }

                Train t = new Train(number, name, route, totalSeats);
                this.trains.add(t);
            }
            // After loading trains, mark seats as booked if there are active tickets
            try (PreparedStatement ps2 = c.prepareStatement("SELECT train_number, seat_number FROM tickets WHERE status = 'ACTIVE'");
                 ResultSet rs2 = ps2.executeQuery()) {

                while (rs2.next()) {
                    String tnum = rs2.getString("train_number");
                    String seatNum = rs2.getString("seat_number");

                    // find train in memory
                    for (Train train : this.trains) {
                        if (train.getTrainNumber().equalsIgnoreCase(tnum)) {
                            for (Seat seat : train.getSeats()) {
                                if (seat.getSeatNumber().equalsIgnoreCase(seatNum)) {
                                    seat.book();
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (SQLException ex) {
                System.out.println("Error marking booked seats from tickets: " + ex.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Error loading trains from DB: " + e.getMessage());
        }
    }

    /**
     * Finds all trains that run between the two specified stations.
     * @param startStation The passenger's starting station.
     * @param endStation The passenger's ending station.
     * @return A list of matching Train objects.
     */
    public List<Train> searchTrains(String startStation, String endStation) {
        List<Train> availableTrains = new ArrayList<>();
        for (Train train : this.trains) {
            // We use the helper method we defined in Train.java
            if (train.hasStops(startStation, endStation)) {
                availableTrains.add(train);
            }
        }
        return availableTrains;
    }

    /**
     * Displays all available seats for a specific train.
     * @param train The train to check.
     */
    public void displaySeats(Train train) {
        System.out.println("Available seats for " + train.getTrainName() + ":");
        for (Seat seat : train.getSeats()) {
            if (!seat.isBooked()) {
                // Print available seats, 8 per line
                System.out.print(seat.getSeatNumber() + " ");
            }
        }
        System.out.println(); // New line at the end
    }

    /**
     * Finds a specific seat on a train.
     * @param train The train.
     * @param seatNumber The seat number (e.g., "S5").
     * @return The Seat object if found and available, null otherwise.
     */
    public Seat findSeat(Train train, String seatNumber) {
        for (Seat seat : train.getSeats()) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatNumber) && !seat.isBooked()) {
                return seat;
            }
        }
        return null; // Not found or already booked
    }

    // --- ADMIN METHOD ---
    public boolean addTrain(String trainNumber, String trainName, List<String> route, int totalSeats) {
        // Check if train number already exists
        for (Train train : this.trains) {
            if (train.getTrainNumber().equalsIgnoreCase(trainNumber)) {
                System.out.println("Error: Train Number already exists.");
                return false;
            }
        }
        // Persist to DB
        String sql = "INSERT INTO trains (train_number, train_name, route, total_seats) VALUES (?, ?, ?, ?)";
        String routeCsv = String.join(",", route);
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            ps.setString(2, trainName);
            ps.setString(3, routeCsv);
            ps.setInt(4, totalSeats);
            ps.executeUpdate();

            Train newTrain = new Train(trainNumber, trainName, route, totalSeats);
            this.trains.add(newTrain);
            System.out.println("Train " + trainName + " added successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding train to DB: " + e.getMessage());
            return false;
        }
    }

    // --- ADMIN METHOD ---
    public List<Train> getAllTrains() {
        return this.trains;
    }
}