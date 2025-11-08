package com.booking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainService {

    private List<Train> trains;

    public TrainService() {
        this.trains = new ArrayList<>();
        // Let's add some default trains for testing
        initializeTrains();
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
        
        Train newTrain = new Train(trainNumber, trainName, route, totalSeats);
        this.trains.add(newTrain);
        System.out.println("Train " + trainName + " added successfully.");
        return true;
    }

    // --- ADMIN METHOD ---
    public List<Train> getAllTrains() {
        return this.trains;
    }
}