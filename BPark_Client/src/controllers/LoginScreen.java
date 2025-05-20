package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import client.LoginController;
import common.User;


public class LoginScreen {
	
	 // This field connects to the TextField in the FXML (fx:id="username")
    @FXML
    private TextField username;

    // This field connects to the PasswordField in the FXML (fx:id="code")
    @FXML
    private PasswordField code;

    // This connects to the login button (fx:id="submit")
    @FXML
    private Button submit;

    // Label to show error messages if login fails
    @FXML
    private Label lblError;

    // This method is called when the login button is clicked
    @FXML
    private void handleLoginClick() {
        // Get user input from the text fields
        String userId = username.getText();
        String password = code.getText();

        // Call the LoginController to validate the credentials
        User user = LoginController.logIn(userId, password);

        if (user != null) {
            // If login is successful, move to the correct screen based on role
            redirectToMain(user.getRole());
        } else {
            // If login fails, show an error message
            lblError.setText("Invalid ID or password.");
        }
    }

    // This method is called when the user clicks "Login as Guest"
    @FXML
    private void redirectToMainGuest() {
        redirectToMain("Guest");
    }

    // This method will load the correct screen depending on the user's role
    private void redirectToMain(String role) {
        // You will implement screen switching later
        System.out.println("Redirecting to main screen for role: " + role);
    }

}
