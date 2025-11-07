package com.booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookingService {

    // In-memory database for all tickets
    private List<Ticket> allTickets;

    public BookingService() {
        this.allTickets = new ArrayList<>();
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

        Ticket newTicket = new Ticket(pnr, passenger, train, seat, date);
        
        // Save the ticket to our list
        this.allTickets.add(newTicket);
        
        // Mark the seat as booked
        seat.book();
        
        return newTicket;
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
        ticket.getSeat().unbook();
        
        // Step 2: Remove the ticket from our database
        return this.allTickets.remove(ticket);
    }
}