package common;

import java.io.Serializable;

/**
 * Represents a user in the BPARK system.
 * Contains only safe information to transfer between server and client.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;     // Unique username used for login
    private UserRole role;       // User role (e.g., Subscriber, Manager, Attendant)
    private String password;

    /**
     * Constructs a new User object.
     *
     * @param username the username of the user
     * @param role     the user's role (enum)
     */
    public User(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }
    
    public User(String username, String password) {
    	this.username=username;
    	this.password=password;
    }
    
    /**
     * Constructs a new User with username, password, and role string.
     *
     * @param username the user's username
     * @param password the user's password
     * @param role     the role as string (e.g., "Subscriber")
     */
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = UserRole.valueOf(role);
    }

    /**
     * Returns the user's username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the user's role.
     *
     * @return the UserRole (enum)
     */
    public UserRole getRole() {
        return role;
    }
    
    
    /**
     *  returns password of user.
     * @return password of user
     */
    public String getPassword() {
		return password;
	}

	/**
     * String representation of the user for debugging/logging.
     */
    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", role=" + role +
               '}';
    }
}

