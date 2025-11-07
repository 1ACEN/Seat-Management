package com.booking;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuthService authService = new AuthService();
        User loggedInUser = null; // This will store the user who is logged in

        // This is the main application loop
        while (true) {
            System.out.println("\n---== Welcome to the Train Booking System ==---");
            System.out.println("1. Login");
            System.out.println("2. Register New User");
            System.out.println("3. Exit");
            System.out.print("Please choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    // --- LOGIN FLOW ---
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    
                    loggedInUser = authService.login(username, password);

                    if (loggedInUser != null) {
                        // Check the user's role and show the correct menu
                        if (loggedInUser.getRole() == Role.ADMIN) {
                            showAdminMenu(scanner);
                        } else {
                            showPassengerMenu(scanner);
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
                    return; // Exits the main method, stopping the program
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    // --- Placeholder Menus ---
    // We will build these out in the next steps

    /**
     * This method will handle the Admin's workflow.
     */
    private static void showAdminMenu(Scanner scanner) {
        System.out.println("\n--- ADMIN MENU ---");
        System.out.println("1. Add/Update Train Details");
        System.out.println("2. Check Passenger Details");
        System.out.println("3. Logout");
        System.out.println("--------------------");
        // We will add logic for this menu next
    }

    /**
     * This method will handle the Passenger's (User's) workflow.
     */
    private static void showPassengerMenu(Scanner scanner) {
        System.out.println("\n--- PASSENGER MENU ---");
        System.out.println("1. Book New Ticket");
        System.out.println("2. View My Bookings (Previous Tickets)");
        System.out.println("3. Cancel Ticket");
        System.out.println("4. Logout");
        System.out.println("----------------------");
        // We will add logic for this menu next
    }
}