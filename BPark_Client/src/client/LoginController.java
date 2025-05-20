package client;

import common.User;
import db.DBController; // Import the class that handles DB access

public class LoginController {
	/**
     * Checks login credentials using the database.
     * @param userId - user's ID from the input field
     * @param password - user's password from the input field
     * @return User object if valid, or null if invalid
     */
    public static User logIn(String userId, String password) {
        return DBController.validateLogin(userId, password);
    }

}
