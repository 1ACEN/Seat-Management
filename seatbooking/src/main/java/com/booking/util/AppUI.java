package com.booking.util;

import com.booking.model.User;
import com.booking.model.Train;
import com.booking.model.Seat;
import com.booking.model.Ticket;
import com.booking.model.Role;
import com.booking.service.AuthProvider;
import com.booking.service.TrainService;
import com.booking.service.BookingService;
import com.booking.exception.ValidationException;
import com.booking.util.InputValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Console UI helper extracted from Main to keep main class small and focused.
 */
public class AppUI {

    private final AuthProvider authService;
    private final TrainService trainService;
    private final BookingService bookingService;
    private final Scanner scanner;

    public AppUI(AuthProvider authService, TrainService trainService, BookingService bookingService, Scanner scanner) {
        this.authService = authService;
        this.trainService = trainService;
        this.bookingService = bookingService;
        this.scanner = scanner;
    }

    public void run() {
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
                        authService.recordLogout(loggedInUser.getUsername());
                        loggedInUser = null;
                    }
                    break;
                case 2:
                    System.out.print("Enter desired username: ");
                    String newUsername = scanner.nextLine();
                    System.out.print("Enter desired password: ");
                    String newPassword = scanner.nextLine();
                    authService.register(newUsername, newPassword);
                    break;
                case 3:
                    System.out.println("Thank you for using the system. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    // Admin Menu
    private void showAdminMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Add New Train");
            System.out.println("2. View All Trains");
            System.out.println("3. Check Passenger Details (View All Bookings)");
            System.out.println("4. Logout");
            System.out.print("Please choose an option: ");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    handleAddTrain();
                    break;
                case 2:
                    handleViewAllTrains();
                    break;
                case 3:
                    handleViewAllBookings();
                    break;
                case 4:
                    System.out.println("Logging out admin...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void handleAddTrain() {
        System.out.println("\n--- Add New Train ---");
        System.out.print("Enter Train Number (e.g., T123): ");
        String trainNumber = scanner.nextLine();
        System.out.print("Enter Train Name (e.g., City Express): ");
        String trainName = scanner.nextLine();
        System.out.print("Enter Route (comma-separated, e.g., Mumbai,Pune,Delhi): ");
        String routeStr = scanner.nextLine();
        List<String> route = Arrays.stream(routeStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (route.size() < 2) {
            System.out.println("Error: A route must contain at least two stops (start and end).");
            return;
        }
        System.out.print("Enter Total Seats (e.g., 50): ");
        int totalSeats = Integer.parseInt(scanner.nextLine());

        trainService.addTrain(trainNumber, trainName, route, totalSeats);
    }

    private void handleViewAllTrains() {
        System.out.println("\n--- All Trains in System ---");
        List<Train> trains = trainService.getAllTrains();
        if (trains.isEmpty()) {
            System.out.println("No trains have been added to the system yet.");
            return;
        }
        for (Train train : trains) {
            System.out.println("--------------------");
            System.out.println("Train Name: " + train.getTrainName());
            System.out.println("Train Number: " + train.getTrainNumber());
            System.out.println("Route: " + String.join(" -> ", train.getRoute()));
            System.out.println("Total Seats: " + train.getSeats().size());
            System.out.println("Booked Seats: " + train.getBookedSeatCount());
            System.out.println("Available Seats: " + train.getAvailableSeatCount());
            // Optionally show booked seat numbers (if any)
            List<String> booked = train.getSeats().stream().filter(Seat::isBooked).map(Seat::getSeatNumber).toList();
            if (!booked.isEmpty()) {
                System.out.println("Booked Seat Numbers: " + String.join(", ", booked));
            }
        }
    }

    private void handleViewAllBookings() {
        System.out.println("\n--- All Passenger Bookings ---");
        List<Ticket> allTickets = bookingService.getAllTickets();
        if (allTickets.isEmpty()) {
            System.out.println("No tickets have been booked in the system yet.");
        } else {
            System.out.println("Total bookings: " + allTickets.size());
            for (Ticket ticket : allTickets) {
                ticket.displayTicketDetails();
            }
        }
    }

    // Passenger menu
    private void showPassengerMenu(User passenger) {
        while (true) {
            System.out.println("\n--- PASSENGER MENU ---");
            System.out.println("1. Book New Ticket");
            System.out.println("2. View My Bookings");
            System.out.println("3. Cancel Ticket");
            System.out.println("4. Logout");
            System.out.print("Please choose an option: ");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    handleBookTicket(passenger);
                    break;
                case 2:
                    handleViewBookings(passenger);
                    break;
                case 3:
                    handleCancelTicket(passenger);
                    break;
                case 4:
                    System.out.println("Logging out " + passenger.getUsername() + "...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void handleBookTicket(User passenger) {
        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        // quick validation before doing heavier work
        if (!InputValidator.isValidDate(date)) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }
        if (!InputValidator.isNotPastDate(date)) {
            System.out.println("Travel date cannot be before today.");
            return;
        }
        System.out.print("Enter Start Station (e.g., Mumbai, Delhi): ");
        String startStation = scanner.nextLine();
        System.out.print("Enter End Station (e.g., Pune, Jaipur): ");
        String endStation = scanner.nextLine();

        List<Train> availableTrains = trainService.searchTrains(startStation, endStation);
        if (availableTrains.isEmpty()) {
            System.out.println("No trains found for your route. Returning to menu.");
            return;
        }

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

        trainService.displaySeats(selectedTrain);
        System.out.print("Select a seat (e.g., S1): ");
        String seatNumber = scanner.nextLine();

        Seat selectedSeat = trainService.findSeat(selectedTrain, seatNumber);
        if (selectedSeat == null) {
            System.out.println("Invalid seat or seat is already booked. Returning to menu.");
            return;
        }

        System.out.print("Confirm booking for " + selectedTrain.getTrainName() + ", Seat " + selectedSeat.getSeatNumber() + "? (yes/no): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("yes")) {
            try {
                Ticket newTicket = bookingService.createTicket(passenger, selectedTrain, selectedSeat, date);
                System.out.println("\nBooking successful! Your Ticket Details:");
                newTicket.displayTicketDetails();
            } catch (ValidationException ve) {
                System.out.println("Booking failed: " + ve.getMessage());
            }
        } else {
            System.out.println("Booking cancelled.");
        }
    }

    private void handleViewBookings(User passenger) {
        System.out.println("\n--- My Bookings ---");

        List<Ticket> upcoming = bookingService.findTicketsByPassenger(passenger);
        List<Ticket> past = bookingService.findPastTicketsByPassenger(passenger);

        System.out.println("\nUpcoming / Active Journeys:");
        if (upcoming == null || upcoming.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (Ticket ticket : upcoming) {
                ticket.displayTicketDetails();
            }
        }

        System.out.println("\nPast Journeys:");
        if (past == null || past.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (Ticket ticket : past) {
                ticket.displayTicketDetails();
            }
        }
    }

    private void handleCancelTicket(User passenger) {
        System.out.println("\n--- Cancel Ticket ---");
            List<Ticket> myTickets = bookingService.findTicketsByPassenger(passenger);
            if (myTickets.isEmpty()) {
                System.out.println("You have no active bookings to cancel.");
                return;
            }

            System.out.println("Your active bookings:");
            for (int i = 0; i < myTickets.size(); i++) {
                Ticket t = myTickets.get(i);
                System.out.println((i + 1) + ") PNR: " + t.getPnrNumber() + " | Train: " + t.getTrain().getTrainName() + " (" + t.getTrain().getTrainNumber() + ") | Seat: " + t.getSeat().getSeatNumber() + " | Date: " + t.getTravelDate());
            }

            System.out.print("Select a ticket to cancel (enter number) or 'q' to go back: ");
            String sel = scanner.nextLine().trim();
            if (sel.equalsIgnoreCase("q") || sel.isEmpty()) {
                System.out.println("Cancellation aborted.");
                return;
            }

            int idx;
            try {
                idx = Integer.parseInt(sel);
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection.");
                return;
            }

            if (idx < 1 || idx > myTickets.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            Ticket ticket = myTickets.get(idx - 1);
            System.out.println("Found ticket:");
            ticket.displayTicketDetails();
            System.out.print("Are you sure you want to cancel this ticket? (yes/no): ");
            String confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("yes")) {
                if (bookingService.cancelTicket(ticket)) {
                    System.out.println("Ticket " + ticket.getPnrNumber() + " has been successfully cancelled.");
                    System.out.println("Seat " + ticket.getSeat().getSeatNumber() + " is now available.");
                } else {
                    System.out.println("Error: Could not cancel ticket.");
                }
            } else {
                System.out.println("Cancellation aborted.");
            }
    }
}
