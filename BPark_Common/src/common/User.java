package common;


/**
 * This class represents a user in the system.
 * It holds the user's ID and their role (e.g. Subscriber, Manager, Bouncer).
 */

public class User {
	// The user's ID (e.g. 012345678)
    private String userId;

    // The user's role (e.g. Subscriber, Manager, etc.)
    private String role;

    /**
     * Constructor to create a new User object with ID and role
     * @param userId the unique ID of the user
     * @param role the user's role
     */
    public User(String userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    /**
     * Get the user's ID
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the user's role
     * @return role
     */
    public String getRole() {
        return role;
    }

}
