package server;

/**
 * Main class that starts the BPARK server using OCSF.
 */
public class ServerApp {
    public static void main(String[] args) {
        int port = 5555; // default port
        PrototypeServer server = new PrototypeServer(port);
        try {
            server.listen();
        } catch (Exception e) {
            System.out.println("Failed to start server: " + e.getMessage());
        }
    }
}
