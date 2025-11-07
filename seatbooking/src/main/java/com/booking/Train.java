package com.booking;

import java.util.ArrayList;
import java.util.List;

public class Train {

    private String trainNumber;
    private String trainName;
    private List<String> route; // A list of station names
    private List<Seat> seats;

    public Train(String trainNumber, String trainName, List<String> route, int totalSeats) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.route = route;
        
        // OOPS: Composition
        // The Train 'has-a' list of Seats. We create them here.
        this.seats = new ArrayList<>();
        for (int i = 1; i <= totalSeats; i++) {
            this.seats.add(new Seat("S" + i)); // e.g., "S1", "S2", etc.
        }
    }

    // --- Getters ---
    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public List<String> getRoute() {
        return route;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    // --- Helper Method ---
    // Checks if this train runs between the two stations
    public boolean hasStops(String startStation, String endStation) {
        return route.contains(startStation) && route.contains(endStation);
    }
}