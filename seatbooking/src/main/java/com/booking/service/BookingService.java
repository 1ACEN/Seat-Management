package com.booking.service;

import com.booking.exception.ValidationException;
import com.booking.model.Ticket;
import com.booking.model.User;
import com.booking.model.Train;
import com.booking.model.Seat;
import com.booking.model.Role;
import com.booking.util.InputValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BookingService {

    // In-memory database for active tickets
    private List<Ticket> allTickets;
    private TrainService trainService;
    private final DatabaseProvider db;

    public BookingService(TrainService trainService, DatabaseProvider db) {
        this.allTickets = new ArrayList<>();
        this.trainService = trainService;
        this.db = db;

        // Ensure DB schema exists
        try {
            this.db.init();
        } catch (com.booking.exception.DatabaseException e) {
            // fatal - rethrow
            throw e;
        }

        // Load active tickets from DB into memory
        loadActiveTicketsFromDb();
    }

    public Ticket createTicket(User passenger, Train train, Seat seat, String date) {
        // Validate date
        if (date == null || !InputValidator.isValidDate(date)) {
            throw new ValidationException("Invalid travel date format. Expected YYYY-MM-DD.");
        }
        if (!InputValidator.isNotPastDate(date)) {
            throw new ValidationException("Travel date cannot be before today.");
        }

        String pnr = "TKT" + (new Random().nextInt(90000) + 10000);
        String sql = "INSERT INTO tickets(pnr, username, train_number, seat_number, travel_date, status) VALUES(?,?,?,?,?,?)";
        try (Connection c = this.db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pnr);
            ps.setString(2, passenger.getUsername());
            ps.setString(3, train.getTrainNumber());
            ps.setString(4, seat.getSeatNumber());
            ps.setString(5, date);
            ps.setString(6, "ACTIVE");
            ps.executeUpdate();

            Ticket newTicket = new Ticket(pnr, passenger, train, seat, date);
            this.allTickets.add(newTicket);
            seat.book();

            String findId = "SELECT id FROM users WHERE username = ?";
            Integer userId = null;
            try (Connection conn2 = this.db.getConnection(); PreparedStatement ps2 = conn2.prepareStatement(findId)) {
                ps2.setString(1, passenger.getUsername());
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) userId = rs2.getInt("id");
                }
            } catch (SQLException | com.booking.exception.DatabaseException e) {
            }

                String insertHistory = "INSERT INTO user_history(user_id, pnr, username, action, details) VALUES(?,?,?,?,?)";
                try (Connection conn3 = this.db.getConnection(); PreparedStatement ps3 = conn3.prepareStatement(insertHistory)) {
                    if (userId != null) ps3.setInt(1, userId); else ps3.setNull(1, java.sql.Types.INTEGER);
                    ps3.setString(2, pnr);
                    ps3.setString(3, passenger.getUsername());
                    ps3.setString(4, "BOOK");
                    ps3.setString(5, "Booked seat " + seat.getSeatNumber() + " on train " + train.getTrainNumber());
                    ps3.executeUpdate();
                } catch (SQLException | com.booking.exception.DatabaseException e) {
                    // Fallback for older schema: try legacy insert without user_id/pnr
                    try {
                        String legacy = "INSERT INTO user_history(username, action, details) VALUES(?,?,?)";
                        try (Connection c2 = this.db.getConnection(); PreparedStatement ps2 = c2.prepareStatement(legacy)) {
                            ps2.setString(1, passenger.getUsername());
                            ps2.setString(2, "BOOK");
                            ps2.setString(3, "Booked seat " + seat.getSeatNumber() + " on train " + train.getTrainNumber());
                            ps2.executeUpdate();
                        }
                    } catch (SQLException | com.booking.exception.DatabaseException ex) {
                        // best-effort, ignore
                    }
                }

            return newTicket;
        } catch (com.booking.exception.DatabaseException | SQLException e) {
            System.out.println("Error creating ticket in DB: " + e.getMessage());
            return null;
        }
    }

    public List<Ticket> findTicketsByPassenger(User passenger) {
        List<Ticket> passengerTickets = new ArrayList<>();
        String sql = "SELECT pnr, train_number, seat_number, travel_date FROM tickets WHERE username = ? AND status = 'ACTIVE'";
        try (Connection c = this.db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passenger.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pnr = rs.getString("pnr");
                    String trainNumber = rs.getString("train_number");
                    String seatNumber = rs.getString("seat_number");
                    String travelDate = rs.getString("travel_date");

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
                        User u = new User(passenger.getUsername(), "", Role.PASSENGER);
                        Ticket tkt = new Ticket(pnr, u, foundTrain, foundSeat, travelDate);
                        passengerTickets.add(tkt);
                    } else {
                        System.out.println("Warning: Could not resolve train/seat for ticket " + pnr);
                    }
                }
            }
        } catch (SQLException | com.booking.exception.DatabaseException e) {
            System.out.println("Error loading active tickets from DB: " + e.getMessage());
        }
        return passengerTickets;
    }

    public Ticket findTicketByPnr(String pnr) {
        for (Ticket ticket : allTickets) {
            if (ticket.getPnrNumber().equalsIgnoreCase(pnr)) {
                return ticket;
            }
        }
        return null;
    }

    public boolean cancelTicket(Ticket ticket) {
        String sql = "UPDATE tickets SET status = 'CANCELLED' WHERE pnr = ?";
        try (Connection c = this.db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ticket.getPnrNumber());
            int updated = ps.executeUpdate();
            if (updated > 0) {
                ticket.getSeat().unbook();
                boolean removed = this.allTickets.remove(ticket);

                // record cancellation in user_history (best-effort)
                String findId = "SELECT id FROM users WHERE username = ?";
                Integer userId = null;
                try (Connection conn2 = this.db.getConnection(); PreparedStatement ps2 = conn2.prepareStatement(findId)) {
                    ps2.setString(1, ticket.getPassenger().getUsername());
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        if (rs2.next()) userId = rs2.getInt("id");
                    }
                } catch (SQLException | com.booking.exception.DatabaseException e) {
                    // ignore
                }

                String insertHistory = "INSERT INTO user_history(user_id, pnr, username, action, details) VALUES(?,?,?,?,?)";
                try (Connection conn3 = this.db.getConnection(); PreparedStatement ps3 = conn3.prepareStatement(insertHistory)) {
                    if (userId != null) ps3.setInt(1, userId); else ps3.setNull(1, java.sql.Types.INTEGER);
                    ps3.setString(2, ticket.getPnrNumber());
                    ps3.setString(3, ticket.getPassenger().getUsername());
                    ps3.setString(4, "CANCEL");
                    ps3.setString(5, "Cancelled ticket PNR " + ticket.getPnrNumber());
                    ps3.executeUpdate();
                } catch (SQLException | com.booking.exception.DatabaseException e) {
                    // best-effort
                }

                return removed;
            } else {
                return false;
            }
        } catch (com.booking.exception.DatabaseException | SQLException e) {
            System.out.println("Error cancelling ticket in DB: " + e.getMessage());
            return false;
        }
    }

    public List<Ticket> getAllTickets() {
        return this.allTickets;
    }

    private void loadActiveTicketsFromDb() {
        String sql = "SELECT pnr, username, train_number, seat_number, travel_date FROM tickets WHERE status = 'ACTIVE'";
        try (Connection c = this.db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String pnr = rs.getString("pnr");
                String username = rs.getString("username");
                String trainNumber = rs.getString("train_number");
                String seatNumber = rs.getString("seat_number");
                String travelDate = rs.getString("travel_date");

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
                    User u = new User(username, "", Role.PASSENGER);
                    Ticket tkt = new Ticket(pnr, u, foundTrain, foundSeat, travelDate);
                    this.allTickets.add(tkt);
                    foundSeat.book();
                } else {
                    System.out.println("Warning: Could not resolve train/seat for ticket " + pnr);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading active tickets from DB: " + e.getMessage());
        }
    }

    /**
     * Load past/cancelled tickets for a given passenger from the database.
     * A ticket is considered "past" when its travel_date is before today, or
     * when its status is not 'ACTIVE'.
     */
    public List<Ticket> findPastTicketsByPassenger(User passenger) {
        List<Ticket> past = new ArrayList<>();
        String sql = "SELECT pnr, username, train_number, seat_number, travel_date, status FROM tickets WHERE username = ?";
        try (Connection c = this.db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passenger.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pnr = rs.getString("pnr");
                    String username = rs.getString("username");
                    String trainNumber = rs.getString("train_number");
                    String seatNumber = rs.getString("seat_number");
                    String travelDate = rs.getString("travel_date");
                    String status = rs.getString("status");

                    boolean isPast = false;
                    if (travelDate != null && !travelDate.isBlank()) {
                        try {
                            LocalDate d = LocalDate.parse(travelDate, DateTimeFormatter.ISO_LOCAL_DATE);
                            if (d.isBefore(LocalDate.now())) isPast = true;
                        } catch (DateTimeParseException ex) {
                            // ignore parse errors and treat as not-past here
                        }
                    }

                    if (!"ACTIVE".equalsIgnoreCase(status) || isPast) {
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
                            User u = new User(username, "", Role.PASSENGER);
                            Ticket tkt = new Ticket(pnr, u, foundTrain, foundSeat, travelDate);
                            past.add(tkt);
                        } else {
                            System.out.println("Warning: Could not resolve train/seat for past ticket " + pnr);
                        }
                    }
                }
            }
        } catch (SQLException | com.booking.exception.DatabaseException e) {
            System.out.println("Error loading past tickets from DB: " + e.getMessage());
        }
        return past;
    }
}
