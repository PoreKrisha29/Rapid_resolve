import java.util.Scanner;

/**
 * Officer role. Handles assigned complaints and crime records within a specified area.
 */
public class Officer extends User {

    /**
     * Construct an Officer without password-change enforcement flag (defaults to 0).
     */
    public Officer(int userId, String username, String password, int assignedCount, String area) {
        super(userId, username, password, assignedCount, area);
        this.mustChangePassword = 0; // Default value
    }

    /**
     * Construct an Officer with an explicit "must change password" flag.
     */
    public Officer(int userId, String username, String password, int assignedCount, String area, int mustChangePassword) {
        super(userId, username, password, assignedCount, area);
        this.mustChangePassword = mustChangePassword;
    }

    /**
     * Render officer dashboard and handle actions until logout.
     */
    public void showMenu(Scanner sc) {
        int choice = -1;

        do {
            String[] options = {
                    "1. View Assigned Complaints",
                    "2. Update Complaint Status",
                    "3. File Crime Record",
                    "4. View Crime Records",
                    "5. View Profile",
                    "6. Generate Officer Report",
                    "7. FAQs",
                    "8. Logout"
            };
            CLIUtils.printBoxedMenu("Officer Menu", options);

            choice = CLIUtils.promptInt(sc, "Enter your choice: ", 1, 8);

            switch (choice) {
                case 1:
                    CLIUtils.printInfo("Viewing assigned complaints...");
                    ComplaintManager.viewComplaintsByOfficer(userId);
                    break;
                case 2:
                    CLIUtils.printInfo("Updating complaint status...");
                    ComplaintManager.updateComplaintStatus(userId);
                    break;
                case 3:
                    CLIUtils.printInfo("Filing new crime record...");
                    CrimeManager.fileCrime(userId);

                    break;
                case 4:
                    CLIUtils.printInfo("Viewing crime records...");
                    CrimeManager.viewCrimesByOfficer(userId);
                    break;
                case 5:
                    viewProfile();
                    break;
                case 6:
                    generateReport();
                    break;
                case 7:
                    showFAQs();
                    break;
                case 8:
                    CLIUtils.printInfo("Logged out successfully.");
                    ActionTracker.log("Officer_" + userId, "Logged out");


                    break;
                default:
                    CLIUtils.printError("Invalid choice. Please select 1-8.");
            }
            if (choice != 8) CLIUtils.waitForEnter();
        } while (choice != 8);
    }

    /**
     * Print the officer's own profile summary.
     */
    private void viewProfile() {
        CLIUtils.printInfo("Viewing your profile...");
        String[] profileDetails = {
                "Officer ID: " + this.userId,
                "Officer Name: " + this.username,
                "Assigned Complaints: " + this.assignedCount,
                "Assigned Area: " + this.area
        };
        CLIUtils.printBoxedInfo("Your Profile", profileDetails);
        ActionTracker.log("Officer_" + userId, "Viewed own profile");
    }

    /**
     * Generate a TXT report listing all complaints assigned to this officer.
     */
    private void generateReport() {
        CLIUtils.printInfo("Generating officer report...");
        try {
            ReportGenerator.generateOfficerComplaintsReport(this.userId, this.username);
            ActionTracker.log("Officer_" + userId, "Generated personal complaints report");
        } catch (Exception e) {
            CLIUtils.printError("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Print frequently asked questions for the Officer role.
     */
    private void showFAQs() {
        String[] faqs = {
                "Q: How do I update complaint status?",
                "A: Use the 'Update Complaint Status' option and follow prompts.",
                "Q: How do I file a crime record?",
                "A: Use the 'File Crime Record' option.",
                "Q: How do I view my profile?",
                "A: Use the 'View Profile' option.",
                "Q: How are complaints assigned to me?",
                "A: Complaints are assigned based on area and workload.",
                "Q: Can I reassign complaints?",
                "A: Contact admin for complaint reassignment."
        };
        CLIUtils.printBoxedInfo("Officer FAQs", faqs);
    }
}

