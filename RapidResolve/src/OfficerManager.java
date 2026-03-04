import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Random;


public class OfficerManager {

    public static Officer getOfficer(String username, String password) {
        Officer officer = null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.connect();

            // Hash the password before database comparison
            String hashedPassword = HashingForPassword.hashPassword(password);

            String sql = "SELECT * FROM officers WHERE username = ? AND password = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, username.trim());
            ps.setString(2, hashedPassword);
            rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("officer_id");
                int assignedCount = rs.getInt("assigned_count");
                String area = rs.getString("area");
                int mustChangePassword = rs.getInt("must_change_password");
                officer = new Officer(userId, username, password, assignedCount, area, mustChangePassword);
            }
        } catch (Exception e) {
            System.out.println("Error fetching officer: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
        return officer;
    }

    public static int assignOfficer(String area) {
        int officerId = -1;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (area == null || area.trim().isEmpty()) {
                System.out.println("❌ Area cannot be null or empty.");
                return -1;
            }

            con = DBConnection.connect();
            if (con == null) {
                System.out.println("❌ Database connection failed.");
                return -1;
            }

            String sql = "SELECT o.officer_id, COUNT(c.complaint_id) as complaint_count " +
                    "FROM officers o " +
                    "LEFT JOIN complaints c ON o.officer_id = c.officer_id " +
                    "GROUP BY o.officer_id " +
                    "ORDER BY complaint_count ASC " +
                    "LIMIT 1";

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                officerId = rs.getInt("officer_id");
            }

        } catch (SQLException e) {
            System.out.println("❌ Database error assigning officer: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error assigning officer: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("❌ Error closing database resources: " + e.getMessage());
            }
        }

        return officerId;
    }

    public static int getOfficerCount() {
        int count = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            String sql = "SELECT COUNT(*) FROM officers";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);

        } catch (Exception e) {
            System.out.println("❌ Error counting officers: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) { }
        }
        return count;
    }

    public static void viewAllOfficers() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            String sql = "SELECT * FROM officers";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            System.out.println("\n👮 All Officers:");
            while (rs.next()) {
                System.out.println("Officer ID: " + rs.getInt("officer_id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Assigned Count: " + rs.getInt("assigned_count"));
                System.out.println("-----------------------------");
            }

        } catch (Exception e) {
            System.out.println("❌ Error viewing officers: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) { }
        }
    }

    public static void viewOfficersByArea(String area) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean found = false;
        try {
            con = DBConnection.connect();
            String sql = "SELECT * FROM officers WHERE LOWER(area) LIKE ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + area.toLowerCase() + "%");
            rs = ps.executeQuery();
            System.out.println("\n👮 Officers in area/city: " + area);
            while (rs.next()) {
                found = true;
                System.out.println("Officer ID: " + rs.getInt("officer_id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Assigned Count: " + rs.getInt("assigned_count"));
                System.out.println("Area: " + rs.getString("area"));
                System.out.println("-----------------------------");
            }
            if (!found) {
                System.out.println("No officers found for area/city: " + area);
            }
        } catch (Exception e) {
            System.out.println("Error viewing officers: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception e) { }
        }
    }

    // ================== ADD OFFICER WITH EMAIL (FIXED) ==================
    public static void addOfficerWithEmail(String name, String email, String phone, String area) {
        try (Connection conn = DBConnection.connect()) {
            // Generate username & password
            String username = email.split("@")[0];
            String password = generateRandomPassword();
            String hashedPassword = hashPassword(password);

            conn.setAutoCommit(false); // Start transaction

            // Insert officer into officers table with all required fields including name
            String sql = "INSERT INTO officers (name, email, phone, username, password, area, assigned_count, must_change_password) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);                // Officer's full name
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setString(4, username);
                ps.setString(5, hashedPassword);
                ps.setString(6, area);
                ps.setInt(7, 0);                     // assigned_count = 0 initially
                ps.setInt(8, 1);                     // must_change_password = 1 on creation

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    CLIUtils.printSuccess("✅ Officer added successfully to DB.");
                    // Send credentials via email
                    sendEmail(email, name, username, password);
                } else {
                    conn.rollback();
                    CLIUtils.printError("❌ Failed to add officer to DB.");
                }
            }
        } catch (Exception e) {
            CLIUtils.printError("Error adding officer: " + e.getMessage());
            e.printStackTrace();
        }
        // No finally block needed - try-with-resources handles connection cleanup automatically
    }

    // ================== GENERATE RANDOM PASSWORD ==================
    private static String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        Random rand = new Random();
        char[] password = new char[8];
        for (int i = 0; i < 8; i++) {
            password[i] = chars.charAt(rand.nextInt(chars.length()));
        }
        return new String(password);
    }

    // ================== HASH PASSWORD (SHA-256) ==================
    private static String hashPassword(String password) {
        return HashingForPassword.hashPassword(password);
    }

    // ================== SEND EMAIL ==================
    private static void sendEmail(String to, String officerName, String username, String password) {
        // Mail server settings
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        // Add additional TLS properties to fix connection issues
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        final String from = "maninbuch13112006@gmail.com";  // Replace with your Gmail
        final String appPassword = "mjisogseixmlkxak";

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, appPassword);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject("Rapid Resolve - Officer Login Credentials");
            msg.setText("Dear " + officerName + ",\n\n"
                    + "Your officer account has been successfully created in Rapid Resolve.\n\n"
                    + "Username: " + username + "\n"
                    + "Password: " + password + "\n\n"
                    + "Please log in and change your password immediately for security.\n\n"
                    + "Regards,\nAdmin - Rapid Resolve");

            Transport.send(msg);
            CLIUtils.printSuccess("📧 Credentials sent successfully to " + to);
        } catch (MessagingException e) {
            CLIUtils.printError("❌ Failed to send email: " + e.getMessage());
        }
    }

    public static boolean updatePassword(int officerId, String newHashedPassword) {
        String sql = "UPDATE officers SET password = ?, must_change_password = 0 WHERE officer_id = ?";
        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setInt(2, officerId);

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
}