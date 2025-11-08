package com.booking.model;

import java.util.ArrayList;
import java.util.List;


public class Train {

    private String trainNumber;
    private String trainName;
    private List<String> route; 
    private List<Seat> seats;

    public Train(String trainNumber, String trainName, List<String> route, int totalSeats) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.route = route;
        
        // Composition: Train has Seats
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

    /**
     * Returns number of seats that are currently booked on this train.
     */
    public int getBookedSeatCount() {
        int count = 0;
        for (Seat s : seats) {
            if (s.isBooked()) count++;
        }
        return count;
    }

    /**
     * Returns number of seats that are currently available on this train.
     */
    public int getAvailableSeatCount() {
        return seats.size() - getBookedSeatCount();
    }

    // --- Helper Method ---
    // Checks if this train runs between the two stations
    public boolean hasStops(String startStation, String endStation) {
        if (startStation == null || endStation == null) return false;
        String start = startStation.trim().toLowerCase();
        String end = endStation.trim().toLowerCase();

        int startIdx = -1;
        int endIdx = -1;
        for (int i = 0; i < route.size(); i++) {
            String stop = route.get(i);
            if (stop == null) continue;
            String norm = stop.trim().toLowerCase();
            if (norm.equals(start) && startIdx == -1) startIdx = i;
            if (norm.equals(end) && endIdx == -1) endIdx = i;
        }

        // Ensure both stops exist and start appears before end (direction-aware)
        return startIdx != -1 && endIdx != -1 && startIdx < endIdx;
    }
}
