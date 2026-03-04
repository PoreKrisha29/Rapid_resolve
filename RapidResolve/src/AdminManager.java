import java.sql.*;

/**
 * Admin authentication and retrieval utilities.
 */
public class AdminManager {

    /**
     * Authenticate an admin using username and hashed password.
     * @return Admin instance if credentials are valid; otherwise null
     */
    public static Admin getAdmin(String username, String password) {
        Admin admin = null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.connect();
            String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, username.trim());
            ps.setString(2, password.trim());  // hashed password string expected here
            rs = ps.executeQuery();

            if (rs.next()) {
                int adminId = rs.getInt("admin_id");
                admin = new Admin(adminId, username, password);
            }
        } catch (Exception e) {
            CLIUtils.printError("Error fetching admin: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
        return admin;
    }
}
