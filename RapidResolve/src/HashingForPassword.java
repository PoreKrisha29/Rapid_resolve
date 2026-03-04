import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password hashing and one-off migration helper.
 * - Provides SHA-256 hashing used across the app
 * - Includes a main() tool to migrate plaintext passwords to hashed values
 */
public class HashingForPassword {

    /**
     * Compute a SHA-256 hex string for the provided input.
     * @param input plaintext string
     * @return 64-character lowercase hexadecimal digest
     */
    public static String hashPassword(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: SHA-256 not supported", e);
        }
    }

    /**
     * Command-line migration tool: iterate rows for key tables and replace any
     * non-hashed passwords with SHA-256 hashes in-place.
     */
    public static void main(String[] args) {
        migrateTablePasswords("users", "user_id", "password");
        migrateTablePasswords("officers", "officer_id", "password");
        migrateTablePasswords("admins", "admin_id", "password");
    }

    /**
     * Migrate a single table's password column from plaintext to SHA-256.
     * Skips rows that already contain a 64-char hex digest.
     */
    private static void migrateTablePasswords(String tableName, String idColumn, String passwordColumn) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = DBConnection.connect();
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT " + idColumn + ", " + passwordColumn + " FROM " + tableName);

            int updatedCount = 0;
            while (rs.next()) {
                String plainPassword = rs.getString(passwordColumn);
                if (plainPassword != null && !plainPassword.isEmpty() && !isSHA256Hash(plainPassword)) {
                    String hashedStr = hashPassword(plainPassword);
                    rs.updateString(passwordColumn, hashedStr);
                    rs.updateRow();
                    System.out.println("Updated " + tableName + " ID: " + rs.getInt(idColumn));
                    updatedCount++;
                }
            }

            System.out.println("Total passwords updated in table '" + tableName + "': " + updatedCount);

        } catch (Exception e) {
            System.out.println("Error migrating passwords in " + tableName + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if(rs != null) rs.close();
                if(stmt != null) stmt.close();
                if(con != null) con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Detect whether a string looks like a 64-character lowercase hex digest.
     */
    private static boolean isSHA256Hash(String password) {
        // SHA-256 produces a 64-character hexadecimal string
        return password != null && password.matches("[0-9a-f]{64}");
    }
}
