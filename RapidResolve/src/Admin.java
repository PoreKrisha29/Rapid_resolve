import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

/**
 * Admin user role: provides access to system-wide views, reports,
 * maintenance tasks, and user management utilities.
 */
public class Admin extends User {

    /**
     * Construct an Admin identity.
     * @param userId   admin id from DB
     * @param username admin username
     * @param password hashed password or placeholder (not used interactively here)
     */
    public Admin(int userId, String username, String password) {
        super(userId, username, password);
    }

    @Override
    /**
     * Render the admin menu loop and handle menu actions.
     * @param sc shared scanner from main application
     */
    public void showMenu(Scanner sc) {
        int choice = -1;

        do {
            String[] options = {
                    "1. View All Complaints",
                    "2. View All Crime Reports",
                    "3. Generate .txt Report",
                    "4. View Logs",
                    "5. Manage Users",
                    "6. System Settings",
                    "7. FAQs",
                    "8. View Feedback Rating Summary",
                    "9. Add Officer",
                    "10. Logout"
            };

            CLIUtils.printBoxedMenu("Admin Menu", options);
            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 10);

            switch (choice) {
                case 1:
                    CLIUtils.printInfo("Showing all complaints...");
                    ComplaintManager.viewAllComplaints();
                    break;
                case 2:
                    CLIUtils.printInfo("Showing all crime reports...");
                    CrimeManager.viewAllCrimes();
                    break;
                case 3:
                    handleReportGeneration();
                    break;
                case 4:
                    CLIUtils.printInfo("Viewing system logs...");
                    ActionTracker.viewLog();
                    ActionTracker.log("Admin", "Viewed logs");
                    break;
                case 5:
                    manageUsers(sc);
                    break;
                case 6:
                    systemSettings(sc);
                    break;
                case 7:
                    showFAQs();
                    break;
                case 8:
                    showFeedbackRatingsSummary();
                    break;
                case 9:
                    // Input validation before adding officer
                    String name;
                    while (true) {
                        System.out.print("Enter Officer Name: ");
                        name = sc.nextLine().trim();
                        if (name.matches("^[A-Za-z ]{2,50}$")) break;
                        CLIUtils.printError("Invalid name. Use letters and spaces only, at least 2 characters.");

                    }

                    String email;
                    while (true) {
                        System.out.print("Enter Officer Email: ");
                        email = sc.nextLine().trim();
                        if (email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) break;
                        CLIUtils.printError("Invalid email address.");

                    }

                    String phone;
                    while (true) {
                        System.out.print("Enter Officer Phone: ");
                        phone = sc.nextLine().trim();
                        if (phone.matches("^\\d{10}$")) break;
                        CLIUtils.printError("Phone no must be 10 digits.");

                    }

                    String area;
                    while (true) {
                        System.out.print("Enter Officer Area: ");
                        area = sc.nextLine().trim();
                        if (area.matches("^[A-Za-z0-9 .,-]{2,100}$")) break;
                        CLIUtils.printError("Area must be alphanumeric and at least 2 characters.");
                    }

                    OfficerManager.addOfficerWithEmail(name, email, phone, area);
                    break;

                case 10:
                    CLIUtils.printInfo("Logged out successfully.");
                    ActionTracker.log("Admin", "Logged out");
                    break;

                default:
                    CLIUtils.printError("Invalid choice. Please select 1-10.");
            }
            if (choice != 10) CLIUtils.waitForEnter();
        } while (choice != 10);
    }

    /**
     * Submenu to list citizens, and delete a citizen (with cascading clean-up for related data).
     * @param sc shared scanner
     */
    private void manageUsers(Scanner sc) {
        int choice = -1;
        do {
            String[] options = {
                    "1. View All Citizens",
                    "2. Delete a Citizen",
                    "3. Back to Admin Menu"
            };
            CLIUtils.printBoxedMenu("User Management", options);
            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 3);

            switch (choice) {
                case 1:
                    UserManager.viewAllUsers();
                    break;
                case 2:
                    int userIdToDelete = CLIUtils.promptInt(sc, "Enter Citizen User ID to delete: ", 1, Integer.MAX_VALUE);
                    try {
                        int helplineDeleted = DBConnection.executeUpdate(
                                "DELETE FROM helpline_requests WHERE user_id = ?", userIdToDelete);
                        CLIUtils.printInfo(helplineDeleted + " helpline record(s) deleted for user " + userIdToDelete);
                    } catch (Exception e) {
                        CLIUtils.printError("Error deleting helpline records: " + e.getMessage());
                        break;
                    }
                    try {
                        int userDeleted = UserManager.deleteUser(userIdToDelete);
                        if (userDeleted > 0) {
                            CLIUtils.printSuccess("User deleted successfully.");
                        } else {
                            CLIUtils.printError("User ID not found or already deleted.");
                        }
                    } catch (Exception e) {
                        CLIUtils.printError("Error deleting user: " + e.getMessage());
                    }
                    break;
                case 3:
                    return;
            }
            if (choice != 3) CLIUtils.waitForEnter();
        } while (choice != 3);
    }

    /**
     * System utilities for admins: clear logs, archive old complaints, show DB properties.
     * @param sc shared scanner
     */
    private void systemSettings(Scanner sc) {
        int choice = -1;
        do {
            String[] options = {
                    "1. Clear Action Log",
                    "2. Archive Old Complaints",
                    "3. View System Paths",
                    "4. Back to Admin Menu"
            };
            CLIUtils.printBoxedMenu("System Settings", options);
            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 4);

            switch (choice) {
                case 1:
                    CLIUtils.printInfo("Clearing action log...");
                    ActionTracker.clearLog();
                    ActionTracker.log("Admin", "Cleared the action log");
                    break;
                case 2:
                    CLIUtils.printInfo("Archiving old complaints...");
                    ReportGenerator.archiveOldComplaints();
                    ActionTracker.log("Admin", "Archived old complaints");
                    break;
                case 3:
                    viewSystemPaths();
                    break;
                case 4:
                    return;
            }
            if (choice != 4) CLIUtils.waitForEnter();
        } while (choice != 4);
    }

    /**
     * Print database driver details and connection information for diagnostics.
     */
    private void viewSystemPaths() {
        try {
            Connection con = DBConnection.connect();
            DatabaseMetaData dbmd = con.getMetaData();
            String[] database = {
                    "Driver Name: " + dbmd.getDriverName(),
                    "Driver Version: " + dbmd.getDriverVersion(),
                    "UserName is: " + dbmd.getUserName(),
                    "Product Name:" + dbmd.getDatabaseProductName(),
                    "Product Version:" + dbmd.getDatabaseProductVersion(),
                    "URL : " + dbmd.getURL()
            };
            CLIUtils.printBoxedMenu("DataBase Properties", database);
        } catch (Exception e) {
            CLIUtils.printError("Could not retrieve system path: " + e.getMessage());
        }
    }

    /**
     * Generate a full complaints TXT report to the working directory.
     */
    private void handleReportGeneration() {
        CLIUtils.printInfo("Generating report file...");
        try {
            ReportGenerator.generateAllComplaintsReport();
            ActionTracker.log("Admin", "Generated system-wide .txt report");
        } catch (Exception e) {
            CLIUtils.printError("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Print frequently asked questions for the Admin role.
     */
    private void showFAQs() {
        String[] faqs = {
                "Q: How do I generate a report?",
                "A: Use the 'Generate .txt Report' option.",
                "Q: How do I manage users?",
                "A: Use the 'Manage Users' option to view or delete citizens.",
                "Q: How do I export data?",
                "A: Use the 'Export Data' option to get a TXT file of all complaints.",
                "Q: How do I view system logs?",
                "A: Use the 'View Logs' option to see all system activities.",
                "Q: What are System Settings?",
                "A: This area contains maintenance tasks like clearing logs or archiving old data."
        };
        CLIUtils.printBoxedInfo("Admin FAQs", faqs);
    }

    /**
     * Show simple aggregate of feedback star ratings and compute an average.
     */
    private void showFeedbackRatingsSummary() {
        String sql = "SELECT stars, COUNT(*) as total FROM feedback GROUP BY stars ORDER BY stars DESC";

        int totalUsers = 0;
        int weightedSum = 0;

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            CLIUtils.printInfo("--- Feedback Rating Summary ---");
            while (rs.next()) {
                int stars = rs.getInt("stars");
                int count = rs.getInt("total");
                totalUsers += count;
                weightedSum += stars * count;
                String starDisplay = "★".repeat(stars) + "☆".repeat(5 - stars);
                CLIUtils.printInfo(stars + " " + starDisplay + " = " + count);
            }
            double average = totalUsers == 0 ? 0.0 : (double) weightedSum / totalUsers;
            CLIUtils.printInfo(String.format("Average rating: %.1f based on %d users", average, totalUsers));


        } catch (Exception e) {
            CLIUtils.printError("Error retrieving ratings summary: " + e.getMessage());
        }
    }
}
