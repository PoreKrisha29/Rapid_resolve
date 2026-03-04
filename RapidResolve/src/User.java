import java.util.*;

/**
 * Base class for all authenticated roles (Citizen, Officer, Admin).
 * Stores common identity and profile attributes; role-specific classes
 * expose menus and additional fields as needed.
 */
public abstract class User {
    protected int userId;
    protected String username;
    protected String password;
    protected String name;
    protected String countryCode;
    protected String phone;
    protected String state;
    protected String city;
    protected String landmark;
    protected String houseNo;
    protected int age;
    protected String email;

    protected int assignedCount;
    protected String area;
    protected int mustChangePassword; // 1 = must change, 0 = no need to change

    /**
     * Construct with minimal identity (for Admin).
     */
    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    /**
     * Construct with officer fields.
     */
    public User(int userId, String username, String password, int assignedCount, String area) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = username;
        this.assignedCount = assignedCount;
        this.area = area;
    }

    /**
     * Construct with full citizen profile.
     */
    public User(int userId, String username, String password, String name, String countryCode, String phone,
                String state, String city, String landmark, String houseNo, int age, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.countryCode = countryCode;
        this.phone = phone;
        this.state = state;
        this.city = city;
        this.landmark = landmark;
        this.houseNo = houseNo;
        this.age = age;
        this.email = email;
    }

    // MODIFIED LINE: The abstract method now requires a Scanner parameter.
    /**
     * Entry point for role-specific interactive menus.
     */
    public abstract void showMenu(Scanner sc);

    // Getter methods
    /**
     * @return db identifier for the user
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @return username/login id
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return whether the user must change their password (used by Officer)
     */
    public int getMustChangePassword() {
        return mustChangePassword;
    }

    /**
     * Set the password-change flag (0/1) for the user (used by Officer).
     */
    public void setMustChangePassword(int mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}