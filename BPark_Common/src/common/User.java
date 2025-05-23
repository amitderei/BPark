package common;

import java.io.Serializable;

/**
 * Represents a user in the BPARK system.
 * Implements Serializable to support client-server communication.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String password;
    private UserRole role;

    /**
     * Constructs a new User object.
     *
     * @param userId   the user's unique ID
     * @param password the user's password
     * @param role     the user's role (enum)
     */
    public User(String userId, String password, UserRole role) {
        this.userId = userId;
        this.password = password;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "User{" +
               "userId='" + userId + '\'' +
               ", role=" + role +
               '}';
    }
}

