import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainApplication {

    public static void main(String[] args) {
        Scanner sc = null;
        try {
            sc = new Scanner(System.in);
            CLIUtils.clearScreen();
            CLIUtils.printSingleBlockHeading();
            boolean running = true;
            while (running) {
                try {
                    String[] options = {
                            "1. Sign Up as Citizen",
                            "2. Login",
                            "3. View System Statistics",
                            "4. About RapidResolve",
                            "5. Help / Instructions",
                            "6. Exit"
                    };
                    CLIUtils.printBoxedMenu("Main Menu", options);

                    int choice = CLIUtils.promptInt(sc, "Enter choice: ", 1, options.length);

                    switch (choice) {
                        case 1:
                            signUpFlow(sc);
                            break;
                        case 2:
                            loginFlow(sc);
                            break;
                        case 3:
                            showSystemStats();
                            break;
                        case 4:
                            showAbout();
                            break;
                        case 5:
                            showHelp();
                            break;
                        case 6:
                            CLIUtils.printInfo("Thank you for using RapidResolve. Goodbye!");
                            running = false;
                            break;
                        default:
                            CLIUtils.printError("Invalid choice. Please select 1-" + options.length + ".");
                    }
                    if (running) CLIUtils.waitForEnter();

                } catch (Exception e) {
                    CLIUtils.printError("An unexpected error occurred: " + e.getMessage());
                    CLIUtils.printInfo("Please try again.");
                    CLIUtils.waitForEnter();
                }
            }
        } catch (Exception e) {
            CLIUtils.printError("Critical error: " + e.getMessage());
        } finally {
            if (sc != null) {
                try {
                    sc.close();
                } catch (Exception e) {
                    // Ignore scanner close errors
                }
            }
        }
    }

    // Hash password using SHA-256
    public static String hashPassword(String input) {
        return HashingForPassword.hashPassword(input);
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        // check length (4 to 15 characters)
        if (username.length() < 4 || username.length() > 15) {
            return false;
        }
        // first character must be a letter
        if (!Character.isLetter(username.charAt(0))) {
            return false;
        }
        // check each character
        for (char c : username.toCharArray()) {
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                return false;
            }
        }
        return true;
    }

    private static void loginFlow(Scanner sc) {
        // Robust role input loop
        String role;
        while (true) {
            role = CLIUtils.promptString(sc, "Enter role (citizen/officer/admin): ", true, 3, 10, null, null).toLowerCase();
            if (role.equals("citizen") || role.equals("officer") || role.equals("admin")) {
                break;
            } else {
                CLIUtils.printError("Invalid role entered. Please enter 'citizen', 'officer', or 'admin'.");
            }
        }
        System.out.print(CLIUtils.YELLOW + "Enter username: " + CLIUtils.RESET);
        String username = sc.nextLine();

        if (!isValidUsername(username)) {
            System.out.println(CLIUtils.RED + "❌ Invalid username." + CLIUtils.RESET);
        }

        String rawPassword = CLIUtils.promptString(sc, "Password: ", true, 6, 30, null, null);

        boolean authenticated = false;
        switch (role) {
            case "citizen":
                String hashedPassword = hashPassword(rawPassword);
                Citizen citizen = UserManager.getUser(username, hashedPassword);
                if (citizen != null) {
                    citizen.showMenu(sc);
                    authenticated = true;
                } else {
                    CLIUtils.printError("Invalid citizen credentials.");
                }
                break;
            case "officer":
                Officer officer = OfficerManager.getOfficer(username, rawPassword);
                if (officer != null) {
                    if (officer.getMustChangePassword() == 1) {
                        CLIUtils.printInfo("You must change your temporary password to proceed.");
                        changePasswordFlow(officer, sc);
                    } else {
                        officer.showMenu(sc);
                    }
                    authenticated = true;
                } else {
                    CLIUtils.printError("Invalid officer credentials.");
                }
                break;

            case "admin":
                String adminHashedPassword = hashPassword(rawPassword);
                Admin admin = AdminManager.getAdmin(username, adminHashedPassword);
                if (admin != null) {
                    admin.showMenu(sc);
                    authenticated = true;
                } else {
                    CLIUtils.printError("Invalid admin credentials.");
                }
                break;
        }
        // Exit program after logout or dashboard exit
        if (authenticated) {
            CLIUtils.printInfo("You have logged out. Exiting application.");
            System.exit(0);
        }
    }

    private static void signUpFlow(Scanner sc) {
        try {
            CLIUtils.printInfo("💡 Tip: Use strong passwords for your account!");
            CLIUtils.printInfo("Sign Up - Enter your details:");

            // Username
            String username = CLIUtils.promptString(sc, "Username: ", true, 3, 20, "^(?=.*[A-Za-z])[A-Za-z0-9]+$", "Username must contain at least one letter and cannot be purely numeric.");

            // Password
            String rawPassword = CLIUtils.promptString(sc, "Password: ", true, 6, 30, "^(?=.*[A-Za-z])[A-Za-z0-9]+$", "Password must contain at least one letter and cannot be purely numeric.");
            String password = hashPassword(rawPassword);

            // Full Name
            String name = CLIUtils.promptString(sc, "Full Name: ", true, 2, 40, null, null);

            // Country Code
            String countryCode = CLIUtils.promptString(sc, "Country Code (+91, etc): ", true, 2, 5, "^\\+\\d+$", "Country code must start with + and be numeric (e.g., +91). ");

            // Phone number validation loop
            String phone;
            while (true) {
                CLIUtils.printPrompt("Phone Number (10 digits): ");
                String newValue = sc.nextLine().trim();
                boolean allDigits = newValue.length() == 10;
                for (int i = 0; i < newValue.length() && allDigits; i++) {
                    if (!Character.isDigit(newValue.charAt(i))) {
                        allDigits = false;
                        break;
                    }
                }
                if (allDigits) {
                    phone = newValue;
                    break;
                } else {
                    CLIUtils.printError("Phone number must be exactly 10 digits.");
                }
            }

            // OTP generation and file output
            int otp = 1000 + new java.util.Random().nextInt(9000);
            try (FileWriter fw = new FileWriter("otp.txt")) {
                fw.write(" Thank you for visiting\n RapidResolve Portal. " +
                        "OTP for login is : " + otp + ". \nThis is valid for 10 mins. " +
                        "Do not share it with \nanyone. - Department of Telecom");
                fw.flush();
                CLIUtils.printInfo("OTP generated! Please check the file otp.txt to view your OTP.");
            } catch (IOException e) {
                CLIUtils.printError("Could not write OTP to file: " + e.getMessage());
                return;
            }

            // Prompt and validate OTP in a loop with lock check
            while (true) {
                if (OTPAttemptTracker.isLocked(username)) {
                    CLIUtils.printError("Too many incorrect OTP attempts. Account is locked. Try again later.");
                    return;
                }
                int enteredOtp = CLIUtils.promptInt(sc, "Enter OTP: ", 1000, 9999);
                if (enteredOtp != otp) {
                    OTPAttemptTracker.recordFailure(username);
                    CLIUtils.printError("Incorrect OTP. Please try again.");
                } else {
                    CLIUtils.printSuccess("OTP verified successfully!");
                    break;
                }
            }

            // State
            String state = CLIUtils.promptString(sc, "State: ", true, 2, 30, null, null);

            // City
            String city = CLIUtils.promptString(sc, "City: ", true, 2, 30, null, null);

            // Landmark
            String landmark = CLIUtils.promptString(sc, "Landmark: ", true, 2, 30, null, null);

            // House No
            String houseNo = CLIUtils.promptString(sc, "House No: ", true, 1, 10, null, null);

            // Age
            int age = CLIUtils.promptInt(sc, "Age: ", 1, 120);


            // Email with normalization and feedback
            System.out.print(CLIUtils.YELLOW + "Enter your email: " + CLIUtils.RESET);
            String inputEmail = sc.nextLine();
            String email = normalizeEmail(inputEmail);
            if (email != null) {
                System.out.println(CLIUtils.GREEN + "✅ Stored Email: " + email + CLIUtils.RESET);
            } else {
                System.out.println(CLIUtils.RED + "❌ Invalid email." + CLIUtils.RESET);
            }

            // Register user
            Citizen citizen = UserManager.registerUser(username, password, name, countryCode, phone, state, city, landmark, houseNo, age, email);
            if (citizen == null) {
                CLIUtils.printError("Registration failed! Username may already exist or there was a database error.");
            } else {
                CLIUtils.printSuccess("Registration successful! Please login to continue.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error during registration: " + e.getMessage());
        }
    }

    // Email normalization helper
    public static String normalizeEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null; // invalid input
        }

        email = email.trim().toLowerCase(); // clean up

        // If email already contains "@gmail.com"
        if (email.endsWith("@gmail.com")) {
            return email;
        }

        // If user only gave name without domain, add @gmail.com
        if (!email.contains("@")) {
            return email + "@gmail.com";
        }

        // If some other domain is given, force it to gmail.com
        int atIndex = email.indexOf("@");
        String username = email.substring(0, atIndex);
        return username + "@gmail.com";
    }

    private static void showSystemStats() {
        try {
            String[] lines = {
                    "Total Registered Citizens: " + UserManager.getCitizenCount(),
                    "Total Officers: " + OfficerManager.getOfficerCount(),
                    "Total Complaints: " + ComplaintManager.getTotalComplaintCount(),
                    "Total Crimes: " + CrimeManager.getTotalCrimeCount()
            };
            CLIUtils.printBoxedInfo("System Statistics", lines);
        } catch (Exception e) {
            CLIUtils.printError("Error loading system statistics: " + e.getMessage());
        }
    }

    private static void showAbout() {
        try {
            String[] lines = {
                    "RapidResolve is a professional Complaint & Crime Management System.",
                    "It enables citizens to file complaints, officers to manage cases,",
                    "and admins to oversee the system with advanced reporting.",
                    "Developed for smart, efficient, and transparent governance."
            };
            CLIUtils.printBoxedInfo("About RapidResolve", lines);
        } catch (Exception e) {
            CLIUtils.printError("Error displaying about information: " + e.getMessage());
        }
    }

    private static void showHelp() {
        try {
            String[] lines = {
                    "1. Sign Up as Citizen: Register yourself to use the system.",
                    "2. Login: Access your account based on role (citizen, officer, admin).",
                    "3. View System Statistics: Live stats of the platform.",
                    "4. About: Learn about RapidResolve.",
                    "5. Help: View usage instructions.",
                    "6. Exit: Close the application."
            };
            CLIUtils.printBoxedInfo("Help / Instructions", lines);
        } catch (Exception e) {
            CLIUtils.printError("Error displaying help: " + e.getMessage());
        }
    }

    private static void changePasswordFlow(Officer officer, Scanner sc) {
        while (true) {
            String newPassword = CLIUtils.promptString(sc, "Enter new password: ", true, 6, 30,
                    "^(?=.*[a-zA-Z])[a-zA-Z0-9@#$%_]+$", "Password must contain letters and allowed symbols.");
            String confirmPassword = CLIUtils.promptString(sc, "Confirm new password: ", true, 6, 30,
                    null, null);
            if (!newPassword.equals(confirmPassword)) {
                CLIUtils.printError("Passwords do not match. Please try again.");
                continue;
            }
            String hashedNewPassword = hashPassword(newPassword);
            boolean updateSuccess = OfficerManager.updatePassword(officer.getUserId(), hashedNewPassword);
            if (updateSuccess) {
                CLIUtils.printSuccess("Password changed successfully. You may now log in with your new password.");
                // Reload officer with new password to continue
                Officer updatedOfficer = OfficerManager.getOfficer(officer.getUsername(), newPassword);
                if (updatedOfficer != null) {
                    updatedOfficer.showMenu(sc);
                }
                break;
            } else {
                CLIUtils.printError("Failed to update password. Please try again.");
            }
        }
    }

}