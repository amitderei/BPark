package common;

import java.io.Serializable;

/**
 * Represents a user in the BPARK system.
 * Each user has a username, password,
 * and a role that defines their permissions in the system.
 * This class is transferred between client and server,
 * but contains only the necessary and safe fields.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The username used for login (unique in the system) */
    private String username;

    /** The role of the user (e.g., Subscriber, Manager, Attendant) */
    private UserRole role;

    /** The user's password  */
    private String password;

    /**
     * Constructor used when creating a user with a username and role only.
     * Typically used when the password is not needed or already verified.
     *
     * @param username the login username
     * @param role     the user's role
     */
    public User(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    /**
     * Constructor used when handling login credentials (username + password).
     * Note: role is not included here.
     *
     * @param username the login username
     * @param password the login password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Constructor that builds a user with username, password, and role as a string.
     * Useful when role is received as a string (e.g., from the database).
     *
     * @param username the user's username
     * @param password the user's password
     * @param role     the user's role (as a string)
     */
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = UserRole.valueOf(role);
    }

    /**
     * Returns the user's login username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the user's role in the system.
     *
     * @return the UserRole enum
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Returns the user's password (if applicable).
     * Note: this field may be null depending on context.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns a short string summary of the user,
     * showing only the username and role (not the password).
     *
     * @return user details as a string
     */
    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", role=" + role +
               '}';
    }
}
