import java.sql.*;

/**
 * Minimal JDBC helper for obtaining connections and executing single updates.
 * Centralizes the database URL and credentials used throughout the app.
 */
public class DBConnection {
    /**
     * Simple connectivity check: prints "success" if a connection can be created.
     */
    public static void main(String[] args) throws Exception {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/rapidresolve_final", "root", "");
        System.out.println((con != null) ? "success" : "failed");
    }

    /**
     * Obtain a new JDBC connection to the RapidResolve schema.
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/rapidresolve_final", "root", "");
    }

    /**
     * Execute a single SQL update with positional parameters.
     * @param sql parameterized SQL (e.g. INSERT/UPDATE/DELETE)
     * @param params sequential parameters bound to the prepared statement
     * @return number of affected rows
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate(); // Return number of affected rows
        }
    }
}
