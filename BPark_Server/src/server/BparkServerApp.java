package server;

/**
 * Entry point for launching the BPARK server application.
 * This class is responsible for starting the server, binding it to a port,
 * and initializing the listening process using the OCSF framework.
 */
public class BparkServerApp {

    /**
     * Main method – the starting point of the server application.
     * Creates an instance of BparkServer bound to port 5555 and starts listening for client connections.
     *
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        // Create a new server instance bound to port 5555
        BparkServer server = new BparkServer(5555);

        try {
            // Start the OCSF server to begin listening for incoming client connections
            server.listen();
        } catch (Exception e) {
            // Print an error message if the server fails to start
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
