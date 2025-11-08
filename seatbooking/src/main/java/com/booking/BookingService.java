package com.booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingService {

    // In-memory database for active tickets
    private List<Ticket> allTickets;
    private TrainService trainService;

    public BookingService(TrainService trainService) {
        this.allTickets = new ArrayList<>();
        this.trainService = trainService;

        // Ensure DB schema exists
        Database.init();

        // Load active tickets from DB into memory
        loadActiveTicketsFromDb();
    }

    /**
     * Creates a new ticket and saves it.
     * @param passenger The user booking the ticket.
     * @param train The selected train.
     * @param seat The selected seat.
     * @param date The travel date.
     * @return The newly created Ticket object.
     */
    public Ticket createTicket(User passenger, Train train, Seat seat, String date) {
        // Generate a random PNR number (e.g., TKT12345)
        String pnr = "TKT" + (new Random().nextInt(90000) + 10000);
        // Persist ticket to DB
        String sql = "INSERT INTO tickets(pnr, username, train_number, seat_number, travel_date, status) VALUES(?,?,?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pnr);
            ps.setString(2, passenger.getUsername());
            ps.setString(3, train.getTrainNumber());
            ps.setString(4, seat.getSeatNumber());
            ps.setString(5, date);
            ps.setString(6, "ACTIVE");
            ps.executeUpdate();

            Ticket newTicket = new Ticket(pnr, passenger, train, seat, date);
            // Save the ticket to our in-memory active list
            this.allTickets.add(newTicket);
            // Mark the seat as booked
            seat.book();
            return newTicket;
        } catch (SQLException e) {
            System.out.println("Error creating ticket in DB: " + e.getMessage());
            return null;
        }
    }

    /**
     * Finds all tickets belonging to a specific passenger.
     * @param passenger The user to search for.
     * @return A list of tickets.
     */
    public List<Ticket> findTicketsByPassenger(User passenger) {
        List<Ticket> passengerTickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            if (ticket.getPassenger().getUsername().equals(passenger.getUsername())) {
                passengerTickets.add(ticket);
            }
        }
        return passengerTickets;
    }

    /**
     * Finds a specific ticket by PNR number.
     * @param pnr The PNR to search for.
     * @return The Ticket object, or null if not found.
     */
    public Ticket findTicketByPnr(String pnr) {
        for (Ticket ticket : allTickets) {
            if (ticket.getPnrNumber().equalsIgnoreCase(pnr)) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * Cancels a ticket.
     * @param ticket The ticket to be cancelled.
     * @return true if cancellation was successful.
     */
    public boolean cancelTicket(Ticket ticket) {
        // Step 1: Unbook the seat
        // Update DB: mark ticket as CANCELLED
        String sql = "UPDATE tickets SET status = 'CANCELLED' WHERE pnr = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ticket.getPnrNumber());
            int updated = ps.executeUpdate();
            if (updated > 0) {
                // Unbook seat in memory
                ticket.getSeat().unbook();
                // Remove from active in-memory list (history remains in DB)
                return this.allTickets.remove(ticket);
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error cancelling ticket in DB: " + e.getMessage());
            return false;
        }
    }

    public List<Ticket> getAllTickets() {
        return this.allTickets;
    }

    /**
     * Loads active tickets from the DB into memory so users can view them.
     */
    private void loadActiveTicketsFromDb() {
        String sql = "SELECT pnr, username, train_number, seat_number, travel_date FROM tickets WHERE status = 'ACTIVE'";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String pnr = rs.getString("pnr");
                String username = rs.getString("username");
                String trainNumber = rs.getString("train_number");
                String seatNumber = rs.getString("seat_number");
                String travelDate = rs.getString("travel_date");

                // Resolve Train and Seat from TrainService
                Train foundTrain = null;
                Seat foundSeat = null;
                for (Train t : trainService.getAllTrains()) {
                    if (t.getTrainNumber().equalsIgnoreCase(trainNumber)) {
                        foundTrain = t;
                        for (Seat s : t.getSeats()) {
                            if (s.getSeatNumber().equalsIgnoreCase(seatNumber)) {
                                foundSeat = s;
                                break;
                            }
                        }
                        break;
                    }
                }

                if (foundTrain != null && foundSeat != null) {
                    // create a lightweight User object (we only need username for display)
                    User u = new User(username, "", Role.PASSENGER);
                    Ticket tkt = new Ticket(pnr, u, foundTrain, foundSeat, travelDate);
                    this.allTickets.add(tkt);
                    // mark seat as booked in memory
                    foundSeat.book();
                } else {
                    // Could not resolve train/seat in memory; skip (data stays in DB as history)
                    System.out.println("Warning: Could not resolve train/seat for ticket " + pnr);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading active tickets from DB: " + e.getMessage());
        }
    }
}