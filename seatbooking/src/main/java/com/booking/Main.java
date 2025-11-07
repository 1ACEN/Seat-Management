package com.booking;

import java.util.List;
import java.util.Scanner;

public class Main {

    // Make services and scanner static so all methods can access them
    private static AuthService authService = new AuthService();
    private static TrainService trainService = new TrainService();
    private static BookingService bookingService = new BookingService(); // <-- NEW SERVICE
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        User loggedInUser = null; 

        while (true) {
            System.out.println("\n---== Welcome to the Train Booking System ==---");
            System.out.println("1. Login");
            System.out.println("2. Register New User");
            System.out.println("3. Exit");
            System.out.print("Please choose an option: ");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    // --- LOGIN FLOW ---
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    
                    loggedInUser = authService.login(username, password);

                    if (loggedInUser != null) {
                        if (loggedInUser.getRole() == Role.ADMIN) {
                            showAdminMenu();
                        } else {
                            showPassengerMenu(loggedInUser);
                        }
                    }
                    break;
                case 2:
                    // --- REGISTRATION FLOW ---
                    System.out.print("Enter desired username: ");
                    String newUsername = scanner.nextLine();
                    System.out.print("Enter desired password: ");
                    String newPassword = scanner.nextLine();
                    authService.register(newUsername, newPassword);
                    break;
                case 3:
                    // --- EXIT FLOW ---
                    System.out.println("Thank you for using the system. Goodbye!");
                    scanner.close();
                    return; 
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    /**
     * Handles the Admin's workflow.
     */
    private static void showAdminMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Add/Update Train Details (Not Implemented)");
            System.out.println("2. Check Passenger Details (Not Implemented)");
            System.out.println("3. Logout");
            System.out.print("Please choose an option: ");
            
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                case 2:
                    System.out.println("This feature is not yet implemented.");
                    break;
                case 3:
                    System.out.println("Logging out admin...");
                    return; // Returns to the main menu
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Handles the Passenger's (User's) workflow.
     * @param passenger The user who is logged in.
     */
    private static void showPassengerMenu(User passenger) {
        while (true) {
            System.out.println("\n--- PASSENGER MENU ---");
            System.out.println("1. Book New Ticket");
            System.out.println("2. View My Bookings");
            System.out.println("3. Cancel Ticket");
            System.out.println("4. Logout");
            System.out.print("Please choose an option: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    handleBookTicket(passenger);
                    break;
                case 2:
                    handleViewBookings(passenger); // <-- NEWLY IMPLEMENTED
                    break;
                case 3:
                    handleCancelTicket(passenger); // <-- NEWLY IMPLEMENTED
                    break;
                case 4:
                    System.out.println("Logging out " + passenger.getUsername() + "...");
                    return; // Returns to the main menu
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Handles the full workflow for booking a new ticket.
     * @param passenger The user who is booking.
     */
    private static void handleBookTicket(User passenger) {
        // Step 1: Get travel details (as per your workflow)
        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = scanner.nextLine(); // <-- The 'date' variable is now used!
        System.out.print("Enter Start Station (e.g., Mumbai, Delhi): ");
        String startStation = scanner.nextLine();
        System.out.print("Enter End Station (e.g., Pune, Jaipur): ");
        String endStation = scanner.nextLine();

        // Step 2: Search for trains
        List<Train> availableTrains = trainService.searchTrains(startStation, endStation);

        if (availableTrains.isEmpty()) {
            System.out.println("No trains found for your route. Returning to menu.");
            return;
        }

        // Step 3: Display and select a train
        System.out.println("Available Trains:");
        for (int i = 0; i < availableTrains.size(); i++) {
            Train train = availableTrains.get(i);
            System.out.println((i + 1) + ". " + train.getTrainName() + " (" + train.getTrainNumber() + ")");
        }
        System.out.print("Select a train (enter number): ");
        int trainChoice = Integer.parseInt(scanner.nextLine());

        if (trainChoice < 1 || trainChoice > availableTrains.size()) {
            System.out.println("Invalid train selection. Returning to menu.");
            return;
        }
        Train selectedTrain = availableTrains.get(trainChoice - 1);

        // Step 4: Display and select a seat
        trainService.displaySeats(selectedTrain);
        System.out.print("Select a seat (e.g., S1): ");
        String seatNumber = scanner.nextLine();

        // Step 5: Book the seat
        Seat selectedSeat = trainService.findSeat(selectedTrain, seatNumber);

        if (selectedSeat == null) {
            System.out.println("Invalid seat or seat is already booked. Returning to menu.");
            return;
        }

        // --- Confirmation ---
        System.out.print("Confirm booking for " + selectedTrain.getTrainName() + 
                         ", Seat " + selectedSeat.getSeatNumber() + "? (yes/no): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("yes")) {
            // *** Create and save the ticket ***
            Ticket newTicket = bookingService.createTicket(passenger, selectedTrain, selectedSeat, date);
            
            System.out.println("\nBooking successful! Your Ticket Details:");
            newTicket.displayTicketDetails();
        } else {
            System.out.println("Booking cancelled.");
        }
    }

    /**
     * Finds and displays all tickets for the currently logged-in passenger.
     * @param passenger The logged-in user.
     */
    private static void handleViewBookings(User passenger) {
        System.out.println("\n--- My Bookings ---");
        List<Ticket> myTickets = bookingService.findTicketsByPassenger(passenger);

        if (myTickets.isEmpty()) {
            System.out.println("You have no active bookings.");
        } else {
            for (Ticket ticket : myTickets) {
                ticket.displayTicketDetails();
            }
        }
    }

    /**
     * Handles the workflow for canceling a ticket.
     * @param passenger The logged-in user.
     */
    private static void handleCancelTicket(User passenger) {
        System.out.println("\n--- Cancel Ticket ---");
        System.out.print("Enter your PNR Number to cancel: ");
        String pnr = scanner.nextLine();

        Ticket ticket = bookingService.findTicketByPnr(pnr);

        // Security check: Make sure the ticket exists AND belongs to this passenger
        if (ticket == null || !ticket.getPassenger().getUsername().equals(passenger.getUsername())) {
            System.out.println("Invalid PNR or ticket does not belong to you.");
            return;
        }

        System.out.println("Found ticket:");
        ticket.displayTicketDetails();
        System.out.print("Are you sure you want to cancel this ticket? (yes/no): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("yes")) {
            if (bookingService.cancelTicket(ticket)) {
                System.out.println("Ticket " + pnr + " has been successfully cancelled.");
                System.out.println("Seat " + ticket.getSeat().getSeatNumber() + " is now available.");
            } else {
                System.out.println("Error: Could not cancel ticket.");
            }
        } else {
            System.out.println("Cancellation aborted.");
        }
    }
}