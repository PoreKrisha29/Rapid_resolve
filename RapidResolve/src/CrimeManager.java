import java.sql.*;
import java.util.*;

/**
 * Crime operations facade using MySQL Stored Procedures:
 * - File and view crimes by officer
 * - View all crimes (admin)
 * - Show snapshot/history reports by time window
 * - Provide total count for dashboards
 */
public class CrimeManager {

    // ✅ 1. Officer files a new crime record   
    public static void fileCrime(int officerId) {
        Scanner sc = new Scanner(System.in);
        CLIUtils.printPrompt("Enter Crime Title: ");
        String title = sc.nextLine().trim();
        if (title.isEmpty() || title.length() < 5) {
            CLIUtils.printError("Crime title must be at least 5 characters long.");
            return;
        }

        CLIUtils.printPrompt("Enter Description: ");
        String desc = sc.nextLine().trim();
        if (desc.isEmpty() || desc.length() < 10) {
            CLIUtils.printError("Description must be at least 10 characters long.");
            return;
        }

        CLIUtils.printPrompt("Enter Location: ");
        String location = sc.nextLine().trim();
        if (location.isEmpty() || location.length() < 3) {
            CLIUtils.printError("Location must be at least 3 characters long.");
            return;
        }

        String date = java.time.LocalDate.now().toString();

        try (Connection con = DBConnection.connect()) {
            CallableStatement cs = con.prepareCall("{CALL file_crime(?, ?, ?, ?, ?)}");
            cs.setInt(1, officerId);
            cs.setString(2, title);
            cs.setString(3, desc);
            cs.setString(4, location);
            cs.setString(5, date);

            int rows = cs.executeUpdate();
            if (rows > 0) {
                CLIUtils.printSuccess("✅ Crime record filed successfully!");
                ActionTracker.log("Officer_" + officerId, "Filed crime: " + title + " at " + location);
            } else {
                CLIUtils.printError("Failed to file record.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error: " + e.getMessage());
        }
    }

    // ✅ 2. Officer views all crimes filed by them
    public static void viewCrimesByOfficer(int officerId) {
        try (Connection con = DBConnection.connect()) {
            CallableStatement cs = con.prepareCall("{CALL get_crimes_by_officer(?)}");
            cs.setInt(1, officerId);
            ResultSet rs = cs.executeQuery();

            CLIUtils.printInfo("\n📂 Crimes Filed by You:");
            while (rs.next()) {
                CLIUtils.printInfo("Crime ID     : " + rs.getInt("crime_id"));
                CLIUtils.printInfo("Title        : " + rs.getString("title"));
                CLIUtils.printInfo("Description  : " + rs.getString("description"));
                CLIUtils.printInfo("Location     : " + rs.getString("location"));
                CLIUtils.printInfo("Date Reported: " + rs.getString("date_reported"));
                CLIUtils.printInfo("-----------------------------");
            }

            ActionTracker.log("Officer_" + officerId, "Viewed all crimes filed by self");

        } catch (Exception e) {
            CLIUtils.printError("Error viewing crimes: " + e.getMessage());
        }
    }

    // ✅ 3. Admin views all crimes
    public static void viewAllCrimes() {
        try (Connection con = DBConnection.connect()) {
            CallableStatement cs = con.prepareCall("{CALL get_all_crimes()}");
            ResultSet rs = cs.executeQuery();

            CLIUtils.printInfo("\n📁 All Crime Records in System:");
            while (rs.next()) {
                CLIUtils.printInfo("Crime ID     : " + rs.getInt("crime_id"));
                CLIUtils.printInfo("Officer ID   : " + rs.getInt("officer_id"));
                CLIUtils.printInfo("Title        : " + rs.getString("title"));
                CLIUtils.printInfo("Description  : " + rs.getString("description"));
                CLIUtils.printInfo("Location     : " + rs.getString("location"));
                CLIUtils.printInfo("Date Reported: " + rs.getString("date_reported"));
                CLIUtils.printInfo("----------------------------------");
            }

            ActionTracker.log("Admin", "Viewed all crime records");

        } catch (Exception e) {
            CLIUtils.printError("Error viewing all crimes: " + e.getMessage());
        }
    }

    // ✅ 4. Show snapshot/history
    public static void showSnapshot(int option) {
        try (Connection con = DBConnection.connect()) {
            CallableStatement cs = con.prepareCall("{CALL get_crimes_snapshot(?)}");
            cs.setInt(1, option);
            ResultSet rs = cs.executeQuery();

            CLIUtils.printInfo("\nSnapshot Report:");
            while (rs.next()) {
                CLIUtils.printInfo("Crime ID     : " + rs.getInt("crime_id"));
                CLIUtils.printInfo("Officer ID   : " + rs.getInt("officer_id"));
                CLIUtils.printInfo("Title        : " + rs.getString("title"));
                CLIUtils.printInfo("Description  : " + rs.getString("description"));
                CLIUtils.printInfo("Location     : " + rs.getString("location"));
                CLIUtils.printInfo("Date Reported: " + rs.getString("date_reported"));
                CLIUtils.printInfo("-----------------------------");
            }

        } catch (Exception e) {
            CLIUtils.printError("Error fetching snapshot: " + e.getMessage());
        }
    }

    // ✅ 5. Total crimes count
    public static int getTotalCrimeCount() {
        int count = 0;
        try (Connection con = DBConnection.connect()) {
            CallableStatement cs = con.prepareCall("{CALL get_total_crime_count()}");
            ResultSet rs = cs.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) {
            CLIUtils.printError("Error counting crimes: " + e.getMessage());
        }
        return count;
    }
}
