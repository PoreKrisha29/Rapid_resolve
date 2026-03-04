import java.util.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utilities to export TXT reports for auditing and archival.
 * - All complaints report for admin
 * - Officer-specific complaints report
 * - Archive old resolved/closed complaints to a dated file and delete from DB
 */
public class ReportGenerator {

    /**
     * Generate a TXT file `All_Complaints_Report.txt` containing all complaints and
     * basic citizen metadata, ordered by filed date (desc).
     */
    public static void generateAllComplaintsReport() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("All_Complaints_Report.txt"))) {
            con = DBConnection.connect();
            String sql = "SELECT c.*, u.name as user_name, u.phone as user_phone FROM complaints c " +
                    "LEFT JOIN users u ON c.user_id = u.user_id " +
                    "ORDER BY c.filed_on DESC";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            writer.write("=".repeat(80));
            writer.newLine();
            writer.write("                    RAPIDRESOLVE - ALL COMPLAINTS REPORT");
            writer.newLine();
            writer.write("=".repeat(80));
            writer.newLine();
            writer.write("Generated on: " + LocalDate.now());
            writer.newLine();
            writer.write("Total Complaints: " + ComplaintManager.getTotalComplaintCount());
            writer.newLine();
            writer.write("-".repeat(80));
            writer.newLine();

            while (rs.next()) {
                writer.write("Complaint ID: " + rs.getInt("complaint_id"));
                writer.newLine();
                writer.write("Filed by: " + rs.getString("user_name") + " (" + rs.getString("user_phone") + ")");
                writer.newLine();
                writer.write("Area: " + rs.getString("area"));
                writer.newLine();
                writer.write("Type: " + rs.getString("type"));
                writer.newLine();
                writer.write("Description: " + rs.getString("description"));
                writer.newLine();
                writer.write("Status: " + rs.getString("status"));
                writer.newLine();
                writer.write("Officer ID: " + rs.getInt("officer_id"));
                writer.newLine();
                writer.write("Filed on: " + rs.getString("filed_on"));
                writer.newLine();
                writer.write("-".repeat(40));
                writer.newLine();
            }

            CLIUtils.printSuccess("✅ Report generated successfully: All_Complaints_Report.txt");

        } catch (Exception e) {
            CLIUtils.printError("Error generating report: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Generate a TXT report listing an officer's assigned complaints.
     */
    public static void generateOfficerComplaintsReport(int officerId, String officerName) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String filename = "Officer_" + officerId + "_Report.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            con = DBConnection.connect();
            String sql = "SELECT * FROM complaints WHERE officer_id = ? ORDER BY filed_on DESC";
            ps = con.prepareStatement(sql);
            ps.setInt(1, officerId);
            rs = ps.executeQuery();

            writer.write("=".repeat(60));
            writer.newLine();
            writer.write("           RAPIDRESOLVE - OFFICER COMPLAINTS REPORT");
            writer.newLine();
            writer.write("=".repeat(60));
            writer.newLine();
            writer.write("Officer ID: " + officerId);
            writer.newLine();
            writer.write("Officer Name: " + officerName);
            writer.newLine();
            writer.write("Generated on: " + LocalDate.now());
            writer.newLine();
            writer.write("-".repeat(60));
            writer.newLine();

            boolean hasComplaints = false;
            while (rs.next()) {
                hasComplaints = true;
                writer.write("Complaint ID: " + rs.getInt("complaint_id"));
                writer.newLine();
                writer.write("Area: " + rs.getString("area"));
                writer.newLine();
                writer.write("Type: " + rs.getString("type"));
                writer.newLine();
                writer.write("Description: " + rs.getString("description"));
                writer.newLine();
                writer.write("Status: " + rs.getString("status"));
                writer.newLine();
                writer.write("Filed on: " + rs.getString("filed_on"));
                writer.newLine();
                writer.write("-".repeat(30));
                writer.newLine();
            }

            if (!hasComplaints) {
                writer.write("No complaints assigned to this officer.");
                writer.newLine();
            }

            CLIUtils.printSuccess("✅ Report generated successfully: " + filename);

        } catch (Exception e) {
            CLIUtils.printError("Database error generating officer report: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Archive resolved/closed complaints older than 1 year to a dated TXT file
     * and delete them from the database in a single transaction.
     */
    public static void archiveOldComplaints() {
        Connection con = null;
        PreparedStatement psSelect = null;
        ResultSet rs = null;
        List<Integer> idsToDelete = new ArrayList<>();

        String dateStamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "Archived_Complaints_" + dateStamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            con = DBConnection.connect();
            con.setAutoCommit(false);

            String selectSql = "SELECT * FROM complaints WHERE (status = 'RESOLVED' OR status = 'CLOSED') AND filed_on <= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)";
            psSelect = con.prepareStatement(selectSql);
            rs = psSelect.executeQuery();

            if (!rs.isBeforeFirst()) { // no data
                CLIUtils.printInfo("No complaints older than one year found to archive.");
                con.rollback();
                return;
            }

            writer.write("Complaints Archived on: " + dateStamp);
            writer.newLine();
            writer.write("========================================");
            writer.newLine();

            while (rs.next()) {
                int complaintId = rs.getInt("complaint_id");
                idsToDelete.add(complaintId);

                writer.write("Complaint ID: " + complaintId);
                writer.newLine();
                writer.write("User ID     : " + rs.getInt("user_id"));
                writer.newLine();
                writer.write("Status      : " + rs.getString("status"));
                writer.newLine();
                writer.write("Filed On    : " + rs.getString("filed_on"));
                writer.newLine();
                writer.write("Description : " + rs.getString("description"));
                writer.newLine();
                writer.write("------------------------------------");
                writer.newLine();
            }

            if (!idsToDelete.isEmpty()) {
                String deleteSql = "DELETE FROM complaints WHERE complaint_id IN (";
                for (int i = 0; i < idsToDelete.size(); i++) {
                    deleteSql += idsToDelete.get(i);
                    if (i != idsToDelete.size() - 1) {
                        deleteSql += ",";
                    }
                }
                deleteSql += ")";

                Statement stmtDelete = con.createStatement();
                int deletedRows = stmtDelete.executeUpdate(deleteSql);
                stmtDelete.close();

                con.commit();
                CLIUtils.printSuccess("✅ Successfully archived and deleted " + deletedRows + " old complaints.");
                CLIUtils.printInfo("Archive file created: " + filename);
            } else {
                con.rollback();
                CLIUtils.printInfo("No complaints were archived.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error archiving complaints: " + e.getMessage());
            try {
                if (con != null) con.rollback();
            } catch (Exception ex) {
                CLIUtils.printError("Error rolling back transaction: " + ex.getMessage());
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (psSelect != null) psSelect.close();
                if (con != null) con.close();
            } catch (Exception e) {}
        }
    }
}
