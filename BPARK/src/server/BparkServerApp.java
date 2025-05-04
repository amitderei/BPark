package server;

/**
 * Entry point for launching the server-side application of the BPARK system.
 * Initializes the server on a specified port and starts listening for client connections.
 */
public class BparkServerApp {

    /**
     * Main method that starts the server.
     * Creates an instance of PrototypeServer and begins listening for clients on port 5555.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        BparkServer server = new BparkServer(5555);
        try {
            server.listen(); // Starts the OCSF server listening on the specified port
        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
