package com.booking;

public class Seat {

    private String seatNumber;
    private boolean isBooked;

    public Seat(String seatNumber) {
        this.seatNumber = seatNumber;
        this.isBooked = false; // By default, a new seat is not booked
    }

    // --- Getters ---
    public String getSeatNumber() {
        return seatNumber;
    }

    public boolean isBooked() {
        return isBooked;
    }

    // --- Methods to change the state ---
    public void book() {
        this.isBooked = true;
    }

    public void unbook() {
        this.isBooked = false;
    }
}
