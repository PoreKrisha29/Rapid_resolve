import java.util.*;
import java.sql.*;

/**
 * Citizen role. Represents a registered end-user who can file and track complaints,
 * submit suggestions, request helpline, view snapshots and manage profile.
 */
public class Citizen extends User {

    /**
     * Construct a Citizen with full profile information.
     * @param userId database user id
     * @param username unique username
     * @param password hashed password
     * @param name full name
     * @param countryCode phone country code (e.g. "+91")
     * @param phone phone number
     * @param state state
     * @param city city
     * @param landmark address landmark
     * @param houseNo house number
     * @param age age in years
     * @param email email address
     */
    public Citizen(int userId, String username, String password, String name, String countryCode, String phone,
                   String state, String city, String landmark, String houseNo, int age, String email) {
        super(userId, username, password, name, countryCode, phone, state, city, landmark, houseNo, age, email);
    }

    /**
     * Render the citizen dashboard and process actions until logout.
     * @param sc shared scanner from main application
     */
    public void showMenu(Scanner sc) {
        int choice = -1;

        do {
            String[] options = {
                    "1. Raise a Request/Suggestion",
                    "2. File a Complaint",
                    "3. Check Complaint Status",
                    "4. View Complaint Dashboard",
                    "5. Know Your Officer",
                    "6. Helpline Number",
                    "7. Snapshot (Current/History)",
                    "8. Update Profile",
                    "9. FAQs",
                    "10. Feedback",
                    "11. Logout"
            };
            CLIUtils.printBoxedMenu("Citizen Dashboard", options);

            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 11);

            switch (choice) {
                case 1:
                    handleSuggestionSubmission(sc);
                    break;
                case 2:
                    handleComplaintFiling(sc);
                    break;
                case 3:
                    CLIUtils.printInfo("Checking complaint status...");
                    ComplaintManager.showComplaintTracker(userId);
                    break;
                case 4:
                    CLIUtils.printInfo("Your Complaint Dashboard:");
                    ComplaintManager.viewComplaintsByUser(userId);
                    break;
                case 5:
                    handleOfficerLookup(sc);
                    break;
                case 6:
                    helplineFlow(sc);
                    break;
                case 7:
                    handleSnapshotView(sc);
                    break;
                case 8:
                    updateProfile(sc);
                    break;
                case 9:
                    showFAQs();
                    break;
                case 10:
                    handleFeedbackSubmission(sc);
                    break;
                case 11:
                    CLIUtils.printInfo("Logged out successfully.");
                    ActionTracker.log("Citizen_" + userId, "Logged out");
                    break;
                default:
                    CLIUtils.printError("Invalid choice. Please select 1-11.");
            }
            if (choice != 11) CLIUtils.waitForEnter();
        } while (choice != 11);
    }

    /**
     * Collect and submit a suggestion to a chosen department.
     */
    private void handleSuggestionSubmission(Scanner sc) {
        String[] Options = {
                "1. Engineering",
                "2. Food Safety",
                "3. Entomology",
                "4. Revenue(Property tax)",
                "5. Information Technology",
                "6. Health Statistics",
                "7. Safety",
                "8. Exit"
        };
        CLIUtils.printBoxedMenu("Suggestion Box:", Options);
        int choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 8);

        if (choice == 8) {
            return;
        }

        String suggestion = CLIUtils.promptString(sc, "Suggestion: ", true, 10, 200, null, null);
        try {
            Connection con = DBConnection.connect();
            String sql = "INSERT INTO suggestions (user_id, suggestion, type) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, suggestion);
            ps.setInt(3, choice);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                CLIUtils.printSuccess("✅ Suggestion submitted successfully!");
                ActionTracker.log("Citizen_" + userId, "Submitted suggestion");
            } else {
                CLIUtils.printError("❌ Failed to save suggestion.");
            }
            con.close();
        } catch (Exception e) {
            CLIUtils.printError("❌ Error while saving suggestion: " + e.getMessage());
        }
    }

    /**
     * Simulate a helpline request and create a record with the user's location.
     */
    private void helplineFlow(Scanner sc) {
        CLIUtils.printInfo("Helpline Numbers:");
        System.out.println("108 - Ambulance");
        System.out.println("101 - Fire Service");
        System.out.println("100 - Police");
        int choice = CLIUtils.promptInt(sc, "Enter Helpline Number: ", 100, 108);

        System.out.print("Confirm call to " + choice + "? (Y/N): ");
        String confirm = sc.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            CLIUtils.printInfo("Helpline request cancelled.");
            return;
        }

        String location = UserManager.getUserLocation(userId);

        try {
            DBConnection.executeUpdate(
                    "INSERT INTO helpline_requests (user_id, helpline_number, location, status) VALUES (?, ?, ?, ?)",
                    userId, String.valueOf(choice), location, getStatusForHelpline(choice)
            );
            CLIUtils.printSuccess("✅ Request sent! " + getServiceName(choice) + " is on its way to: " + location);
        } catch (Exception e) {
            CLIUtils.printError("Failed to send helpline request: " + e.getMessage());
        }
    }

    /**
     * Map a helpline number to an initial request status string.
     */
    private String getStatusForHelpline(int choice) {
        switch (choice) {
            case 108: return "Ambulance Dispatched";
            case 101: return "Fire Service Dispatched";
            case 100: return "Police Dispatched";
            default: return "Pending";
        }
    }

    /**
     * Human-readable service name for a helpline number.
     */
    private String getServiceName(int choice) {
        switch (choice) {
            case 108: return "Ambulance";
            case 101: return "Fire Service";
            case 100: return "Police";
            default: return "Service";
        }
    }

    /**
     * Update profile fields (phone, email, or city) with validation.
     */
    private void updateProfile(Scanner sc) {
        int choice = -1;
        do {
            CLIUtils.printInfo("\n--- Update Your Profile ---");
            String[] updateOptions = {
                    "1. Update Phone Number",
                    "2. Update Email Address",
                    "3. Update City",
                    "4. Back to Dashboard"
            };
            CLIUtils.printBoxedMenu("Update Profile", updateOptions);
            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 4);

            if (choice == 4) {
                return;
            }

            String newValue = "";
            switch (choice) {
                case 1:
                    newValue = CLIUtils.promptString(sc, "Enter new Phone Number (10 digits): ", true, 10, 10, "^\\d{10}$", "Phone number must be exactly 10 digits.");
                    break;
                case 2:
                    newValue = CLIUtils.promptString(sc, "Enter new Email Address: ", true, 5, 50, "^[A-Za-z0-9+_.-]+@(.+)$", "Please enter a valid email address.");
                    break;
                case 3:
                    newValue = CLIUtils.promptString(sc, "Enter new City: ", true, 2, 30, null, null);
                    break;
            }

            UserManager.updateUserProfile(this.userId, choice, newValue);
            CLIUtils.waitForEnter();

        } while (choice != 4);
    }


    /**
     * Drive complaint filing workflow and generate a QR code for the latest complaint.
     */
    private void handleComplaintFiling(Scanner sc) {
        CLIUtils.printInfo("Choose Complaint Type:");
        CLIUtils.printInfo("1. Civil");
        CLIUtils.printInfo("2. Criminal");
        int complaintType = CLIUtils.promptInt(sc, "Enter type (1-2): ", 1, 2);
        String type = (complaintType == 1) ? "Civil" : "Criminal";
        try {
            Connection con = DBConnection.connect();
            String officerCheck = "SELECT COUNT(*) FROM officers WHERE area = ?";
            PreparedStatement ps = con.prepareStatement(officerCheck);
            ps.setString(1, this.city);   // using citizen's city
            var rs = ps.executeQuery();

            boolean officersAvailable = false;
            if (rs.next() && rs.getInt(1) > 0) {
                officersAvailable = true;
            }

            if (!officersAvailable) {
                CLIUtils.printError("Sorry for your inconvenience!! Rapid resolve officers are not available in your area. Will connect with you very soon.");
                return; // stop filing
            }

            ComplaintManager.fileComplaint(userId, type);
            int latestComplaintId = ComplaintManager.getLatestComplaintId(userId);
            if (latestComplaintId > 0) {
                CLIUtils.printInfo("Generating QR Code for your complaint...");
                QRGenerator.generateQRCode(latestComplaintId);
            } else {
                CLIUtils.printError("Failed to file complaint. Please try again.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error filing complaint: " + e.getMessage());
        }
    }

    /**
     * Prompt for area/city and display matching officers.
     */
    private void handleOfficerLookup(Scanner sc) {
        String area = CLIUtils.promptString(sc, "Enter area/city to view officer details: ", true, 2, 40, null, null);
        OfficerManager.viewOfficersByArea(area);
    }

    /**
     * Show crime snapshots for different time windows.
     */
    private void handleSnapshotView(Scanner sc) {
        CLIUtils.printInfo("Snapshot Options:");
        CLIUtils.printInfo("1. Current");
        CLIUtils.printInfo("2. Incident History (6 months)");
        CLIUtils.printInfo("3. Incident History (3 months)");
        CLIUtils.printInfo("4. Incident History (15 days)");
        int snap = CLIUtils.promptInt(sc, "Enter option (1-4): ", 1, 4);
        try {
            CrimeManager.showSnapshot(snap);
        } catch (Exception e) {
            CLIUtils.printError("Error loading snapshot: " + e.getMessage());
        }
    }

    /**
     * Print frequently asked questions for the Citizen role.
     */
    private void showFAQs() {
        String[] faqs = {
                "Q: How do I file a complaint?",
                "A: Use the 'File a Complaint' option and follow the prompts.",
                "Q: How do I check my complaint status?",
                "A: Use the 'Check Complaint Status' option.",
                "Q: Who can see my complaints?",
                "A: Only authorized officers and admins.",
                "Q: How long does it take to resolve a complaint?",
                "A: Resolution time varies based on complaint type and complexity.",
                "Q: Can I update my complaint details?",
                "A: Contact your assigned officer for any updates."
        };
        CLIUtils.printBoxedInfo("FAQs", faqs);
    }

    /**
     * Collect and persist star rating and feedback text for the citizen.
     */
    private void handleFeedbackSubmission(Scanner sc) {
        System.out.println("Rate Your Experience (1–5):");
        System.out.println("1 → ★☆☆☆☆");
        System.out.println("2 → ★★☆☆☆");
        System.out.println("3 → ★★★☆☆");
        System.out.println("4 → ★★★★☆");
        System.out.println("5 → ★★★★★");
        int stars = CLIUtils.promptInt(sc, "Select stars: ", 1, 5);
        CLIUtils.printSuccess("You selected " + "★".repeat(stars) + "☆".repeat(5 - stars));
        String feedback = CLIUtils.promptString(sc, "Write your feedback: ", true, 5, 200, null, null);

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO feedback (user_id, feedback, stars) VALUES (?, ?, ?)")) {
            ps.setInt(1, userId);
            ps.setString(2, feedback);
            ps.setInt(3, stars);
            ps.executeUpdate();
            CLIUtils.printSuccess("Thank you! Your feedback with rating "
                    + "★".repeat(stars) + "☆".repeat(5 - stars) + " has been recorded.");
            ActionTracker.log("Citizen_" + userId, "Submitted feedback and rating");
        } catch (Exception e) {
            CLIUtils.printError("❌ Error while saving feedback: " + e.getMessage());
        }
    }
}