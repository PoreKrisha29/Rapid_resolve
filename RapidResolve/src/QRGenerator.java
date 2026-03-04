import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import java.sql.*;

/**
 * QR code generation utilities for complaints and user profiles.
 * Integrates with ZXing to render PNG files and opens them on Windows.
 */
public class QRGenerator {

    /**
     * MODIFIED METHOD
     * This method now joins with the 'officers' table to get the assigned officer's name.
     */
    public static void generateQRCode(int complaintId) {
        String qrContent = "";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed. Cannot generate QR code details.");
                return;
            }

            // MODIFIED: SQL query now joins all three tables (complaints, users, officers).
            String sql = "SELECT c.description, c.filed_on, c.status, " +
                    "u.user_id, u.name as citizen_name, " +
                    "o.username as officer_name " +
                    "FROM complaints c " +
                    "JOIN users u ON c.user_id = u.user_id " +
                    "JOIN officers o ON c.officer_id = o.officer_id " +
                    "WHERE c.complaint_id = ?";

            ps = con.prepareStatement(sql);
            ps.setInt(1, complaintId);
            rs = ps.executeQuery();

            if (rs.next()) {
                String citizenName = rs.getString("citizen_name");
                int userId = rs.getInt("user_id");
                String description = rs.getString("description");
                String filedOn = rs.getString("filed_on");
                String status = rs.getString("status");
                // MODIFIED: Retrieve the officer's name.
                String officerName = rs.getString("officer_name");

                // MODIFIED: Restructured the content string to look more professional.
                qrContent = "--- RapidResolve Complaint Summary ---\n\n" +
                        "Complaint ID: " + complaintId + "\n" +
                        "Status: " + status + "\n\n" +
                        "Filed By: " + citizenName + " (ID: " + userId + ")\n" +
                        "Filed On: " + filedOn + "\n\n" +
                        "Description:\n" + description + "\n\n" +
                        "Assigned Officer: " + officerName;

            } else {
                CLIUtils.printError("Could not find details for complaint ID: " + complaintId);
                return;
            }

            String filename = "Complaint_" + complaintId + "_QR.png";
            int width = 400;
            int height = 400;

            BitMatrix matrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, width, height);
            Path path = new File(filename).toPath();
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);

            CLIUtils.printSuccess("✅ QR Code generated successfully: " + filename);
            openFileInWindows(filename);

        } catch (Exception e) {
            CLIUtils.printError("Error generating QR code: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) {
                CLIUtils.printError("Error closing DB resources in QRGenerator: " + e.getMessage());
            }
        }
    }

    /**
     * Generate a simple QR code for a citizen profile card.
     */
    public static void generateQRForUser(int userId) {
        String qrContent = "";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            if (con == null) {
                CLIUtils.printError("Database connection failed. Cannot generate QR code details.");
                return;
            }

            String sql = "SELECT username, name, email, phone, city FROM users WHERE user_id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String city = rs.getString("city");

                qrContent = "--- RapidResolve Citizen Profile ---\n" +
                        "User ID: " + userId + "\n" +
                        "Username: " + username + "\n" +
                        "Full Name: " + name + "\n" +
                        "Email: " + email + "\n" +
                        "Phone: " + phone + "\n" +
                        "City: " + city;
            } else {
                CLIUtils.printError("Could not find details for user ID: " + userId);
                return;
            }

            String filename = "User_" + userId + "_QR.png";
            generateQRFile(filename, qrContent);

            CLIUtils.printSuccess("✅ User QR Code generated successfully: " + filename);
            openFileInWindows(filename);

        } catch (Exception e) {
            CLIUtils.printError("Error generating user QR code: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) {
                CLIUtils.printError("Error closing DB resources in QRGenerator: " + e.getMessage());
            }
        }
    }

    /**
     * Render and write a QR code PNG to disk for the given content.
     */
    private static void generateQRFile(String filename, String content) {
        try {
            int width = 300;
            int height = 300;

            BitMatrix matrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            Path path = new File(filename).toPath();
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        } catch (Exception e) {
            CLIUtils.printError("QR Code generation failed: " + e.getMessage());
        }
    }

    /**
     * Best-effort attempt to open the generated file on Windows desktop.
     */
    private static void openFileInWindows(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                CLIUtils.printError("QR file does not exist to open.");
            }
        } catch (Exception e) {
            CLIUtils.printError("Error opening QR file: " + e.getMessage());
        }
    }
}